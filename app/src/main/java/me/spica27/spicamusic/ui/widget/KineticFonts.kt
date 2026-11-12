package me.spica27.spicamusic.ui.widget

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.spica27.spicamusic.core.preferences.LyricsStyleSettings
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URL

/** A font the kinetic lyrics can use. [url] is set for fonts that are downloaded the first time they are picked. */
internal data class KineticFontOption(
    val id: String,
    val label: String,
    val url: String?,
    val available: Boolean,
)

/**
 * Fonts for the kinetic lyrics: a few fonts that every phone has, fonts that are downloaded on first use
 * (Playfair Display and more) and fonts the user imports (TTF or OTF). Downloaded and imported files are
 * kept in the app's private storage.
 */
internal object KineticFontStore {
    /** Changes whenever a font file is added, so lists and previews refresh. */
    val revision = mutableIntStateOf(0)

    private const val BASE = "https://raw.githubusercontent.com/google/fonts/main/ofl/"
    private const val IMPORT_PREFIX = "imp_"

    private val builtin: List<Pair<Pair<String, String>, FontFamily>> by lazy {
        listOf(
            ("sans" to "Sans") to FontFamily.SansSerif,
            ("serif" to "Serif") to FontFamily.Serif,
            ("mono" to "Mono") to FontFamily.Monospace,
            ("cursive" to "Cursive") to FontFamily.Cursive,
            ("condensed" to "Condensed") to FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD)),
            ("casual" to "Casual") to FontFamily(Typeface.create("casual", Typeface.NORMAL)),
        )
    }

    private val downloadable =
        listOf(
            Triple("playfair", "Playfair Display", BASE + "playfairdisplay/PlayfairDisplay%5Bwght%5D.ttf"),
            Triple("abril", "Abril Fatface", BASE + "abrilfatface/AbrilFatface-Regular.ttf"),
            Triple("bebas", "Bebas Neue", BASE + "bebasneue/BebasNeue-Regular.ttf"),
            Triple("anton", "Anton", BASE + "anton/Anton-Regular.ttf"),
            Triple("archivoblack", "Archivo Black", BASE + "archivoblack/ArchivoBlack-Regular.ttf"),
            Triple("bungee", "Bungee", BASE + "bungee/Bungee-Regular.ttf"),
            Triple("righteous", "Righteous", BASE + "righteous/Righteous-Regular.ttf"),
            Triple("lobster", "Lobster", BASE + "lobster/Lobster-Regular.ttf"),
            Triple("pacifico", "Pacifico", BASE + "pacifico/Pacifico-Regular.ttf"),
            Triple("dancing", "Dancing Script", BASE + "dancingscript/DancingScript%5Bwght%5D.ttf"),
            Triple("marker", "Permanent Marker", BASE + "permanentmarker/PermanentMarker-Regular.ttf"),
        )

    private val cache = HashMap<String, FontFamily?>()

    private fun dir(context: Context): File = File(context.filesDir, "kinetic_fonts").apply { mkdirs() }

    private fun fileFor(
        context: Context,
        id: String,
    ): File? = dir(context).listFiles()?.firstOrNull { it.nameWithoutExtension == id && (it.extension == "ttf" || it.extension == "otf") }

    fun options(context: Context): List<KineticFontOption> {
        val out = ArrayList<KineticFontOption>()
        builtin.forEach { (key, _) -> out += KineticFontOption(key.first, key.second, null, true) }
        downloadable.forEach { (id, label, url) -> out += KineticFontOption(id, label, url, fileFor(context, id) != null) }
        dir(context)
            .listFiles()
            .orEmpty()
            .filter { it.name.startsWith(IMPORT_PREFIX) && (it.extension == "ttf" || it.extension == "otf") }
            .sortedBy { it.name }
            .forEach { f ->
                val id = f.nameWithoutExtension
                out += KineticFontOption(id, id.removePrefix(IMPORT_PREFIX).replace('_', ' '), null, true)
            }
        return out
    }

    /** The font family for [id], or null when it is not available (not downloaded yet or broken). */
    fun family(
        context: Context,
        id: String,
    ): FontFamily? {
        builtin.firstOrNull { it.first.first == id }?.let { return it.second }
        if (cache.containsKey(id)) return cache[id]
        val file = fileFor(context, id)
        val family =
            if (file == null) {
                null
            } else {
                runCatching { FontFamily(Font(file, FontWeight.Black)) }.getOrNull()
            }
        if (file != null) cache[id] = family
        return family
    }

    /** Downloads a font to the app's storage. Returns true when it is ready to use. */
    suspend fun download(
        context: Context,
        option: KineticFontOption,
    ): Boolean =
        withContext(Dispatchers.IO) {
            val url = option.url ?: return@withContext option.available
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 15_000
                connection.readTimeout = 30_000
                connection.instanceFollowRedirects = true
                if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext false
                val part = File(dir(context), option.id + ".part")
                connection.inputStream.use { input -> part.outputStream().use { output -> input.copyTo(output) } }
                if (!looksLikeFont(part)) {
                    part.delete()
                    return@withContext false
                }
                val target = File(dir(context), option.id + ".ttf")
                target.delete()
                if (!part.renameTo(target)) return@withContext false
                cache.remove(option.id)
                revision.intValue++
                true
            } catch (e: Exception) {
                false
            }
        }

    /** Copies a TTF or OTF the user picked into the app's storage. Returns the new font id, or null. */
    suspend fun importFont(
        context: Context,
        uri: Uri,
    ): String? =
        withContext(Dispatchers.IO) {
            try {
                val name = displayName(context, uri) ?: "font.ttf"
                val ext = name.substringAfterLast('.', "ttf").lowercase()
                if (ext != "ttf" && ext != "otf") return@withContext null
                val base =
                    name
                        .substringBeforeLast('.')
                        .replace(Regex("[^A-Za-z0-9 _-]"), "")
                        .trim()
                        .replace(' ', '_')
                        .take(40)
                        .ifEmpty { "Font" }
                val id = IMPORT_PREFIX + base
                val part = File(dir(context), "$id.part")
                context.contentResolver.openInputStream(uri)?.use { input -> part.outputStream().use { output -> input.copyTo(output) } }
                    ?: return@withContext null
                if (!looksLikeFont(part)) {
                    part.delete()
                    return@withContext null
                }
                dir(context).listFiles()?.filter { it.nameWithoutExtension == id }?.forEach { it.delete() }
                val target = File(dir(context), "$id.$ext")
                if (!part.renameTo(target)) return@withContext null
                cache.remove(id)
                revision.intValue++
                id
            } catch (e: Exception) {
                null
            }
        }

    private fun displayName(
        context: Context,
        uri: Uri,
    ): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }

    /** TrueType / OpenType files start with a known tag. */
    private fun looksLikeFont(file: File): Boolean {
        val head = ByteArray(4)
        val read = FileInputStream(file).use { it.read(head) }
        if (read < 4) return false
        val tag = String(head, Charsets.ISO_8859_1)
        return tag == "OTTO" || tag == "true" || tag == "ttcf" ||
            (head[0].toInt() == 0 && head[1].toInt() == 1 && head[2].toInt() == 0 && head[3].toInt() == 0)
    }
}

/** The font list for the settings sheet; refreshes after a download or an import. */
@Composable
internal fun rememberKineticFontOptions(): List<KineticFontOption> {
    val context = LocalContext.current.applicationContext
    val revision = KineticFontStore.revision.intValue
    return remember(revision) { KineticFontStore.options(context) }
}

/**
 * The font slots of the kinetic lyrics. Slot k belongs to colour k, so a colour always comes with the same font.
 * A null entry means the app's normal font. With "Mix fonts" off there is one slot (the normal font).
 */
@Composable
internal fun rememberKineticFontSlots(settings: LyricsStyleSettings): List<FontFamily?> {
    val context = LocalContext.current.applicationContext
    val revision = KineticFontStore.revision.intValue
    return remember(settings.kineticFontMix, settings.kineticFontCount, settings.kineticFonts, revision) {
        if (!settings.kineticFontMix) {
            listOf(null)
        } else {
            settings.kineticFonts
                .split(',')
                .filter { it.isNotBlank() }
                .mapNotNull { KineticFontStore.family(context, it.trim()) }
                .take(settings.kineticFontCount)
                .ifEmpty { listOf(null) }
        }
    }
}
