package me.spica27.spicamusic.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RuntimeShader
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.util.lerp
import androidx.core.graphics.ColorUtils
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.spica27.spicamusic.common.entity.LyricItem
import me.spica27.spicamusic.core.preferences.KineticStyle
import me.spica27.spicamusic.core.preferences.LyricsStyleSettings
import me.spica27.spicamusic.ui.player.LocalPlayerViewModel
import java.text.BreakIterator
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random
import androidx.compose.ui.graphics.lerp as lerpColor

/**
 * Kinetic lyrics: the line being sung is shown big, word by word, in one of seven styles
 * (Stack, Slide, Zoom, Letters, Scatter, Graph, Flow) over a flowing "glass" background with
 * ripples. Ported from the Lyrical Studio web player. Used instead of the normal lyrics list
 * when the setting is on.
 */

// ==================== data ====================

private data class KWord(
    val text: String,
    val startMs: Long,
)

private data class KLine(
    val text: String,
    val words: List<KWord>,
    val startMs: Long,
    val endMs: Long,
    val repeats: Int,
)

private val KineticBase = Color(0xFF0B0A14)

private fun buildKineticLines(items: List<LyricItem>): List<KLine> {
    val out = ArrayList<KLine>()
    items.forEachIndexed { index, item ->
        when (item) {
            is LyricItem.WordsLyric -> {
                val words = mergeTimedWords(item.words)
                if (words.isNotEmpty()) {
                    out +=
                        KLine(
                            text = words.joinToString(" ") { it.text },
                            words = words,
                            startMs = item.startTime,
                            endMs = max(item.endTime, words.last().startMs + 300L),
                            repeats = 1,
                        )
                }
            }

            is LyricItem.NormalLyric -> {
                val tokens = item.content.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
                if (tokens.isNotEmpty()) {
                    val next = items.getOrNull(index + 1)?.time ?: (item.time + 4000L)
                    val duration = (next - item.time).coerceIn(1200L, 6000L)
                    val weights = tokens.map { it.length + 2 }
                    val sum = weights.sum().toFloat()
                    var acc = 0f
                    val words =
                        tokens.mapIndexed { i, t ->
                            val w = KWord(t, item.time + (duration * acc / sum).toLong())
                            acc += weights[i]
                            w
                        }
                    out += KLine(tokens.joinToString(" "), words, item.time, item.time + duration, 1)
                }
            }
        }
    }
    val counts = out.groupingBy { it.text.lowercase() }.eachCount()
    return out.map { it.copy(repeats = counts[it.text.lowercase()] ?: 1) }
}

/** Joins the pieces of one word (sym + phony); lines without spaces (CJK) keep one piece per word. */
private fun mergeTimedWords(words: List<LyricItem.WordsLyric.WordWithTiming>): List<KWord> {
    val spaced = words.any { it.endsWithSpace || it.content.any(Char::isWhitespace) }
    if (!spaced) {
        return words.filter { it.content.isNotBlank() }.map { KWord(it.content.trim(), it.startTime) }
    }
    val out = ArrayList<KWord>()
    val sb = StringBuilder()
    var start = 0L
    var open = false

    fun flush() {
        if (open && sb.isNotEmpty()) out += KWord(sb.toString(), start)
        sb.setLength(0)
        open = false
    }
    words.forEach { w ->
        val c = w.content
        if (c.isBlank()) {
            flush()
            return@forEach
        }
        if (c.first().isWhitespace()) flush()
        if (!open) {
            start = w.startTime
            open = true
        }
        sb.append(c.trim())
        if (w.endsWithSpace || c.last().isWhitespace()) flush()
    }
    flush()
    return out
}

/** Index of the last line that has started at [time] (-1 before the first line). */
private fun lastStartedIndex(
    lines: List<KLine>,
    time: Long,
): Int {
    var lo = 0
    var hi = lines.size - 1
    var res = -1
    while (lo <= hi) {
        val mid = (lo + hi) ushr 1
        if (lines[mid].startMs <= time + 50L) {
            res = mid
            lo = mid + 1
        } else {
            hi = mid - 1
        }
    }
    return res
}

// ==================== random, text fitting, style choice ====================

private class KRng(
    seed: Int,
) {
    private var a: Int = if (seed == 0) 1 else seed

    fun next(): Float {
        a += 0x6D2B79F5
        var t = a
        t = (t xor (t ushr 15)) * (t or 1)
        t = t xor (t + (t xor (t ushr 7)) * (t or 61))
        return (((t xor (t ushr 14)).toLong() and 0xFFFFFFFFL).toDouble() / 4294967296.0).toFloat()
    }
}

private fun KRng.pickWeighted(weighted: List<Pair<KineticStyle, Float>>): KineticStyle {
    var x = next() * weighted.sumOf { it.second.toDouble() }.toFloat()
    for ((v, k) in weighted) {
        x -= k
        if (x <= 0f) return v
    }
    return weighted.first().first
}

/** How likely each style is for a line, from its length, singing speed and chorus repeats. */
private fun kindWeights(line: KLine): List<Pair<KineticStyle, Float>> {
    val n = line.words.size
    val wps = n / max(0.5f, (line.endMs - line.startMs) / 1000f)
    val energetic = wps > 2.5f
    val calm = wps < 1.4f
    val chorus = line.repeats >= 2
    return when {
        n <= 2 -> listOf(KineticStyle.Zoom to 3f, KineticStyle.Letters to 2f, KineticStyle.Stack to 1f)
        n > 11 || wps > 3.8f -> listOf(KineticStyle.Flow to 3f, KineticStyle.Slide to 2f)
        chorus && n <= 7 ->
            if (energetic) {
                listOf(KineticStyle.Zoom to 2f, KineticStyle.Slide to 2f, KineticStyle.Letters to 1f)
            } else {
                listOf(KineticStyle.Stack to 2f, KineticStyle.Letters to 1f, KineticStyle.Zoom to 1f)
            }

        else ->
            listOf(
                KineticStyle.Stack to (if (calm) 3f else 2f),
                KineticStyle.Slide to (if (energetic) 3f else 2f),
                KineticStyle.Graph to (if (n <= 8) (if (calm) 2.5f else 1.5f) else 0f),
                KineticStyle.Scatter to (if (energetic) 2f else 1f),
                KineticStyle.Zoom to (if (energetic) 2f else 1f),
                KineticStyle.Letters to 1.5f,
                KineticStyle.Flow to (if (calm) 2f else 0.5f),
            )
    }
}

