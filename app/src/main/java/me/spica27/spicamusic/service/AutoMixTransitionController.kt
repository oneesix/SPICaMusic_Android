package me.spica27.spicamusic.service

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import me.spica27.spicamusic.automix.AutoMixStore
import me.spica27.spicamusic.automix.AutoMixTransition
import timber.log.Timber
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Plays an imported AutoMix clip in place of the end of a song.
 *
 * The clip is played by a second, silent-until-needed player. The main player keeps going (muted) so the
 * app's queue, progress and lyrics keep working. Both ends are joined with short cross-fades, and the
 * players are lined up first by nudging the playback speed of the player that is not audible.
 *
 *  ARMED    clip is loaded, waiting for the song to reach the clip start
 *  SYNC_IN  clip plays muted and is lined up with the song
 *  FADE_IN  song -> clip
 *  CLIP     clip is audible, main player is muted
 *  SYNC_OUT main player jumps to the next song (muted) and is lined up with the end of the clip
 *  FADE_OUT clip -> next song
 *
 * Any seek, skip or queue change during a transition cancels it and normal playback continues.
 * The clip does not go through the app's equalizer / DSP chain (it uses its own plain player).
 */
@OptIn(UnstableApi::class)
class AutoMixTransitionController(
    context: Context,
    private val main: ExoPlayer,
) : Player.Listener {
    private enum class Phase { IDLE, ARMED, SYNC_IN, FADE_IN, CLIP, SYNC_OUT, FADE_OUT }

    private val appContext = context.applicationContext
    private val handler = Handler(Looper.getMainLooper())
    private val scope = MainScope()
    private var aux: ExoPlayer? = null
    private var phase = Phase.IDLE
    private var active: AutoMixTransition? = null
    private var userVolume = 1f
    private var fadeStart = 0L
    private var selfSeekUntil = 0L
    private var released = false

    private val tick =
        object : Runnable {
            override fun run() {
                if (released) return
                try {
                    step()
                } catch (e: Exception) {
                    Timber.e(e, "AutoMix transition failed, back to normal playback")
                    finish(rearm = false)
                }
                if (phase != Phase.IDLE) handler.postDelayed(this, TICK_MS)
            }
        }

    fun attach() {
        main.addListener(this)
        scope.launch { AutoMixStore.enabled.collect { evaluate() } }
        scope.launch { AutoMixStore.transitions.collect { evaluate() } }
    }

    fun release() {
        released = true
        handler.removeCallbacks(tick)
        finish(rearm = false)
        main.removeListener(this)
        scope.cancel()
    }

    // ───────────────────────── Player events ─────────────────────────

    override fun onMediaItemTransition(
        mediaItem: MediaItem?,
        reason: Int,
    ) {
        if (isIdleOrArmed()) {
            evaluate()
            return
        }
        // The song ended on its own while the clip was playing: carry on.
        if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) return
        if (SystemClock.uptimeMillis() < selfSeekUntil) return
        finish(rearm = true)
    }

    override fun onTimelineChanged(
        timeline: Timeline,
        reason: Int,
    ) {
        if (isIdleOrArmed()) evaluate()
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int,
    ) {
        if (reason != Player.DISCONTINUITY_REASON_SEEK) return
        if (isIdleOrArmed()) {
            evaluate()
        } else if (SystemClock.uptimeMillis() >= selfSeekUntil) {
            finish(rearm = true)
        }
    }

    override fun onPlayWhenReadyChanged(
        playWhenReady: Boolean,
        reason: Int,
    ) {
        if (!isIdleOrArmed()) aux?.playWhenReady = playWhenReady
    }

    private fun isIdleOrArmed() = phase == Phase.IDLE || phase == Phase.ARMED

    // ───────────────────────── Arming ─────────────────────────

    private fun evaluate() {
        if (released || !isIdleOrArmed()) return
        disarm()
        if (!AutoMixStore.enabled.value) return
        if (main.mediaItemCount == 0 || main.playbackParameters.speed != 1f) return
        val index = main.currentMediaItemIndex
        val next = main.nextMediaItemIndex
        if (next == C.INDEX_UNSET || next == index) return
        val from = main.getMediaItemAt(index).mediaId
        val to = main.getMediaItemAt(next).mediaId
        val transition = AutoMixStore.find(from, to) ?: return
        if (main.currentPosition > transition.clipStartMs - 500) return

        val player =
            ExoPlayer
                .Builder(appContext)
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    false, // never fight the main player for audio focus
                ).build()
        player.volume = 0f
        player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(transition.clipPath))))
        player.playWhenReady = false
        player.prepare()
        aux = player
        active = transition
        phase = Phase.ARMED
        handler.removeCallbacks(tick)
        handler.post(tick)
    }

    private fun disarm() {
        handler.removeCallbacks(tick)
        aux?.release()
        aux = null
        active = null
        phase = Phase.IDLE
    }

    // ───────────────────────── The transition ─────────────────────────

    private fun step() {
        val t = active ?: return
        val a = aux ?: return
        when (phase) {
            Phase.ARMED -> {
                if (!AutoMixStore.enabled.value) {
                    disarm()
                    return
                }
                if (!main.isPlaying || a.playbackState != Player.STATE_READY) return
                val position = main.currentPosition
                if (position >= t.clipStartMs) {
                    if (position <= t.clipStartMs + START_WINDOW_MS) {
                        userVolume = main.volume
                        a.volume = 0f
                        a.playWhenReady = true
                        phase = Phase.SYNC_IN
                    } else {
                        disarm() // the song is already past the start point
                    }
                }
            }

            Phase.SYNC_IN -> {
                if (!main.isPlaying) return
                val auxPosition = a.currentPosition
                val error = (main.currentPosition - t.clipStartMs) - auxPosition // > 0: clip is behind
                a.setPlaybackSpeed(speedFor(error))
                if ((abs(error) <= ALIGN_MS && auxPosition >= 250) || auxPosition >= SYNC_IN_MAX_MS) {
                    a.setPlaybackSpeed(1f)
                    startFade(Phase.FADE_IN)
                }
            }

            Phase.FADE_IN -> {
                val x = fadeProgress()
                a.volume = sin(x * PI.toFloat() / 2f)
                main.volume = userVolume * cos(x * PI.toFloat() / 2f)
                if (x >= 1f) {
                    a.volume = 1f
                    main.volume = 0f
                    phase = Phase.CLIP
                }
            }

            Phase.CLIP -> {
                val remaining = t.clipDurationMs - a.currentPosition
                if (a.playbackState == Player.STATE_ENDED || remaining <= 0) {
                    finish(rearm = true)
                } else if (remaining <= SYNC_OUT_LEAD_MS) {
                    startSyncOut(t, remaining)
                }
            }

            Phase.SYNC_OUT -> {
                val remaining = t.clipDurationMs - a.currentPosition
                if (a.playbackState == Player.STATE_ENDED || remaining <= 0) {
                    main.volume = userVolume
                    finish(rearm = true)
                    return
                }
                if (!main.isPlaying || main.playbackState != Player.STATE_READY) return
                val error = (t.resumeMs - remaining) - main.currentPosition // > 0: main is behind
                main.setPlaybackSpeed(speedFor(error))
                if ((abs(error) <= ALIGN_MS && remaining <= FADE_OUT_WINDOW_MS) || remaining <= FADE_OUT_FORCE_MS) {
                    main.setPlaybackSpeed(1f)
                    startFade(Phase.FADE_OUT)
                }
            }

            Phase.FADE_OUT -> {
                val x = fadeProgress()
                main.volume = userVolume * sin(x * PI.toFloat() / 2f)
                a.volume = cos(x * PI.toFloat() / 2f)
                if (x >= 1f) finish(rearm = true)
            }

            Phase.IDLE -> Unit
        }
    }

    private fun startSyncOut(
        t: AutoMixTransition,
        remaining: Long,
    ) {
        val index =
            if (main.currentMediaItem?.mediaId == t.toMediaId) {
                main.currentMediaItemIndex
            } else {
                main.nextMediaItemIndex.takeIf {
                    it != C.INDEX_UNSET && main.getMediaItemAt(it).mediaId == t.toMediaId
                }
            }
        if (index == null) {
            // The queue changed under us: give the song back and stop.
            finish(rearm = true)
            return
        }
        val start = (t.resumeMs - remaining).coerceAtLeast(0L)
        selfSeekUntil = SystemClock.uptimeMillis() + SELF_SEEK_GRACE_MS
        main.seekTo(index, start)
        main.playWhenReady = true
        phase = Phase.SYNC_OUT
    }

    private fun startFade(next: Phase) {
        fadeStart = SystemClock.uptimeMillis()
        phase = next
    }

    private fun fadeProgress(): Float = ((SystemClock.uptimeMillis() - fadeStart).toFloat() / FADE_MS).coerceIn(0f, 1f)

    private fun speedFor(errorMs: Long): Float = 1f + (errorMs / 800f).coerceIn(-MAX_SPEED_NUDGE, MAX_SPEED_NUDGE)

    /** Stop everything and hand playback back to the main player at normal volume and speed. */
    private fun finish(rearm: Boolean) {
        val wasRunning = phase != Phase.IDLE && phase != Phase.ARMED
        handler.removeCallbacks(tick)
        if (wasRunning) {
            main.volume = userVolume
            main.setPlaybackSpeed(1f)
        }
        aux?.release()
        aux = null
        active = null
        phase = Phase.IDLE
        if (rearm && !released) handler.post { evaluate() }
    }

    private companion object {
        const val TICK_MS = 10L
        const val START_WINDOW_MS = 800L
        const val ALIGN_MS = 12L
        const val SYNC_IN_MAX_MS = 1700L
        const val SYNC_OUT_LEAD_MS = 4500L
        const val FADE_OUT_WINDOW_MS = 1200L
        const val FADE_OUT_FORCE_MS = 500L
        const val FADE_MS = 120L
        const val SELF_SEEK_GRACE_MS = 800L
        const val MAX_SPEED_NUDGE = 0.10f
    }
}
