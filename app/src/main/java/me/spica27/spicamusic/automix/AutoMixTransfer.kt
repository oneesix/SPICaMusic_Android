package me.spica27.spicamusic.automix

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.spica27.spicamusic.common.entity.Song
import me.spica27.spicamusic.feature.library.domain.PlaylistUseCases
import me.spica27.spicamusic.feature.library.domain.SongUseCases
import org.json.JSONArray
import org.json.JSONObject
import org.koin.core.context.GlobalContext
import timber.log.Timber
import java.io.File
import java.io.InputStream
import java.security.DigestInputStream
import java.security.MessageDigest
import java.util.Locale
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.abs

/** Why an import line was skipped. The dialog turns this into a localized message. */
enum class AutoMixSkipReason { SongNotFound, ClipMissing }

/** Why a whole export/import failed. */
enum class AutoMixFailReason { EmptyPlaylist, InvalidZip, Generic }

data class AutoMixImportLine(
    val ok: Boolean,
    val title: String,
    val reason: AutoMixSkipReason? = null,
)

sealed interface AutoMixTransferState {
    data object Idle : AutoMixTransferState

    data class Exporting(
        val done: Int,
        val total: Int,
        val title: String,
    ) : AutoMixTransferState

    data class ExportDone(
        val fileName: String,
        val uri: Uri,
    ) : AutoMixTransferState

    data object Importing : AutoMixTransferState

    data class ImportDone(
        val importedCount: Int,
        val skippedCount: Int,
        val lines: List<AutoMixImportLine>,
    ) : AutoMixTransferState

    data class Failed(
        val reason: AutoMixFailReason,
        val detail: String = "",
    ) : AutoMixTransferState
}

/**
 * Export a playlist as a zip for the AutoMix Colab notebook, and import the result zip.
 *
 * Export zip  (Download/AutoMix/<name>.automix-in.zip):
 *   001_<title>.<ext> ... + playlist.json (written last)
 * Import zip  (<name>.automix-out.zip):
 *   automix.json + the transition clips it lists.
 *
 * Imported clips live in app-private storage: filesDir/.automix/ (with .nomedia),
 * not in the cache directory. This patch only stores them; playback is unchanged.
 */