/** Picks the style of a line: Auto uses every style, otherwise only the chosen ones (still at random). */
private fun chooseKind(
    line: KLine,
    seed: Int,
    allowed: Set<KineticStyle>,
): KineticStyle {
    val r = KRng(seed)
    val weights = kindWeights(line)
    if (KineticStyle.Auto in allowed) return r.pickWeighted(weights.filter { it.second > 0f })
    return r.pickWeighted(allowed.map { k -> k to ((weights.firstOrNull { it.first == k }?.second ?: 0f) + 0.35f) })
}

private fun visualLength(s: String): Float {
    var n = 0f
    for (ch in s.uppercase()) {
        val code = ch.code
        n +=
            when {
                code in 0x3040..0x30FF || code in 0x4E00..0x9FFF || code in 0xAC00..0xD7AF -> 1f
                code in 0x0900..0x097F -> 0.62f
                ch == 'M' || ch == 'W' || ch == '@' -> 0.98f
                ch == 'I' || ch in ".,'!|:;" -> 0.3f
                ch == 'J' -> 0.56f
                ch == ' ' -> 0.26f
                ch.isDigit() -> 0.66f
                else -> 0.72f
            }
    }
    return max(n, 0.5f)
}

/** Font size in px for [rowCount] rows whose widest row is [longest] em wide (percent of the stage). */
private fun fitPx(
    longest: Float,
    rowCount: Int,
    c: KCtx,
    widthPct: Float = 82f,
    heightPct: Float = 62f,
    maxPct: Float = 16f,
): Float {
    val vw = min(maxPct, widthPct / max(0.5f, longest))
    val vh = heightPct / max(1, rowCount) / 1.05f
    return min(vw * c.w / 100f, vh * c.h / 100f)
}

private fun fitRows(
    c: KCtx,
    rows: List<List<Int>>,
    widthPct: Float,
    maxPct: Float,
    heightPct: Float = 62f,
): Float {
    val longest = rows.maxOf { row -> visualLength(row.joinToString(" ") { c.line.words[it].text }) }
    return fitPx(longest, rows.size, c, widthPct, heightPct, maxPct)
}

/** Groups word indices into at most [maxRows] rows of similar visual width. */
private fun groupRows(
    words: List<KWord>,
    maxRows: Int,
): List<List<Int>> {
    if (words.size <= maxRows) return words.indices.map { listOf(it) }
    val total = words.sumOf { (visualLength(it.text) + 0.3f).toDouble() }.toFloat()
    val target = total / maxRows
    val rows = mutableListOf(mutableListOf<Int>())
    var width = 0f
    words.forEachIndexed { i, w ->
        val len = visualLength(w.text) + 0.3f
        if (width > 0f && width + len / 2f > target && rows.size < maxRows) {
            rows.add(mutableListOf())
            width = 0f
        }
        rows.last().add(i)
        width += len
    }
    return rows
}

private fun cleanWord(s: String): String = s.lowercase().filter { it.isLetter() || it == '\'' }

/** The colour and font of every word of a line. */
private class KLook(
    val colors: List<Color>,
    val fonts: List<FontFamily?>,
    val accent: Color,
)

/**
 * A line uses two of the album colours (picked at random, stable for the line). Slot k of the font list belongs to
 * colour k, so a colour always comes with its own font.
 */
private fun buildLook(
    line: KLine,
    seed: Int,
    colors: List<Color>,
    fonts: List<FontFamily?>,
): KLook {
    val r = KRng(seed xor 0x7a3b21)
    val n = colors.size
    val a = (r.next() * n).toInt().coerceIn(0, n - 1)
    val b = (a + 1 + (r.next() * (n - 1)).toInt().coerceIn(0, max(0, n - 2))) % n
    val slots = IntArray(line.words.size)
    var current = if (r.next() < 0.5f) a else b
    for (i in slots.indices) {
        if (i > 0 && r.next() < 0.55f) current = if (current == a) b else a
        slots[i] = current
    }
    if (slots.size >= 2 && slots.all { it == slots[0] }) {
        val j = 1 + (r.next() * (slots.size - 1)).toInt().coerceIn(0, slots.size - 2)
        slots[j] = if (slots[0] == a) b else a
    }
    return KLook(slots.map { colors[it] }, slots.map { fonts[it % fonts.size] }, colors[a])
}

private fun splitLetters(word: String): List<String> {
    val it = BreakIterator.getCharacterInstance()
    it.setText(word)
    val out = ArrayList<String>()
    var s = it.first()
    var e = it.next()
    while (e != BreakIterator.DONE) {
        out += word.substring(s, e)
        s = e
        e = it.next()
    }
    return out.ifEmpty { listOf(word) }
}

// ==================== album colours ====================

@Immutable
private class KPalette(
    val text: List<Color>,
    val pools: List<Color>,
    val base: Color,
)

private fun colorDistance(
    a: Color,
    b: Color,
): Float {
    val dr = a.red - b.red
    val dg = a.green - b.green
    val db = a.blue - b.blue
    return sqrt(dr * dr + dg * dg + db * db)
}

private fun hslOf(c: Color): FloatArray {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(c.toArgb(), hsl)
    return hsl
}

private fun fromHsl(
    h: Float,
    s: Float,
    l: Float,
): Color = Color(ColorUtils.HSLToColor(floatArrayOf(h.mod(360f), s.coerceIn(0f, 1f), l.coerceIn(0f, 1f))))

