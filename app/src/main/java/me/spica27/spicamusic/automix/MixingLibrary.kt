package me.spica27.spicamusic.automix

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.io.File

/** One song that has mixing data (a folder with song.json and the four stems). */
data class MixingSong(
    /** The song folder: Music/mixing/artist/album/song. */
    val folder: File,
    /** "artist/album/song", used as the key in lists. */
    val relativePath: String,
    val title: String,
    val artist: String,
    val album: String,
    val songId: Long,
    val mediaStoreId: Long,
    val durationMs: Long,
    val sha256: String,
    /** true when drums, bass, other and vocals all exist next to song.json. */
    val stemsComplete: Boolean,
)

/**
 * The songs that have mixing data in Music/mixing/<artist>/<album>/<song>/ (made by the Colab notebook).
 * A song counts when its folder holds a valid song.json. The files are read with the
 * "All files access" permission, so they stay in a normal folder and not in app storage.
 */
object MixingLibrary {
    const val FOLDER_PATH = "Music/mixing"
    private const val SONG_FILE = "song.json"
    private val STEM_NAMES = listOf("drums", "bass", "other", "vocals")

    private val _songs = MutableStateFlow<List<MixingSong>>(emptyList())
    val songs: StateFlow<List<MixingSong>> = _songs.asStateFlow()

    private val _scanned = MutableStateFlow(false)

    /** true after the first scan finished (so the screen can tell "not scanned yet" from "empty"). */
    val scanned: StateFlow<Boolean> = _scanned.asStateFlow()

    fun rootDir(): File = File(Environment.getExternalStorageDirectory(), FOLDER_PATH)

    /** "All files access" exists from Android 11. */
    fun isSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

    fun hasAllFilesAccess(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()

    /** Opens the system screen where the user allows "All files access" for this app. */
    fun accessSettingsIntent(context: Context): Intent =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:${context.packageName}"),
            )
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))
        }

    /** Scan Music/mixing again. Without the permission the list is empty. */
    suspend fun refresh() =
        withContext(Dispatchers.IO) {
            val found = ArrayList<MixingSong>()
            try {
                val root = rootDir()
                if (hasAllFilesAccess() && root.isDirectory) {
                    for (artistDir in root.subFolders()) {
                        for (albumDir in artistDir.subFolders()) {
                            for (songDir in albumDir.subFolders()) {
                                readSong(root, songDir)?.let(found::add)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Could not scan %s", FOLDER_PATH)
            }
            _songs.value = found.sortedBy { it.relativePath.lowercase() }
            _scanned.value = true
        }

    private fun File.subFolders(): List<File> =
        listFiles { f -> f.isDirectory && !f.name.startsWith(".") && !f.name.endsWith(".building") }
            ?.sortedBy { it.name.lowercase() }
            ?: emptyList()

    private fun readSong(
        root: File,
        songDir: File,
    ): MixingSong? {
        val file = File(songDir, SONG_FILE)
        if (!file.isFile) return null
        return try {
            val json = JSONObject(file.readText(Charsets.UTF_8))
            if (json.optString("format") != "automix-song") return null
            val stemFiles = json.optJSONObject("stems")?.optJSONObject("files")
            val complete =
                stemFiles != null &&
                    STEM_NAMES.all { name ->
                        val fileName = stemFiles.optString(name)
                        fileName.isNotEmpty() && File(songDir, fileName).isFile
                    }
            MixingSong(
                folder = songDir,
                relativePath = songDir.relativeTo(root).invariantSeparatorsPath,
                title = json.optString("title"),
                artist = json.optString("artist"),
                album = json.optString("album"),
                songId = json.optLong("songId", -1L),
                mediaStoreId = json.optLong("mediaStoreId", -1L),
                durationMs = json.optLong("durationMs", 0L),
                sha256 = json.optString("sha256"),
                stemsComplete = complete,
            )
        } catch (e: Exception) {
            Timber.w(e, "Bad song.json in %s", songDir.path)
            null
        }
    }
}
