package me.spica27.spicamusic.automix

import android.content.Context
import android.media.MediaMetadataRetriever
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import me.spica27.spicamusic.feature.library.domain.SongUseCases
import org.json.JSONObject
import org.koin.core.context.GlobalContext
import timber.log.Timber
import java.io.File

/**
 * One imported transition between two songs.
 * Song ids are the player's media ids (the MediaStore id as text), so the playback service can match them.
 */
data class AutoMixTransition(
    val fromMediaId: String,
    val toMediaId: String,
    val clipPath: String,
    /** Position in the outgoing song where the clip starts. */
    val clipStartMs: Long,
    /** Position in the incoming song where normal playback continues after the clip. */
    val resumeMs: Long,
    val clipDurationMs: Long,
)

/** The on/off setting and the list of imported transitions (read from filesDir/.automix/index.json). */
object AutoMixStore {
    private const val PREFS = "automix_prefs"
    private const val KEY_ENABLED = "enabled"

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _transitions = MutableStateFlow<Map<Pair<String, String>, AutoMixTransition>>(emptyMap())
    val transitions: StateFlow<Map<Pair<String, String>, AutoMixTransition>> = _transitions.asStateFlow()

    // Imported clips (from the zip) and clips the phone rendered from stems. An imported clip wins.
    private var imported: Map<Pair<String, String>, AutoMixTransition> = emptyMap()
    private val rendered = HashMap<Pair<String, String>, AutoMixTransition>()

    private fun publish() {
        _transitions.value = synchronized(rendered) { HashMap(rendered) + imported }
    }

    /** true when a clip for this pair came from an imported zip. */
    fun hasImported(
        fromMediaId: String,
        toMediaId: String,
    ): Boolean = imported.containsKey(fromMediaId to toMediaId)

    /** Add or replace a clip that was rendered on the phone. */
    fun putRendered(transition: AutoMixTransition) {
        synchronized(rendered) { rendered[transition.fromMediaId to transition.toMediaId] = transition }
        publish()
    }

    fun removeRendered(
        fromMediaId: String,
        toMediaId: String,
    ) {
        val removed = synchronized(rendered) { rendered.remove(fromMediaId to toMediaId) }
        if (removed != null) publish()
    }

    @Volatile
    private var prefsLoaded = false

    /** Read the saved on/off value once. Safe to call many times. */
    fun init(context: Context) {
        if (prefsLoaded) return
        _enabled.value =
            context.applicationContext
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getBoolean(KEY_ENABLED, false)
        prefsLoaded = true
    }

    fun setEnabled(
        context: Context,
        value: Boolean,
    ) {
        init(context)
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ENABLED, value)
            .apply()
        _enabled.value = value
    }

    fun find(
        fromMediaId: String,
        toMediaId: String,
    ): AutoMixTransition? = _transitions.value[fromMediaId to toMediaId]

    /** Reload the imported transitions. Call after an import and when the service starts. */
    suspend fun refresh(context: Context) =
        withContext(Dispatchers.IO) {
            try {
                val root = File(context.applicationContext.filesDir, ".automix")
                val indexFile = File(root, "index.json")
                if (!indexFile.exists()) {
                    imported = emptyMap()
                    publish()
                    return@withContext
                }
                val array = JSONObject(indexFile.readText()).getJSONArray("transitions")
                val songs = GlobalContext.get().get<SongUseCases>().getAllSongs()
                val mediaIdBySongId = HashMap<Long, String>()
                for (song in songs) {
                    val id = song.songId ?: continue
                    mediaIdBySongId[id] = song.mediaStoreId.toString()
                }
                val result = HashMap<Pair<String, String>, AutoMixTransition>()
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    val from = mediaIdBySongId[o.optLong("fromSongId", -1L)] ?: continue
                    val to = mediaIdBySongId[o.optLong("toSongId", -1L)] ?: continue
                    val clip = File(root, "clips/" + o.optString("clip"))
                    val start = o.optLong("cueOutMs", -1L)
                    val resume = o.optLong("resumeToMs", -1L)
                    if (!clip.exists() || start < 0 || resume < 0) continue
                    val duration = clipDurationMs(clip) ?: continue
                    result[from to to] = AutoMixTransition(from, to, clip.absolutePath, start, resume, duration)
                }
                imported = result
                publish()
            } catch (e: Exception) {
                Timber.e(e, "AutoMix: could not load transitions")
            }
        }

    private fun clipDurationMs(file: File): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()
        } catch (e: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }
}