/** Reads the main colours of the cover (vibrant ones first). Empty when there is no cover. */
private suspend fun extractSwatches(
    context: Context,
    uri: Uri?,
): List<Color> =
    withContext(Dispatchers.IO) {
        if (uri == null) return@withContext emptyList()
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= 128 && bounds.outHeight / (sample * 2) >= 128) sample *= 2
            val options =
                BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
            val bitmap =
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
                    ?: return@withContext emptyList()
            val palette = Palette.from(bitmap).maximumColorCount(24).generate()
            bitmap.recycle()
            listOfNotNull(
                palette.vibrantSwatch,
                palette.lightVibrantSwatch,
                palette.darkVibrantSwatch,
                palette.mutedSwatch,
                palette.lightMutedSwatch,
                palette.darkMutedSwatch,
                palette.dominantSwatch,
            ).map { Color(it.rgb) }
        } catch (e: Exception) {
            emptyList()
        }
    }

/**
 * Five text colours (light and vivid so they read on the dark glass), three colours for the background and a dark
 * tint of the cover colour as the background base. [textStrength] makes the text colours paler or more vivid.
 */
private fun buildPalette(
    source: List<Color>,
    textStrength: Int,
): KPalette {
    val distinct = ArrayList<Color>()
    for (c in source) {
        val opaque = c.copy(alpha = 1f)
        if (distinct.none { colorDistance(it, opaque) < 0.2f }) distinct += opaque
    }
    if (distinct.isEmpty()) distinct += Color(0xFF7C5CFF)
    var turn = 0
    while (distinct.size < 5) {
        val h = hslOf(distinct[turn % distinct.size])
        distinct += fromHsl(h[0] + 40f + 35f * turn, max(h[1], 0.6f), 0.5f)
        turn++
    }
    val k = textStrength / 100f
    val text =
        distinct.take(5).map { c ->
            val h = hslOf(c)
            fromHsl(h[0], max(h[1], 0.5f) * k, h[2].coerceIn(0.62f, 0.8f))
        }
    val pools =
        distinct.take(3).map { c ->
            val h = hslOf(c)
            fromHsl(h[0], max(h[1], 0.6f), 0.5f)
        }
    val h0 = hslOf(distinct[0])
    return KPalette(text, pools, fromHsl(h0[0], 0.5f, 0.13f))
}

@Composable
private fun rememberKineticPalette(
    uri: Uri?,
    textStrength: Int,
): KPalette {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme
    val fallback = remember(cs.primary, cs.tertiary, cs.secondary) { listOf(cs.primary, cs.tertiary, cs.secondary) }
    var swatches by remember(uri) { mutableStateOf<List<Color>>(emptyList()) }
    LaunchedEffect(uri) { swatches = extractSwatches(context, uri) }
    return remember(swatches, fallback, textStrength) { buildPalette(swatches.ifEmpty { fallback }, textStrength) }
}

// ==================== entry point ====================

private class RippleBuffer {
    val data = FloatArray(32)
    private var next = 0

    fun add(
        x: Float,
        y: Float,
        t: Float,
    ) {
        val i = (next++ % 8) * 4
        data[i] = x
        data[i + 1] = y
        data[i + 2] = t
        data[i + 3] = 1f
    }
}

private class BoxInfo {
    var pos = Offset.Zero
    var w = 1f
    var h = 1f
}

@Composable
internal fun KineticLyricsUI(
    modifier: Modifier,
    lines: List<LyricItem>,
    currentTimeProvider: () -> Long,
    seekPreviewTimeProvider: () -> Long?,
    settings: LyricsStyleSettings,
    displayMode: LyricsDisplayMode,
) {
    val kLines = remember(lines) { buildKineticLines(lines) }
    val salt = remember(kLines) { Random.nextInt() }
    val playerViewModel = LocalPlayerViewModel.current
    val mediaItem by playerViewModel.currentMediaItem.collectAsStateWithLifecycle()
    val palette = rememberKineticPalette(mediaItem?.mediaMetadata?.artworkUri, settings.kineticTextStrength)
    val fonts = rememberKineticFontSlots(settings)
    val clock = remember { mutableFloatStateOf(0f) }
    val timeMs = remember { mutableLongStateOf(0L) }
    val timeProvider by rememberUpdatedState(currentTimeProvider)
    val seekProvider by rememberUpdatedState(seekPreviewTimeProvider)
    LaunchedEffect(Unit) {
        var start = -1L
        while (true) {
            withFrameNanos { now ->
                if (start < 0L) start = now
                clock.floatValue = (now - start) / 1_000_000_000f
                timeMs.longValue = seekProvider() ?: timeProvider()
            }
        }
    }
    val ripples = remember { RippleBuffer() }
    val info = remember { BoxInfo() }
    val lineIndex by remember(kLines) { derivedStateOf { lastStartedIndex(kLines, timeMs.longValue) } }
    val topInset =
        if (displayMode == LyricsDisplayMode.Fullscreen) {
            WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 76.dp
        } else {
            12.dp
        }
    val bottomInset = if (displayMode == LyricsDisplayMode.Fullscreen) 132.dp else 12.dp

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .clipToBounds()
                .onGloballyPositioned {
                    info.pos = it.positionInRoot()
                    info.w = max(1f, it.size.width.toFloat())
                    info.h = max(1f, it.size.height.toFloat())
                },
    ) {
        // Ripple effect off: no glass background, the normal player background shows through.
        if (settings.kineticRipples) {
            KineticBackground(
                clock = clock,
                ripples = ripples,
                palette = palette,
                strength = settings.kineticBgStrength,
                quality = settings.kineticBgQuality,
                modifier = Modifier.fillMaxSize(),
            )
        }
        val index = lineIndex
        if (index >= 0 && index < kLines.size) {
            key(index) {
                KineticStage(
                    line = kLines[index],
                    salt = salt,
                    timeMs = timeMs,
                    settings = settings,
                    palette = palette,
                    fonts = fonts,
                    topInset = topInset,
                    bottomInset = bottomInset,
                    onReveal = { rootCenter ->
                        if (settings.kineticRipples) {
                            ripples.add(
                                (rootCenter.x - info.pos.x) / info.w,
                                (rootCenter.y - info.pos.y) / info.h,
                                clock.floatValue,
                            )
                        }
                    },
                )
            }
        }
    }
}

