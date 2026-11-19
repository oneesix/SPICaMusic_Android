package me.spica27.spicamusic.ui.widget

import android.net.Uri
import me.spica27.spicamusic.ui.motion.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/** Look of the Smooth colours. "auto" = the cover decides (pastel or deep), light / dark force one. */
object SmoothThemeMode {
    const val AUTO = "auto"
    const val LIGHT = "light"
    const val DARK = "dark"

    /** Same as auto, with stronger gradient colours and a more colourful accent (see "Text boost"). */
    const val VIVID = "vivid"
}

/**
 * Colours of Smooth: [fg] title / icons / buttons, [fg2] secondary text, [accent2] artist name (another hue
 * of the cover when it has one), [top] [mid] [bot] [glow] the gradient. [surface] is the page colour
 * and [deep] the card colour when the whole app is coloured.
 */
@Immutable
data class SmoothTheme(
    val fg: Color,
    val fg2: Color,
    val surface: Color,
    val deep: Color,
    val dark: Boolean,
    // The next five are optional so other themes (Blur Plus) can build a SmoothTheme with just the first five.
    val accent2: Color = fg,
    val top: Color = surface,
    val mid: Color = surface,
    val bot: Color = deep,
    val glow: Color = surface,
)

/** Artist-name colour (the second accent of the cover); null = the normal secondary text colour. */
val LocalSmoothArtistColor = compositionLocalOf<Color?> { null }

private fun clampF(
    v: Float,
    a: Float,
    b: Float,
) = min(b, max(a, v))

private fun jsRoundS(v: Float): Int = kotlin.math.floor(v + .5f).toInt()

private fun hsl2rgbS(
    h0: Float,
    s: Float,
    l: Float,
): IntArray {
    val h = h0 / 360f

    fun f(n: Int): Int {
        val k = (n + h * 12f) % 12f
        val a = s * min(l, 1f - l)
        val v = l - a * max(-1f, min(min(k - 3f, 9f - k), 1f))
        return jsRoundS(v * 255f)
    }
    return intArrayOf(f(0), f(8), f(4))
}