object AutoMixTransfer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow<AutoMixTransferState>(AutoMixTransferState.Idle)
    val state: StateFlow<AutoMixTransferState> = _state.asStateFlow()
    private var job: Job? = null

    private val audioExtensions = setOf("m4a", "mp3", "aac", "flac", "wav", "ogg", "opus")

    private fun isBusy(): Boolean {
        val s = _state.value
        return s is AutoMixTransferState.Exporting || s is AutoMixTransferState.Importing
    }

    /** Clear a finished result so the next run starts clean. Does nothing while busy. */
    fun reset() {
        if (!isBusy()) _state.value = AutoMixTransferState.Idle
    }

    /** Cancel the running export/import. */
    fun cancel() {
        job?.cancel()
        _state.value = AutoMixTransferState.Idle
    }

    // ───────────────────────────── Export ─────────────────────────────

    fun startExport(
        context: Context,
        playlistId: Long,
        playlistName: String,
    ) {
        if (isBusy()) return
        val app = context.applicationContext
        _state.value = AutoMixTransferState.Exporting(0, 0, playlistName)
        job =
            scope.launch {
                var pending: Uri? = null
                var finished = false
                try {
                    val songs =
                        GlobalContext
                            .get()
                            .get<PlaylistUseCases>()
                            .getSongsByPlaylistIdList(playlistId)
                    if (songs.isEmpty()) {
                        _state.value = AutoMixTransferState.Failed(AutoMixFailReason.EmptyPlaylist)
                        return@launch
                    }

                    val resolver = app.contentResolver
                    val zipName = "${safeName(playlistName, 40)}.automix-in.zip"
                    val values =
                        ContentValues().apply {
                            put(MediaStore.Downloads.DISPLAY_NAME, zipName)
                            put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                            put(MediaStore.Downloads.RELATIVE_PATH, "Download/AutoMix")
                            put(MediaStore.Downloads.IS_PENDING, 1)
                        }
                    val uri =
                        resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                            ?: error("Could not create the zip in Downloads")
                    pending = uri

                    val tracks = JSONArray()
                    val output = resolver.openOutputStream(uri) ?: error("Could not open the zip for writing")
                    ZipOutputStream(output.buffered()).use { zip ->
                        // Songs are already compressed, so skip compression (faster, same size).
                        zip.setLevel(Deflater.NO_COMPRESSION)
                        songs.forEachIndexed { index, song ->
                            ensureActive()
                            _state.value = AutoMixTransferState.Exporting(index, songs.size, song.displayName)
                            val input = openSong(app, song)
                            if (input == null) {
                                Timber.w("AutoMix export: cannot read %s, skipped", song.displayName)
                                return@forEachIndexed
                            }
                            val ext = extensionOf(song)
                            val base = stripExtension(song.displayName, ext)
                            val entryName =
                                String.format(Locale.ROOT, "%03d_%s.%s", index + 1, safeName(base, 60), ext)
                            val digest = MessageDigest.getInstance("SHA-256")
                            zip.putNextEntry(ZipEntry(entryName))
                            DigestInputStream(input, digest).use { it.copyTo(zip, 64 * 1024) }
                            zip.closeEntry()
                            tracks.put(
                                JSONObject()
                                    .put("position", index + 1)
                                    .put("file", entryName)
                                    .put("songId", song.songId ?: -1L)
                                    .put("mediaStoreId", song.mediaStoreId)
                                    .put("title", song.displayName)
                                    .put("artist", song.artist)
                                    .put("durationMs", song.duration)
                                    .put("size", song.size)
                                    .put("sha256", digest.digest().joinToString("") { "%02x".format(it) }),
                            )
                        }
                        val manifest =
                            JSONObject()
                                .put("format", "automix-in")
                                .put("version", 1)
                                .put("playlist", JSONObject().put("name", playlistName))
                                .put("created", System.currentTimeMillis())
                                .put("tracks", tracks)
                        zip.putNextEntry(ZipEntry("playlist.json"))
                        zip.write(manifest.toString(2).toByteArray(Charsets.UTF_8))
                        zip.closeEntry()
                    }

                    resolver.update(
                        uri,
                        ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) },
                        null,
                        null,
                    )
                    finished = true
                    _state.value = AutoMixTransferState.ExportDone(zipName, uri)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "AutoMix export failed")
                    _state.value = AutoMixTransferState.Failed(AutoMixFailReason.Generic, e.message.orEmpty())
                } finally {
                    if (!finished) {
                        pending?.let { runCatching { app.contentResolver.delete(it, null, null) } }
                    }
                }
            }
    }

    private fun openSong(
        context: Context,
        song: Song,
    ): InputStream? {
        val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.mediaStoreId)
        runCatching { context.contentResolver.openInputStream(uri) }.getOrNull()?.let { return it }
        val file = File(song.path)
        return if (file.exists()) file.inputStream() else null
    }

    private fun extensionOf(song: Song): String {
        val fromPath = song.path.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (fromPath.length in 1..5 && fromPath.all { it.isLetterOrDigit() }) return fromPath
        return when {
            song.mimeType.contains("mpeg") -> "mp3"
            song.mimeType.contains("flac") -> "flac"
            song.mimeType.contains("mp4") || song.mimeType.contains("aac") -> "m4a"
            song.mimeType.contains("ogg") -> "ogg"
            song.mimeType.contains("wav") -> "wav"
            else -> "audio"
        }
    }

    private fun stripExtension(
        name: String,
        ext: String,
    ): String = if (name.endsWith(".$ext", ignoreCase = true)) name.dropLast(ext.length + 1) else name

    /** Letters and digits of any language are kept; everything else becomes "_". */
    private fun safeName(
        text: String,
        maxLength: Int,
    ): String {
        val cleaned =
            text
                .replace(Regex("[^\\p{L}\\p{N}]+"), "_")
                .trim('_')
                .take(maxLength)
                .trim('_')
        return cleaned.ifEmpty { "track" }
    }

    // ───────────────────────────── Import ─────────────────────────────

    fun startImport(
        context: Context,
        zipUri: Uri,
    ) {
        if (isBusy()) return
        val app = context.applicationContext
        _state.value = AutoMixTransferState.Importing
        job =
            scope.launch {
                val root = automixRoot(app)
                val staging = File(root, "staging").apply { deleteRecursively() }
                staging.mkdirs()
                try {
                    var manifestText: String? = null
                    val extracted = mutableMapOf<String, File>()
                    val stream = app.contentResolver.openInputStream(zipUri) ?: error("Could not open the file")
                    ZipInputStream(stream.buffered()).use { zin ->
                        var entry = zin.nextEntry
                        while (entry != null) {
                            ensureActive()
                            if (!entry.isDirectory) {
                                // Keep only the last path segment so an entry can never escape the folder.
                                val name = File(entry.name).name
                                if (name.equals("automix.json", ignoreCase = true)) {
                                    manifestText = zin.readBytes().toString(Charsets.UTF_8)
                                } else if (name.substringAfterLast('.', "").lowercase(Locale.ROOT) in audioExtensions) {
                                    val target = File(staging, name)
                                    target.outputStream().use { zin.copyTo(it) }
                                    extracted[name] = target
                                }
                            }
                            zin.closeEntry()
                            entry = zin.nextEntry
                        }
                    }

                    val text = manifestText
                    if (text == null) {
                        _state.value = AutoMixTransferState.Failed(AutoMixFailReason.InvalidZip)
                        return@launch
                    }
                    val manifest = JSONObject(text)
                    if (manifest.optString("format") != "automix-out") {
                        _state.value = AutoMixTransferState.Failed(AutoMixFailReason.InvalidZip)
                        return@launch
                    }
                    val playlistName = manifest.optJSONObject("playlist")?.optString("name").orEmpty()
                    val transitions = manifest.optJSONArray("transitions") ?: JSONArray()

                    val library = GlobalContext.get().get<SongUseCases>().getAllSongs()
                    val clipsDir = File(root, "clips").apply { mkdirs() }
                    val index = loadIndex(root)
                    val lines = mutableListOf<AutoMixImportLine>()
                    var imported = 0

                    for (i in 0 until transitions.length()) {
                        ensureActive()
                        val t = transitions.optJSONObject(i) ?: continue
                        val fromRef = t.optJSONObject("from") ?: JSONObject()
                        val toRef = t.optJSONObject("to") ?: JSONObject()
                        val label = "${fromRef.optString("title")} → ${toRef.optString("title")}"
                        val from = findSong(library, fromRef)
                        val to = findSong(library, toRef)
                        val clipName = File(t.optString("clip")).name
                        val clipFile = extracted[clipName]
                        val fromId = from?.songId
                        val toId = to?.songId
                        if (fromId == null || toId == null) {
                            lines += AutoMixImportLine(false, label, AutoMixSkipReason.SongNotFound)
                            continue
                        }
                        if (clipFile == null) {
                            lines += AutoMixImportLine(false, label, AutoMixSkipReason.ClipMissing)
                            continue
                        }
                        val ext = clipName.substringAfterLast('.', "m4a")
                        val finalName = "${fromId}_$toId.$ext"
                        clipFile.copyTo(File(clipsDir, finalName), overwrite = true)
                        // One transition per song pair: replace any older entry.
                        for (k in index.length() - 1 downTo 0) {
                            val old = index.getJSONObject(k)
                            if (old.optLong("fromSongId") == fromId && old.optLong("toSongId") == toId) {
                                val oldClip = old.optString("clip")
                                if (oldClip != finalName) File(clipsDir, oldClip).delete()
                                index.remove(k)
                            }
                        }
                        index.put(
                            JSONObject()
                                .put("fromSongId", fromId)
                                .put("toSongId", toId)
                                .put("clip", finalName)
                                .put("cueOutMs", t.optLong("cueOutMs", -1L))
                                .put("resumeToMs", t.optLong("resumeToMs", -1L))
                                .put("playlist", playlistName)
                                .put("importedAt", System.currentTimeMillis()),
                        )
                        imported++
                        lines += AutoMixImportLine(true, label)
                    }
                    saveIndex(root, index)
                    _state.value =
                        AutoMixTransferState.ImportDone(
                            importedCount = imported,
                            skippedCount = lines.size - imported,
                            lines = lines,
                        )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "AutoMix import failed")
                    _state.value = AutoMixTransferState.Failed(AutoMixFailReason.Generic, e.message.orEmpty())
                } finally {
                    staging.deleteRecursively()
                }
            }
    }

    /**
     * Match a manifest song to the library: the stored song id (only if the title also matches,
     * in case the library was rebuilt), then title + artist + duration (within 2 s).
     * The manifest also carries a SHA-256 for a later, stricter check.
     */
    private fun findSong(
        library: List<Song>,
        ref: JSONObject,
    ): Song? {
        val id = ref.optLong("songId", -1L)
        val title = ref.optString("title")
        val artist = ref.optString("artist")
        val duration = ref.optLong("durationMs", -1L)
        if (title.isEmpty()) return null
        fun sameTitle(s: Song) = s.displayName.equals(title, ignoreCase = true)
        if (id >= 0) {
            library.firstOrNull { it.songId == id && sameTitle(it) }?.let { return it }
        }
        return library.firstOrNull {
            sameTitle(it) &&
                it.artist.equals(artist, ignoreCase = true) &&
                (duration < 0 || abs(it.duration - duration) <= 2000)
        }
    }

    /** Hidden, app-private folder (filesDir, not cacheDir) with a .nomedia marker. */
    private fun automixRoot(context: Context): File {
        val root = File(context.filesDir, ".automix").apply { mkdirs() }
        val marker = File(root, ".nomedia")
        if (!marker.exists()) runCatching { marker.createNewFile() }
        return root
    }

    private fun loadIndex(root: File): JSONArray {
        val file = File(root, "index.json")
        if (!file.exists()) return JSONArray()
        return runCatching { JSONObject(file.readText()).getJSONArray("transitions") }.getOrDefault(JSONArray())
    }

    private fun saveIndex(
        root: File,
        transitions: JSONArray,
    ) {
        val json = JSONObject().put("version", 1).put("transitions", transitions)
        val tmp = File(root, "index.json.tmp")
        tmp.writeText(json.toString(2))
        tmp.renameTo(File(root, "index.json"))
    }
}