private class KCtx(
    val line: KLine,
    val revealed: Int,
    val seed: Int,
    val look: KLook,
    val w: Float,
    val h: Float,
    val speed: Float,
    val shadow: Boolean,
    val glow: Boolean,
    val blur: Boolean,
    val flip: Boolean,
    val letterDelay: Int,
    val onReveal: (Offset) -> Unit,
) {
    fun shown(i: Int): Boolean = i < revealed

    /** One word (or one letter of it) with its own colour and font. */
    @Composable
    fun Word(
        i: Int,
        fontPx: Float,
        from: Pose,
        stiffness: Float,
        text: String = line.words[i].text,
        modifier: Modifier = Modifier,
        delayMs: Int = 0,
        offscreen: Boolean = true,
        reveal: Boolean = true,
    ) {
        KWordText(
            text = text,
            shown = shown(i),
            from = from,
            spec = springSpec(stiffness * speed * speed),
            fontPx = fontPx,
            color = look.colors[i],
            fontFamily = look.fonts[i],
            shadow = shadow,
            glow = glow,
            modifier = modifier,
            delayMs = delayMs,
            offscreen = offscreen,
            onReveal = if (reveal) onReveal else null,
        )
    }
}

@Composable
private fun KineticStage(
    line: KLine,
    salt: Int,
    timeMs: State<Long>,
    settings: LyricsStyleSettings,
    palette: KPalette,
    fonts: List<FontFamily?>,
    topInset: Dp,
    bottomInset: Dp,
    onReveal: (Offset) -> Unit,
) {
    val seed = line.text.hashCode() xor salt
    val kind =
        remember(line, settings.kineticStyles, seed) {
            val styles = settings.kineticStyles
            if (styles.size == 1 && styles.first() != KineticStyle.Auto) styles.first() else chooseKind(line, seed, styles)
        }
    val look =
        remember(line, seed, palette, settings.kineticTextColors, fonts) {
            buildLook(line, seed, palette.text.take(settings.kineticTextColors), fonts)
        }
    val revealed by remember(line) { derivedStateOf { line.words.count { it.startMs <= timeMs.value } } }
    val density = LocalDensity.current
    val speed = settings.kineticSpeed / 100f

    BoxWithConstraints(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(top = topInset, bottom = bottomInset),
        contentAlignment = Alignment.Center,
    ) {
        val w = with(density) { maxWidth.toPx() }
        val h = with(density) { maxHeight.toPx() }
        val ctx =
            KCtx(
                line = line,
                revealed = revealed,
                seed = seed,
                look = look,
                w = w,
                h = h,
                speed = speed,
                shadow = settings.kineticShadow,
                glow = settings.kineticGlow,
                blur = settings.kineticBlur,
                flip = settings.kineticFlip3d,
                letterDelay = (settings.kineticLetterDelay / speed).toInt(),
                onReveal = onReveal,
            )
        val scale = settings.kineticSize / 100f
        val shift = (settings.kineticPosition - 50) / 50f * h * 0.35f
        Box(
            modifier =
                Modifier.graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationY = shift
                },
            contentAlignment = Alignment.Center,
        ) {
            when (kind) {
                KineticStyle.Stack -> StackTemplate(ctx)
                KineticStyle.Slide -> SlideTemplate(ctx)
                KineticStyle.Zoom -> ZoomTemplate(ctx)
                KineticStyle.Letters -> LettersTemplate(ctx)
                KineticStyle.Scatter -> ScatterTemplate(ctx)
                KineticStyle.Graph -> GraphTemplate(ctx)
                KineticStyle.Flow -> FlowTemplate(ctx)
                KineticStyle.Auto -> StackTemplate(ctx)
            }
        }
    }
}

// ==================== one animated word ====================

@Immutable
private data class Pose(
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotate: Float = 0f,
    val rotateX: Float = 0f,
    val alpha: Float = 1f,
    val blurPx: Float = 0f,
)

private val specCache = HashMap<Float, AnimationSpec<Float>>()

/** One spring per stiffness, so words do not get a new animation spec (and redraw) every time. */
private fun springSpec(stiffness: Float): AnimationSpec<Float> =
    specCache.getOrPut(stiffness) { spring<Float>(dampingRatio = 1f, stiffness = stiffness) }

/**
 * A word that glides from the hidden pose [from] to rest when [shown] becomes true. The word is drawn once into
 * a texture ([offscreen]) and only that texture is moved, so the shadow and glow are not redrawn every frame.
 */
@Composable
private fun KWordText(
    text: String,
    shown: Boolean,
    from: Pose,
    spec: AnimationSpec<Float>,
    fontPx: Float,
    color: Color,
    fontFamily: FontFamily?,
    shadow: Boolean,
    glow: Boolean,
    modifier: Modifier = Modifier,
    delayMs: Int = 0,
    offscreen: Boolean = true,
    onReveal: ((Offset) -> Unit)? = null,
) {
    val density = LocalDensity.current
    val progress = remember { Animatable(0f) }
    val holder = remember { arrayOfNulls<LayoutCoordinates>(1) }
    val reveal by rememberUpdatedState(onReveal)
    LaunchedEffect(shown) {
        if (shown) {
            if (delayMs > 0) delay(delayMs.toLong())
            holder[0]?.takeIf { it.isAttached }?.let { c ->
                reveal?.invoke(c.positionInRoot() + Offset(c.size.width / 2f, c.size.height / 2f))
            }
            progress.animateTo(1f, spec)
        } else {
            progress.snapTo(0f)
        }
    }
    val fontSp = with(density) { fontPx.toSp() }
    val textShadow =
        remember(color, fontPx, shadow, glow) {
            when {
                glow -> Shadow(color.copy(alpha = 0.55f), Offset.Zero, fontPx * 0.6f)
                shadow -> Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, fontPx * 0.035f), fontPx * 0.22f)
                else -> null
            }
        }
    Text(
        text = text.uppercase(),
        color = color,
        fontSize = fontSp,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.02f).em,
        lineHeight = fontSp * 1.05f,
        softWrap = false,
        maxLines = 1,
        style = TextStyle(shadow = textShadow),
        modifier =
            modifier
                .onGloballyPositioned { holder[0] = it }
                .graphicsLayer {
                    val p = progress.value
                    val h = 1f - p
                    alpha = lerp(from.alpha, 1f, (p * 2f).coerceIn(0f, 1f))
                    translationX = from.x * h * fontPx
                    translationY = from.y * h * fontPx
                    val s = lerp(from.scale, 1f, p)
                    scaleX = s * lerp(from.scaleX, 1f, p)
                    scaleY = s * lerp(from.scaleY, 1f, p)
                    rotationZ = from.rotate * h
                    rotationX = from.rotateX * h
                    cameraDistance = 12f * this.density
                    compositingStrategy = if (offscreen) CompositingStrategy.Offscreen else CompositingStrategy.Auto
                    renderEffect =
                        if (from.blurPx > 0f && h > 0.02f && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val r = from.blurPx * h
                            BlurEffect(r, r)
                        } else {
                            null
                        }
                },
    )
}

