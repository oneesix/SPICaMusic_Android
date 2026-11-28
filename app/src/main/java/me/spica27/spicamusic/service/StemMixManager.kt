package me.spica27.spicamusic.service

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.spica27.spicamusic.automix.AutoMixSettings
import me.spica27.spicamusic.automix.AutoMixSettingsState
import me.spica27.spicamusic.automix.AutoMixStore
import me.spica27.spicamusic.automix.MixingLibrary
import me.spica27.spicamusic.automix.MixingSong
import me.spica27.spicamusic.automix.StemClipRenderer
import me.spica27.spicamusic.automix.StemMixPlanner
import me.spica27.spicamusic.automix.StemSongData
import timber.log.Timber
import java.io.File

/**
 * Builds the AutoMix transition to the next song on the phone, from the stems in Music/mixing.
 *
 * When a song starts (or the queue or the AutoMix settings change) and both the current and the next
 * song have mixing data, the clip is rendered in the background and handed to [AutoMixStore]. The
 * existing [AutoMixTransitionController] then plays it like an imported clip. Pairs that already have an
 * imported clip are left alone. Any failure leaves normal playback untouched.
 */
@OptIn(UnstableApi::class)
class StemMixManager(
    context: Context,
    private val player: ExoPlayer,
) : Player.Listener {
    private val appContext = context.applicationContext
    private val scope = MainScope()
    private var job: Job? = null
    private var lastKey: String? = null
    private var released = false

    fun attach() {
        AutoMixStore.init(appContext)
        AutoMixSettings.init(appContext)
        player.addListener(this)
        scope.launch { AutoMixStore.enabled.collect { evaluate() } }
        scope.launch { AutoMixSettings.state.collect { evaluate() } }
        // New or changed files in Music/mixing (after "Refresh"): try the current pair again.
        scope.launch {
            MixingLibrary.songs.drop(1).collect {
                lastKey = null
                evaluate()
            }
        }
    }

    fun release() {
        released = true
        player.removeListener(this)
        job?.cancel()
        scope.cancel()
    }

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: Int,
    ) = evaluate()

    override fun onTimelineChanged(
        timeline: Timeline,
        reason: Int,
    ) = evaluate()

    private fun evaluate() {
        if (released || !AutoMixStore.enabled.value) return
        if (player.mediaItemCount == 0) return
        val index = player.currentMediaItemIndex
        val next = player.nextMediaItemIndex
        if (next == C.INDEX_UNSET || next == index) return
        val from = player.getMediaItemAt(index)
        val to = player.getMediaItemAt(next)
        if (AutoMixStore.hasImported(from.mediaId, to.mediaId)) return
        val settings = AutoMixSettings.state.value
        val key = "${from.mediaId}>${to.mediaId}|$settings"
        if (key == lastKey) return
        lastKey = key
        job?.cancel()
        job = scope.launch { renderPair(from, to, settings) }
    }

    private suspend fun renderPair(
        from: MediaItem,
        to: MediaItem,
        settings: AutoMixSettingsState,
    ) {
        try {
            if (!MixingLibrary.scanned.value) MixingLibrary.refresh()
            val songA = findSong(from) ?: return
            val songB = findSong(to) ?: return
            if (!songA.stemsComplete || !songB.stemsComplete) return

            val dir = File(appContext.cacheDir, "automix_render")
            val outFile = File(dir, "${from.mediaId}_${to.mediaId}.wav")
            val transition =
                withContext(Dispatchers.Default) {
                    val dataA = StemSongData.load(songA) ?: return@withContext null
                    val dataB = StemSongData.load(songB) ?: return@withContext null
                    val plan =
                        StemMixPlanner.plan(dataA, dataB, from.mediaId, to.mediaId, settings)
                            ?: return@withContext null
                    StemClipRenderer.render(plan, dataA, dataB, outFile) { ensureActive() }
                }
            if (transition != null) {
                AutoMixStore.putRendered(transition)
            } else {
                AutoMixStore.removeRendered(from.mediaId, to.mediaId)
            }
            trimCache(dir)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Throwable) {
            Timber.e(e, "AutoMix: could not render the transition %s -> %s", from.mediaId, to.mediaId)
        }
    }

    /** The mixing data of a queue item: by MediaStore id, else by title and artist. */
    private fun findSong(item: MediaItem): MixingSong? {
        val songs = MixingLibrary.songs.value
        val id = item.mediaId.toLongOrNull()
        if (id != null) {
            songs.firstOrNull { it.mediaStoreId > 0 && it.mediaStoreId == id }?.let { return it }
        }
        val title = normalize(item.mediaMetadata.title?.toString())
        val artist = normalize(item.mediaMetadata.artist?.toString())
        if (title.isEmpty()) return null
        return songs.firstOrNull {
            val songArtist = normalize(it.artist)
            normalize(it.title) == title &&
                (artist.isEmpty() || songArtist.contains(artist) || artist.contains(songArtist))
        }
    }

    private fun normalize(text: String?): String = text.orEmpty().lowercase().trim().replace(Regex("\\s+"), " ")

    /** Keep only the newest few clips in the cache folder. */
    private fun trimCache(dir: File) {
        val files = dir.listFiles()?.sortedByDescending { it.lastModified() } ?: return
        for (file in files.drop(KEEP_CLIPS)) file.delete()
    }

    private companion object {
        const val KEEP_CLIPS = 8
    }
}
