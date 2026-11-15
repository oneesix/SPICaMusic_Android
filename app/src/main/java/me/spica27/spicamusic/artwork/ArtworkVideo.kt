package me.spica27.spicamusic.artwork

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Finds a video cover next to a song. Looked up in the song's folder, in this order:
 * a video named like the song (Song.mp4), then cover.mp4, then folder.mp4.
 * It reads the file system first, then the extra folders the user already granted (same as folder covers).
 */
object ArtworkVideo {
    private val EXTENSIONS = setOf("mp4", "m4v", "mov", "webm")

    suspend fun find(
        context: Context,
        songPath: String,
    ): Uri? {
        if (songPath.isBlank()) return null
        return withContext(Dispatchers.IO) {
            val song = File(songPath)
            val dir = song.parentFile ?: return@withContext null
            val names = listOf(song.nameWithoutExtension, "cover", "folder")
            fromFile(dir, names) ?: fromTree(context, dir, names)
        }
    }

    private fun fromFile(
        dir: File,
        names: List<String>,
    ): Uri? =
        try {
            val files =
                dir
                    .listFiles()
                    ?.filter { it.isFile && it.canRead() && it.extension.lowercase() in EXTENSIONS }
                    .orEmpty()
            names
                .firstNotNullOfOrNull { name -> files.firstOrNull { it.nameWithoutExtension.equals(name, ignoreCase = true) } }
                ?.let { Uri.fromFile(it) }
        } catch (e: Exception) {
            null
        }

    private fun fromTree(
        context: Context,
        dir: File,
        names: List<String>,
    ): Uri? {
        val dirPath = dir.absolutePath
        val root =
            ArtworkLoader
                .treeRootsSnapshot()
                .filter { dirPath == it.pathPrefix || dirPath.startsWith(it.pathPrefix + "/") }
                .maxByOrNull { it.pathPrefix.length }
                ?: return null
        return try {
            val treeDocId = DocumentsContract.getTreeDocumentId(root.treeUri)
            val relative = dirPath.removePrefix(root.pathPrefix).trimStart('/')
            val dirDocId =
                when {
                    relative.isEmpty() -> treeDocId
                    treeDocId.endsWith(":") -> treeDocId + relative
                    else -> "$treeDocId/$relative"
                }
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(root.treeUri, dirDocId)
            val projection =
                arrayOf(
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                    DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                )
            val found = mutableMapOf<String, String>()
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val docId = cursor.getString(0) ?: continue
                    val name = cursor.getString(1) ?: continue
                    if (name.substringAfterLast('.', "").lowercase() in EXTENSIONS) {
                        found[name.substringBeforeLast('.').lowercase()] = docId
                    }
                }
            }
            val docId = names.firstNotNullOfOrNull { found[it.lowercase()] } ?: return null
            DocumentsContract.buildDocumentUriUsingTree(root.treeUri, docId)
        } catch (e: Exception) {
            null
        }
    }
}