@Composable
private fun px2dp(px: Float): Dp = with(LocalDensity.current) { px.toDp() }

// ==================== the seven styles ====================

/** Words stack into big left- or right-aligned rows, each sliding up as it is sung. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StackTemplate(c: KCtx) {
    val r = KRng(c.seed)
    val left = r.next() < 0.5f
    val rows = groupRows(c.line.words, ceil(c.line.words.size / 2f).toInt().coerceIn(2, 5))
    val fontPx = fitRows(c, rows, 90f, 15f)
    val gap = px2dp(fontPx * 0.25f)
    val from = Pose(y = 0.7f, alpha = 0f, scaleY = 1.3f, scaleX = 0.9f)
    Column(
        modifier = Modifier.fillMaxWidth(0.92f),
        horizontalAlignment = if (left) Alignment.Start else Alignment.End,
    ) {
        rows.forEach { row ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(gap, if (left) Alignment.Start else Alignment.End),
            ) {
                row.forEach { i -> c.Word(i, fontPx, from, 150f) }
            }
        }
    }
}

/** Huge staggered rows slide in from the edges and run off the stage, cropped. */
@Composable
private fun SlideTemplate(c: KCtx) {
    val r = KRng(c.seed + 5)
    val rows = groupRows(c.line.words, ceil(c.line.words.size / 2f).toInt().coerceIn(1, 3))
    val fontPx = fitRows(c, rows, 88f, 17f, 70f)
    val indentStep = 3f + r.next() * 4f
    val reverse = r.next() < 0.5f
    val gap = px2dp(fontPx * 0.24f)
    Column(modifier = Modifier.fillMaxWidth(0.94f)) {
        rows.forEachIndexed { ri, row ->
            val fromLeft = (ri % 2 == 0) != reverse
            val indent = (if (reverse) rows.size - 1 - ri else ri) * indentStep
            val from =
                if (fromLeft) {
                    Pose(x = -1.1f, alpha = 0f, scaleX = 1.2f, scaleY = 0.85f)
                } else {
                    Pose(x = 5f, alpha = 0f, scaleX = 1.45f, scaleY = 0.85f)
                }
            Row(
                modifier =
                    Modifier
                        .wrapContentWidth(Alignment.Start, unbounded = true)
                        .padding(start = px2dp(indent * c.w / 100f)),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                row.forEach { i -> c.Word(i, fontPx, from, 120f) }
            }
        }
    }
}