private fun lumS(c: IntArray): Float {
    fun f(v0: Int): Float {
        val v = v0 / 255f
        return if (v <= .03928f) v / 12.92f else ((v + .055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }
    return .2126f * f(c[0]) + .7152f * f(c[1]) + .0722f * f(c[2])
}

private fun contrastS(
    a: Float,
    b: Float,
) = (max(a, b) + .05f) / (min(a, b) + .05f)

private fun hdiffS(
    a: Float,
    b: Float,
): Float {
    val d = abs(a - b)
    return min(d, 360f - d)
}

private fun mixS(
    a: IntArray,
    b: IntArray,
    t: Float,
) = IntArray(3) { jsRoundS(a[it] + (b[it] - a[it]) * t) }

private fun colorS(c: IntArray) = Color(c[0].coerceIn(0, 255), c[1].coerceIn(0, 255), c[2].coerceIn(0, 255))

/** With "Color player UI" off the look simply follows the app theme (light or dark). */
internal fun smoothEngineMode(
    settings: SmoothSettings,
    appDark: Boolean,
): String =
    if (!settings.colorUi) {
        if (appDark) SmoothThemeMode.DARK else SmoothThemeMode.LIGHT
    } else {
        when (settings.themeMode) {
            SmoothThemeMode.LIGHT -> SmoothThemeMode.LIGHT
            SmoothThemeMode.DARK -> SmoothThemeMode.DARK
            else -> SmoothThemeMode.AUTO
        }
    }

private fun fallbackSmoothTheme(light: Boolean): SmoothTheme {
    val top = if (light) Color(0xFFF1D8CF) else Color(0xFF7D1408)
    val mid = if (light) Color(0xFFEBC8BC) else Color(0xFF8F1A09)
    val bot = if (light) Color(0xFFD9B2A6) else Color(0xFF4A0B04)
    val fg = if (light) Color(0xFF111111) else Color.White
    return SmoothTheme(fg, fg.copy(alpha = .78f), top, bot, !light, fg, top, mid, bot, mid)
}

/**
 * The Smooth colour engine: the cover palette gives a background hue (pastel or deep), a gradient, and the
 * text colour is the cover colour with the best contrast (at least 4.5:1) against that background.
 * "Text tint" 0 gives plain white / dark text.
 */
internal fun buildSmoothTheme(
    palette: CoverPalette?,
    settings: SmoothSettings,
    appDark: Boolean,
): SmoothTheme {
    val mode = smoothEngineMode(settings, appDark)
    val cl = palette?.clusters
    if (cl.isNullOrEmpty()) return fallbackSmoothTheme(mode == SmoothThemeMode.LIGHT)
    val vividLook = settings.colorUi && settings.themeMode == SmoothThemeMode.VIVID
    val satC = clampF(settings.saturationPercent / 100f, 0f, 2f)
    val v = if (vividLook) 0.6f else 0f
    val boost = if (vividLook) settings.textBoostPercent.coerceIn(0, 200) / 100f else 1f

    fun ss(x: Float) = clampF(x * satC, 0f, 1f)

    fun fitDark(
        h: Float,
        s: Float,
        l0: Float,
        maxL: Float,
    ): Float {
        var l = l0
        while (l > .04f && lumS(hsl2rgbS(h, s, l)) > maxL) l -= .01f
        return l
    }

    val vivid = cl.maxByOrNull { it.s * sqrt(it.w) }!!
    val base = cl[0]
    var h = base.h
    var s = base.s
    var l = base.l
    val dark = cl.firstOrNull { it.l < .14f && it.w > .42f }
    if (dark != null) {
        h = vivid.h
        s = .38f
        l = .085f
    } else {
        if (s > .1f) s = clampF(s * 1.1f, .3f, .92f)
        l = if (l > .66f) clampF(l, .72f, .86f) else clampF(l, .13f, .44f)
    }
    if (mode == SmoothThemeMode.LIGHT) {
        l = if (l > .66f) clampF(l, .72f, .86f) else .8f
    } else if (mode == SmoothThemeMode.DARK && dark == null) {
        l = clampF(l, .13f, .44f)
    }
    val s0 = s
    s = ss(s)
    l = clampF(l + v * .04f, .05f, .92f)
    if (mode == SmoothThemeMode.DARK) {
        // top of the gradient stays dark enough for light text
        l = max(.05f, fitDark(h, s, l + .05f, .13f) - .05f)
    }
    val bg = hsl2rgbS(h, s, l)
    val bgL = lumS(bg)
    val light =
        when (mode) {
            SmoothThemeMode.LIGHT -> true
            SmoothThemeMode.DARK -> false
            else -> bgL > .36f
        }

    class Cand(
        val h: Float,
        val s: Float,
        val l: Float,
        val base: Float,
    ) {
        var rgb: IntArray = IntArray(3)
        var ok: Boolean = false
    }

    val cands = ArrayList<Cand>()
    cl.forEach { k ->
        if (k === base && dark == null) return@forEach
        cands.add(
            Cand(
                k.h,
                k.s,
                k.l,
                (.2f + k.s) * (.6f + .4f * min(1f, hdiffS(k.h, h) / 70f)) * (.4f + sqrt(k.w) * 1.2f),
            ),
        )
    }
    cands.add(Cand(h, max(s0, .5f) * .9f, .5f, .42f))
    cands.forEach { c ->
        val cs2 = clampF(ss(if (c.s < .1f) c.s else clampF(c.s, .32f, .95f)) * boost, 0f, 1f)
        var ll = c.l
        for (i in 0 until 40) {
            if (contrastS(lumS(hsl2rgbS(c.h, cs2, ll)), bgL) >= 5f) break
            ll = clampF(ll + (if (light) -.03f else .03f), .05f, .95f)
        }
        c.rgb = hsl2rgbS(c.h, cs2, ll)
        c.ok = contrastS(lumS(c.rgb), bgL) >= 4.5f
    }
    val okCands = cands.filter { it.ok }.sortedByDescending { it.base }
    val accRgb = okCands.firstOrNull()?.rgb ?: if (light) intArrayOf(20, 20, 20) else intArrayOf(245, 245, 245)
    val acc2Rgb =
        okCands.firstOrNull()?.let { first -> okCands.firstOrNull { hdiffS(it.h, first.h) > 35f } }?.rgb ?: accRgb

    val second = cl.firstOrNull { it !== base && hdiffS(it.h, h) > 35f && it.s > .3f && it.w > .03f }
    val top = hsl2rgbS(h, s, clampF(l + (if (light) .03f else .05f), 0f, .92f))
    val mid =
        if (second != null) {
            mixS(
                bg,
                hsl2rgbS(second.h, ss(clampF(second.s, .3f, .9f)), clampF(second.l, .2f, if (light) .8f else .45f)),
                clampF(.38f + .2f * v, .15f, .6f),
            )
        } else {
            bg
        }
    val bot = hsl2rgbS(h, s, if (light) l * .9f else l * .55f)
    val glow =
        if (second != null) {
            mixS(bg, hsl2rgbS(second.h, ss(clampF(second.s, .4f, .95f)), .55f), clampF(.5f + .3f * v, .2f, .85f))
        } else {
            mixS(bg, accRgb, clampF(.16f + .14f * v, .05f, .4f))
        }

    // Text tint: how far the text moves from plain white / dark towards the cover colour.
    val neutral = if (light) intArrayOf(17, 17, 17) else intArrayOf(255, 255, 255)
    val tint = settings.textTintPercent.coerceIn(0, 100) / 100f
    val fg = colorS(mixS(neutral, accRgb, tint))
    return SmoothTheme(
        fg = fg,
        fg2 = fg.copy(alpha = .78f),
        surface = colorS(top),
        deep = colorS(bot),
        dark = !light,
        accent2 = colorS(mixS(neutral, acc2Rgb, tint)),
        top = colorS(top),
        mid = colorS(mid),
        bot = colorS(bot),
        glow = colorS(glow),
    )
}

/** The (slow) palette analysis runs once per cover. */
@Composable
fun rememberCoverPalette(coverUri: Uri?): CoverPalette? {
    val context = LocalContext.current
    var palette by remember(coverUri) { mutableStateOf<CoverPalette?>(null) }
    LaunchedEffect(coverUri) { palette = extractCoverPalette(context, coverUri) }
    return palette
}

/** Theme of one cover, fading smoothly between songs and when a slider moves. */
@Composable
fun rememberSmoothTheme(
    palette: CoverPalette?,
    settings: SmoothSettings,
    appDark: Boolean,
): SmoothTheme {
    val target =
        remember(
            palette,
            settings.saturationPercent,
            settings.textTintPercent,
            settings.textBoostPercent,
            settings.themeMode,
            settings.colorUi,
            appDark,
        ) {
            buildSmoothTheme(palette, settings, appDark)
        }
    val spec = tween<Color>(durationMillis = 450)
    val fg by animateColorAsState(target.fg, spec, label = "smooth_fg")
    val fg2 by animateColorAsState(target.fg2, spec, label = "smooth_fg2")
    val accent2 by animateColorAsState(target.accent2, spec, label = "smooth_accent2")
    val top by animateColorAsState(target.top, spec, label = "smooth_top")
    val mid by animateColorAsState(target.mid, spec, label = "smooth_mid")
    val bot by animateColorAsState(target.bot, spec, label = "smooth_bot")
    val glow by animateColorAsState(target.glow, spec, label = "smooth_glow")
    return SmoothTheme(fg, fg2, top, bot, target.dark, accent2, top, mid, bot, glow)
}

private fun smoothColorScheme(
    base: ColorScheme,
    theme: SmoothTheme,
    flat: Boolean = false,
): ColorScheme =
    base.copy(
        primary = theme.fg,
        onPrimary = theme.surface,
        primaryContainer = theme.fg.copy(alpha = .26f),
        onPrimaryContainer = theme.fg,
        secondary = theme.fg,
        onSecondary = theme.surface,
        secondaryContainer = theme.fg.copy(alpha = .26f),
        onSecondaryContainer = theme.fg,
        tertiary = theme.fg,
        onTertiary = theme.surface,
        surface = theme.surface,
        onSurface = theme.fg,
        onSurfaceVariant = theme.fg2,
        surfaceVariant = theme.fg.copy(alpha = .26f),
        surfaceTint = theme.fg,
        background = theme.surface,
        onBackground = theme.fg,
        outline = theme.fg2,
        outlineVariant = theme.fg.copy(alpha = .26f),
        // flat: solid controls, otherwise translucent (textured)
        surfaceContainerLowest = theme.deep.copy(alpha = if (flat) 1f else .5f),
        surfaceContainerLow = theme.deep.copy(alpha = if (flat) 1f else .6f),
        surfaceContainer = theme.deep.copy(alpha = if (flat) 1f else .78f),
        surfaceContainerHigh = theme.deep.copy(alpha = if (flat) 1f else .86f),
        // seek-bar track: fg at 26 %
        surfaceContainerHighest = theme.fg.copy(alpha = .26f),
        inverseSurface = theme.fg,
        inverseOnSurface = theme.surface,
    )

/**
 * Makes every Material text / icon / slider inside use the Smooth colours
 * (text = fg, secondary text = fg2, controls = fg on the gradient colour).
 */
@Composable
fun SmoothThemeScope(
    theme: SmoothTheme?,
    flat: Boolean = false,
    content: @Composable () -> Unit,
) {
    if (theme == null) {
        content()
        return
    }
    val base = MaterialTheme.colorScheme
    val scheme = remember(base, theme, flat) { smoothColorScheme(base, theme, flat) }
    CompositionLocalProvider(LocalSmoothArtistColor provides theme.accent2) {
        MaterialTheme(
            colorScheme = scheme,
            typography = MaterialTheme.typography,
            shapes = MaterialTheme.shapes,
            content = content,
        )
    }
}

/**
 * Colour style "Dynamic": the whole app (page, cards, text, icons, switches, mini player) takes the Smooth
 * colours of the cover that is playing. Without a cover the normal theme stays.
 *
 * The composition structure is the same whether the style is on or off. Only the colours change; if the
 * content moved, navigation pages would be registered twice and crash.
 */
@Composable
fun SmoothAppScope(
    enabled: Boolean,
    settings: SmoothSettings,
    artworkUri: Uri?,
    content: @Composable () -> Unit,
) {
    val palette = rememberCoverPalette(if (enabled) artworkUri else null)
    val base = MaterialTheme.colorScheme
    val appDark = base.surface.luminance() < 0.5f
    val theme = rememberSmoothTheme(palette, settings.copy(colorUi = true), appDark)
    val active = enabled && palette != null
    val scheme = remember(base, theme, active) { if (active) smoothColorScheme(base, theme) else base }
    // MaterialTheme resets the click feedback to ripples; keep the app's own.
    val indication = LocalIndication.current
    MaterialTheme(
        colorScheme = scheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
    ) {
        CompositionLocalProvider(
            LocalIndication provides indication,
            LocalSmoothArtistColor provides (if (active) theme.accent2 else null),
        ) {
            Box(modifier = Modifier.fillMaxSize()) { content() }
        }
    }
}