/** Words fly in huge (and blurred when allowed), settling into centred rows. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ZoomTemplate(c: KCtx) {
    val rows = groupRows(c.line.words, ceil(c.line.words.size / 2f).toInt().coerceIn(1, 4))
    val fontPx = fitRows(c, rows, 90f, 17f)
    val gap = px2dp(fontPx * 0.25f)
    val from =
        Pose(
            scale = if (c.blur) 2.6f else 1.8f,
            alpha = 0f,
            scaleY = 0.75f,
            blurPx = if (c.blur) fontPx * 0.25f else 0f,
        )
    Column(modifier = Modifier.fillMaxWidth(0.94f), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            FlowRow(horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)) {
                row.forEach { i -> c.Word(i, fontPx, from, 80f) }
            }
        }
    }
}

/** Each letter flips in on its own, staggered, like a split-flap board. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LettersTemplate(c: KCtx) {
    val r = KRng(c.seed + 9)
    val fromTop = r.next() < 0.5f
    val rows = groupRows(c.line.words, ceil(c.line.words.size / 2f).toInt().coerceIn(1, 4))
    val fontPx = fitRows(c, rows, 90f, 16f)
    val gap = px2dp(fontPx * 0.25f)
    val from =
        Pose(
            rotateX = if (c.flip) (if (fromTop) 90f else -90f) else 0f,
            y = if (fromTop) -0.4f else 0.4f,
            alpha = 0f,
        )
    Column(modifier = Modifier.fillMaxWidth(0.94f), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            FlowRow(horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally)) {
                row.forEach { i ->
                    val letters = remember(c.line.words[i].text) { splitLetters(c.line.words[i].text) }
                    Row {
                        letters.forEachIndexed { j, ch ->
                            c.Word(
                                i = i,
                                fontPx = fontPx,
                                from = from,
                                stiffness = 160f,
                                text = ch,
                                delayMs = j * c.letterDelay,
                                offscreen = false,
                                reveal = j == 0,
                            )
                        }
                    }
                }
            }
        }
    }
}

/** A collage: words in different sizes and offsets. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScatterTemplate(c: KCtx) {
    val r = KRng(c.seed)
    val total = c.line.words.sumOf { visualLength(it.text).toDouble() }.toFloat()
    val base = fitPx(total / 3f + 1.5f, 3, c, 76f, 54f, 12f)
    val gap = px2dp(base * 0.35f)
    val layout =
        remember(c.line, c.seed) {
            c.line.words.map {
                val scale = 0.65f + r.next() * 0.6f
                val offset = (r.next() - 0.5f) * 0.5f
                val fx = (r.next() - 0.5f) * 3f
                val fy = (r.next() - 0.5f) * 2f
                Triple(scale, offset, Pose(x = fx, y = fy, alpha = 0f, scale = 0.7f, rotate = fy * 8f))
            }
        }
    FlowRow(
        modifier = Modifier.fillMaxWidth(0.9f),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(px2dp(base * 0.1f), Alignment.CenterVertically),
    ) {
        c.line.words.indices.forEach { i ->
            val (scale, offset, from) = layout[i]
            c.Word(
                i = i,
                fontPx = base * scale,
                from = from,
                stiffness = 150f,
                modifier = Modifier.offset(y = px2dp(base * offset)),
            )
        }
    }
}

/** The whole line is visible but dim; words light up as they are sung. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowTemplate(c: KCtx) {
    val total = visualLength(c.line.text)
    val rowCount = ceil(total / 14f).toInt().coerceIn(1, 4)
    val fontPx = fitPx(total / rowCount + 1.5f, rowCount + 1, c, 84f, min(66f, 24f * rowCount), 11f)
    val gap = px2dp(fontPx * 0.26f)
    val density = LocalDensity.current
    val fontSp = with(density) { fontPx.toSp() }
    val fade = (300 / c.speed).toInt()
    FlowRow(
        modifier = Modifier.fillMaxWidth(0.92f),
        horizontalArrangement = Arrangement.spacedBy(gap, Alignment.CenterHorizontally),
    ) {
        c.line.words.forEachIndexed { i, w ->
            val sung = c.shown(i)
            val active = i == c.revealed - 1
            val a by animateFloatAsState(if (sung) 1f else 0.32f, tween(fade), label = "flowAlpha")
            val s by animateFloatAsState(if (active) 1.03f else 1f, tween(fade), label = "flowScale")
            val holder = remember { arrayOfNulls<LayoutCoordinates>(1) }
            LaunchedEffect(sung) {
                if (sung) {
                    holder[0]?.takeIf { it.isAttached }?.let { cc ->
                        c.onReveal(cc.positionInRoot() + Offset(cc.size.width / 2f, cc.size.height / 2f))
                    }
                }
            }
            Text(
                text = w.text.uppercase(),
                color = c.look.colors[i],
                fontSize = fontSp,
                fontFamily = c.look.fonts[i],
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.02f).em,
                lineHeight = fontSp * 1.12f,
                style =
                    TextStyle(
                        shadow =
                            if (c.shadow) {
                                Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, fontPx * 0.035f), fontPx * 0.22f)
                            } else {
                                null
                            },
                    ),
                modifier =
                    Modifier
                        .onGloballyPositioned { holder[0] = it }
                        .graphicsLayer {
                            alpha = a
                            scaleX = s
                            scaleY = s
                        },
            )
        }
    }
}

// ---- Graph: words joined by curves, small words hang off big ones ----

private val SmallWords =
    setOf(
        "a", "an", "the", "to", "of", "in", "on", "at", "by", "for", "from", "with", "into", "my", "your", "his", "her", "our", "their",
        "and", "or", "but", "so", "as", "if", "is", "am", "are", "was", "be", "i", "it", "me", "we", "that", "this", "just", "all", "up", "out",
    )

private class GNode(
    val x: Float,
    val y: Float,
    val font: Float,
    val leaf: Boolean,
    val parent: Int,
    val row: Int,
)

private class GSeg(
    val a: Offset,
    val b: Offset,
    val dest: Int,
    val horizontal: Boolean,
)

private fun layoutGraph(
    words: List<String>,
    seed: Int,
    w: Float,
    h: Float,
): List<GNode> {
    val tf = min(h * 0.1f, w * 0.115f)
    val lf = tf * 0.42f
    val rowW = w * 0.84f
    var leaf = words.mapIndexed { i, t -> i < words.size - 1 && cleanWord(t) in SmallWords }
    if (leaf.none { !it }) leaf = words.map { false }
    val parent =
        words.indices.map { i ->
            if (!leaf[i]) {
                i
            } else {
                (i + 1 until words.size).firstOrNull { !leaf[it] } ?: i
            }
        }
    val phase = (seed.mod(628)) / 100f
    val amp = tf * 0.55f
    val xs = FloatArray(words.size)
    val ys = FloatArray(words.size)
    val rowOf = IntArray(words.size)
    var cx = 0f
    var row = 0
    words.forEachIndexed { i, t ->
        if (leaf[i]) return@forEachIndexed
        val wd = visualLength(t) * tf
        if (cx > 0f && cx + wd > rowW) {
            row++
            cx = 0f
        }
        xs[i] = cx + wd / 2f
        ys[i] = row * tf * 3.4f + sin(i * 1.1f + phase) * amp
        rowOf[i] = row
        cx += wd + tf * 0.9f
    }
    val rows = row + 1
    for (r in 0 until rows) {
        val idx = words.indices.filter { !leaf[it] && rowOf[it] == r }
        if (idx.isEmpty()) continue
        val left = xs[idx.first()] - visualLength(words[idx.first()]) * tf / 2f
        val right = xs[idx.last()] + visualLength(words[idx.last()]) * tf / 2f
        val shift = (w - (right - left)) / 2f - left
        idx.forEach { xs[it] += shift }
    }
    val oy = (h - rows * tf * 3.4f) / 2f + tf
    words.indices.forEach { if (!leaf[it]) ys[it] += oy }
    words.indices.forEach { i ->
        if (!leaf[i]) return@forEach
        val p = parent[i]
        val sib = words.indices.filter { leaf[it] && parent[it] == p }
        val k = sib.indexOf(i)
        xs[i] = xs[p] - (sib.size - 1 - k) * lf * visualLength(words[i]) * 0.9f - tf * 0.5f
        ys[i] = ys[p] + tf * 0.95f + k * lf * 0.2f
        rowOf[i] = -1
    }
    return words.indices.map { GNode(xs[it], ys[it], if (leaf[it]) lf else tf, leaf[it], parent[it], rowOf[it]) }
}

private fun graphSegments(
    words: List<String>,
    nodes: List<GNode>,
): List<GSeg> {
    val out = ArrayList<GSeg>()
    var prev = -1
    nodes.forEachIndexed { i, n ->
        if (!n.leaf) {
            if (prev >= 0) {
                val p = nodes[prev]
                val same = p.row == n.row
                val a = if (same) Offset(p.x + visualLength(words[prev]) * p.font / 2f + 4f, p.y) else Offset(p.x, p.y + p.font * 0.45f)
                val b = if (same) Offset(n.x - visualLength(words[i]) * n.font / 2f - 4f, n.y) else Offset(n.x, n.y - n.font * 0.45f)
                out += GSeg(a, b, i, same)
            }
            prev = i
        } else {
            val q = nodes[n.parent]
            out += GSeg(Offset(n.x, n.y - n.font * 0.5f), Offset(q.x, q.y + q.font * 0.45f), i, false)
        }
    }
    return out
}

@Composable
private fun GraphTemplate(c: KCtx) {
    val words = remember(c.line) { c.line.words.map { it.text } }
    val nodes = remember(c.line, c.seed, c.w, c.h) { layoutGraph(words, c.seed, c.w, c.h) }
    val segs = remember(nodes) { graphSegments(words, nodes) }
    val progress = remember(segs) { segs.map { Animatable(0f) } }
    val drawMs = (800 / c.speed).toInt()
    LaunchedEffect(c.revealed, segs) {
        segs.forEachIndexed { i, s ->
            launch { progress[i].animateTo(if (s.dest < c.revealed) 1f else 0f, tween(drawMs)) }
        }
    }
    val strokeWidth = max(2f, nodes.firstOrNull { !it.leaf }?.font?.times(0.05f) ?: 2f)
    val from = Pose(y = 0.5f, alpha = 0f, scale = 0.8f)
    val lineColor = c.look.accent.copy(alpha = 0.9f)
    Layout(
        content = {
            words.indices.forEach { i -> c.Word(i, nodes[i].font, from, 150f) }
        },
        modifier =
            Modifier
                .size(px2dp(c.w), px2dp(c.h))
                .drawBehind {
                    val path = Path()
                    val part = Path()
                    val measure = PathMeasure()
                    segs.forEachIndexed { i, s ->
                        val p = progress[i].value.coerceIn(0f, 1f)
                        if (p <= 0.001f) return@forEachIndexed
                        path.reset()
                        part.reset()
                        path.moveTo(s.a.x, s.a.y)
                        if (s.horizontal) {
                            val mx = (s.a.x + s.b.x) / 2f
                            path.cubicTo(mx, s.a.y, mx, s.b.y, s.b.x, s.b.y)
                        } else {
                            val my = (s.a.y + s.b.y) / 2f
                            path.cubicTo(s.a.x, my, s.b.x, my, s.b.x, s.b.y)
                        }
                        measure.setPath(path, false)
                        measure.getSegment(0f, measure.length * p, part, true)
                        drawPath(part, lineColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
                    }
                },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(Constraints()) }
        layout(constraints.maxWidth, constraints.maxHeight) {
            placeables.forEachIndexed { i, p ->
                val n = nodes[i]
                p.place((n.x - p.width / 2f).roundToInt(), (n.y - p.height / 2f).roundToInt())
            }
        }
    }
}

// ==================== background ====================

/** Draws the content at [quality] of the screen resolution and scales it up (much less work on high-resolution screens). */
private fun Modifier.lowRes(quality: Float): Modifier =
    if (quality >= 0.99f) {
        this
    } else {
        layout { measurable, constraints ->
            val q = quality.coerceIn(0.2f, 1f)
            val w = max(2, (constraints.maxWidth * q).roundToInt())
            val h = max(2, (constraints.maxHeight * q).roundToInt())
            val placeable = measurable.measure(Constraints.fixed(w, h))
            layout(constraints.maxWidth, constraints.maxHeight) {
                placeable.placeWithLayer(0, 0) {
                    scaleX = 1f / q
                    scaleY = 1f / q
                    transformOrigin = TransformOrigin(0f, 0f)
                    compositingStrategy = CompositingStrategy.Offscreen
                }
            }
        }
    }

@Composable
private fun KineticBackground(
    clock: State<Float>,
    ripples: RippleBuffer,
    palette: KPalette,
    strength: Int,
    quality: Int,
    modifier: Modifier,
) {
    val shader =
        remember {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                runCatching { RuntimeShader(KINETIC_AGSL) }.getOrNull()
            } else {
                null
            }
        }
    val lowRes = modifier.lowRes(quality / 100f)
    if (shader != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        AgslBackground(shader, clock, ripples, palette, strength / 100f, lowRes)
    } else {
        FallbackBackground(clock, ripples, palette, strength / 100f, lowRes)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun AgslBackground(
    shader: RuntimeShader,
    clock: State<Float>,
    ripples: RippleBuffer,
    palette: KPalette,
    k: Float,
    modifier: Modifier,
) {
    val brush = remember(shader) { ShaderBrush(shader) }
    Canvas(modifier) {
        shader.setFloatUniform("uRes", size.width, size.height)
        shader.setFloatUniform("uTime", clock.value)
        palette.pools.forEachIndexed { i, c -> shader.setFloatUniform("uC$i", c.red * k, c.green * k, c.blue * k) }
        val bk = 0.5f + 0.5f * k
        shader.setFloatUniform("uBase", palette.base.red * bk, palette.base.green * bk, palette.base.blue * bk)
        shader.setFloatUniform("uRip", ripples.data)
        drawRect(brush)
    }
}

/** Android 12 and older: soft moving colour pools and simple expanding rings (no glass shader). */
@Composable
private fun FallbackBackground(
    clock: State<Float>,
    ripples: RippleBuffer,
    palette: KPalette,
    k: Float,
    modifier: Modifier,
) {
    Canvas(modifier) {
        val t = clock.value
        drawRect(palette.base)
        val spots =
            listOf(
                Offset(0.25f + 0.08f * sin(t * 0.17f), 0.3f + 0.06f * sin(t * 0.13f + 1f)),
                Offset(0.75f + 0.08f * sin(t * 0.11f + 2f), 0.7f + 0.06f * sin(t * 0.19f)),
                Offset(0.55f + 0.1f * sin(t * 0.09f + 4f), 0.2f + 0.08f * sin(t * 0.15f + 3f)),
            )
        spots.forEachIndexed { i, s ->
            val center = Offset(s.x * size.width, s.y * size.height)
            val radius = max(size.width, size.height) * 0.6f
            drawCircle(
                brush = Brush.radialGradient(listOf(palette.pools[i].copy(alpha = (0.4f * k).coerceIn(0f, 1f)), Color.Transparent), center, radius),
                radius = radius,
                center = center,
            )
        }
        for (i in 0 until 8) {
            val age = t - ripples.data[i * 4 + 2]
            if (ripples.data[i * 4 + 3] <= 0f || age < 0f || age > 3.5f) continue
            drawCircle(
                color = Color.White.copy(alpha = 0.14f * (1f - age / 3.5f)),
                radius = age * 0.32f * size.height,
                center = Offset(ripples.data[i * 4] * size.width, ripples.data[i * 4 + 1] * size.height),
                style = Stroke(width = 3f),
            )
        }
    }
}

/** The glass background of Lyrical Studio (WebGL) ported to AGSL: flowing light, refraction, ripples. */
private const val KINETIC_AGSL = """
uniform float2 uRes;
uniform float uTime;
uniform float3 uC0;
uniform float3 uC1;
uniform float3 uC2;
uniform float3 uBase;
uniform float4 uRip[8];

float hash(float2 p) {
  p = fract(p * float2(123.34, 456.21));
  p += dot(p, p + 45.32);
  return fract(p.x * p.y);
}
float noise(float2 p) {
  float2 i = floor(p);
  float2 f = fract(p);
  float2 u = f * f * (3.0 - 2.0 * f);
  return mix(mix(hash(i), hash(i + float2(1.0, 0.0)), u.x),
             mix(hash(i + float2(0.0, 1.0)), hash(i + float2(1.0, 1.0)), u.x), u.y);
}
float fbm(float2 p) {
  float v = 0.0;
  float a = 0.55;
  for (int i = 0; i < 3; i++) {
    v += a * noise(p);
    p = p * 1.9 + float2(1.7, 9.2);
    a *= 0.45;
  }
  return v / 0.88;
}
float3 lightField(float2 p, float t) {
  float2 s = p * 0.62;
  float2 q = float2(fbm(s + float2(0.0, t * 0.035)), fbm(s + float2(5.2, -t * 0.03)));
  float2 r = float2(fbm(s + 1.6 * q + float2(1.7, 9.2) + t * 0.022), fbm(s + 1.6 * q + float2(8.3, 2.8) - t * 0.026));
  float f = fbm(s * 0.9 + 1.4 * r);
  float w0 = smoothstep(0.42, 0.82, f);
  float w1 = smoothstep(0.5, 0.88, r.x) * (1.0 - w0 * 0.6);
  float w2 = smoothstep(0.55, 0.92, q.y) * (1.0 - max(w0, w1) * 0.6);
  float3 col = uBase * 0.7 + uC0 * w0 * 0.95 + uC1 * w1 * 0.8 + uC2 * w2 * 0.65;
  float lum = dot(col, float3(0.299, 0.587, 0.114));
  return max(mix(float3(lum), col, 1.45), 0.0);
}

half4 main(float2 fragCoord) {
  float2 uv = float2(fragCoord.x / uRes.x, 1.0 - fragCoord.y / uRes.y);
  float aspect = uRes.x / uRes.y;
  float2 p = float2((uv.x - 0.5) * aspect, uv.y - 0.5);
  float t = uTime;

  float2 slope = float2(
    sin(p.y * 2.3 + t * 0.21) + 0.6 * sin(p.x * 1.7 - p.y * 1.3 + t * 0.17),
    cos(p.x * 2.1 - t * 0.19) + 0.6 * cos(p.y * 1.9 + p.x * 0.8 - t * 0.23)) * 0.012;

  float ring = 0.0;
  for (int i = 0; i < 8; i++) {
    float4 rp = uRip[i];
    float age = t - rp.z;
    if (rp.w <= 0.0 || age < 0.0 || age > 3.5) continue;
    float2 c = float2((rp.x - 0.5) * aspect, (1.0 - rp.y) - 0.5);
    float2 d = p - c;
    float dist = length(d);
    float x = dist - age * 0.32;
    float env = exp(-x * x * 90.0) * exp(-age * 1.25) * rp.w;
    slope += (d / max(dist, 0.001)) * sin(x * 55.0) * env * 0.035;
    ring += env;
  }

  float3 col = lightField(p + slope * 6.0, t);
  col += float3(slope.x, 0.3 * (slope.y - slope.x), -slope.y) * 1.6;

  float3 n = normalize(float3(-slope * 14.0, 1.0));
  float3 L = normalize(float3(-0.45, 0.55, 0.7));
  float spec = pow(max(dot(reflect(-L, n), float3(0.0, 0.0, 1.0)), 0.0), 40.0);
  col += spec * 0.2 + ring * 0.05;

  col *= mix(0.4, 1.0, smoothstep(1.2, 0.3, length(p * float2(0.85, 1.1))));
  col = col / (1.0 + col * 0.7);
  col = pow(col, float3(1.18)) * 0.95;
  col += (hash(fragCoord + fract(t) * 100.0) - 0.5) * 0.018;
  return half4(half3(col), 1.0);
}
"""
