package me.spica27.spicamusic.ui.widget

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin

/** Light / dark look of the Smooth player ("auto" follows the app theme). */
object SmoothThemeMode {
    const val AUTO = "auto"
    const val LIGHT = "light"
    const val DARK = "dark"

    /** Keeps the cover's own lightness and saturation; light or dark is picked per cover. */
    const val VIVID = "vivid"
}

/**
 * true = dark look. With "Color player UI" off the look simply follows the app theme, so the
 * background and the normal (white / dark) text always match.
 */
fun resolveSmoothDark(
    colorUi: Boolean,
    mode: String,
    appDark: Boolean,
): Boolean =
    if (!colorUi) {
        appDark
    } else {
        when (mode) {
            SmoothThemeMode.LIGHT -> false
            SmoothThemeMode.DARK -> true
            else -> appDark
        }
    }

/** Colours of the Smooth player UI: [fg] title / icons / buttons, [fg2] artist and secondary text. */
@Immutable
data class SmoothTheme(
    val fg: Color,
    val fg2: Color,
    /** Average colour of the gradient (used as the page surface for Material components). */
    val surface: Color,
    /** A slightly deeper version of [surface] for cards and sheets. */
    val deep: Color,
    val dark: Boolean,
)

private const val MIN_CONTRAST = 4.5f

/** Moves one album colour (LAB) into the light or dark look: same hue, lightness squeezed into a band. */
internal fun toneLab(
    lab: FloatArray,
    dark: Boolean,
): FloatArray {
    val chroma = hypot(lab[1], lab[2])
    val hue = atan2(lab[2], lab[1])
    val l = if (dark) 12f + lab[0] * 0.4f else 64f + lab[0] * 0.26f
    val c = chroma * if (dark) 0.85f else 0.75f
    return floatArrayOf(l, c * cos(hue), c * sin(hue))
}

/** Vivid look: keeps the cover's own lightness / saturation, only clamped into a wide light or dark band. */
internal fun toneLabVivid(
    lab: FloatArray,
    dark: Boolean,
): FloatArray {
    val chroma = hypot(lab[1], lab[2])
    val hue = atan2(lab[2], lab[1])
    val l = if (dark) lab[0].coerceIn(10f, 50f) else lab[0].coerceIn(70f, 90f)
    val c = chroma * if (dark) 1f else 0.95f
    return floatArrayOf(l, c * cos(hue), c * sin(hue))
}

/** LAB (D65) to sRGB 0..255, same maths as the shader. */
internal fun labToRgb(
    l: Float,
    a: Float,
    b: Float,
): FloatArray {
    val y = (l + 16f) / 116f
    val x = a / 500f + y
    val z = y - b / 200f

    fun inv(
        t: Float,
        f: Float,
    ): Float = if (t > 0.008856f) t else (f - 16f / 116f) / 7.787f

    val xx = inv(x * x * x, x) * 0.95047f
    val yy = inv(y * y * y, y)
    val zz = inv(z * z * z, z) * 1.08883f

    fun gamma(c0: Float): Float {
        val c = c0.coerceIn(0f, 1f)
        return 255f * (if (c <= 0.0031308f) 12.92f * c else 1.055f * c.pow(1f / 2.4f) - 0.055f)
    }
    return floatArrayOf(
        gamma(xx * 3.2406f + yy * -1.5372f + zz * -0.4986f),
        gamma(xx * -0.9689f + yy * 1.8758f + zz * 0.0415f),
        gamma(xx * 0.0557f + yy * -0.2040f + zz * 1.0570f),
    )
}

private fun channel(v: Float): Float {
    val c = v / 255f
    return if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
}

private fun relLuminance(c: FloatArray): Float = 0.2126f * channel(c[0]) + 0.7152f * channel(c[1]) + 0.0722f * channel(c[2])

private fun contrastRatio(
    a: FloatArray,
    b: FloatArray,
): Float {
    val x = relLuminance(a)
    val y = relLuminance(b)
    return (max(x, y) + 0.05f) / (min(x, y) + 0.05f)
}

private fun rgbColor(c: FloatArray) = Color(c[0].roundToInt().coerceIn(0, 255), c[1].roundToInt().coerceIn(0, 255), c[2].roundToInt().coerceIn(0, 255))

/**
 * Text colours for the light or dark look. They use the same LAB colours (saturation and dim included)
 * as the shader, take the hue of the most colourful gradient colour, and move in lightness until every
 * gradient colour gives a contrast of at least 4.5:1. "Text tint" 0 gives neutral text.
 */
internal fun buildSmoothTheme(
    palette: CoverPalette?,
    settings: SmoothSettings,
    darkIn: Boolean,
): SmoothTheme {
    val vivid = settings.themeMode == SmoothThemeMode.VIVID
    val pal = buildSmoothPalette(palette, settings.saturationPercent / 100f, darkIn, vivid)
    val dark = if (vivid) pal.dark else darkIn
    val keep = 1f - settings.dimPercent.coerceIn(0, 70) / 100f
    val bg = ArrayList<FloatArray>()
    val mean = FloatArray(3)
    var vividChroma = 0f
    var vividHue = 0f
    for (i in 0 until pal.count) {
        val l = pal.lab[i * 3]
        val a = pal.lab[i * 3 + 1]
        val b = pal.lab[i * 3 + 2]
        val rgb = labToRgb(l, a, b)
        val dimmed = floatArrayOf(rgb[0] * keep, rgb[1] * keep, rgb[2] * keep)
        bg.add(dimmed)
        for (k in 0..2) mean[k] += dimmed[k]
        val c = hypot(a, b)
        if (c > vividChroma) {
            vividChroma = c
            vividHue = atan2(b, a)
        }
    }
    for (k in 0..2) mean[k] /= bg.size.coerceAtLeast(1)
    val chromaMax = vividChroma
    val hue = vividHue
    val tint = settings.textTintPercent.coerceIn(0, 100) / 100f

    fun minContrast(c: FloatArray): Float = bg.minOf { contrastRatio(c, it) }

    fun tone(
        startL: Float,
        chromaMul: Float,
    ): FloatArray {
        val c = min(chromaMax * chromaMul * tint, 75f)
        var l = startL
        var rgb = labToRgb(l, c * cos(hue), c * sin(hue))
        for (i in 0 until 70) {
            rgb = labToRgb(l, c * cos(hue), c * sin(hue))
            if (minContrast(rgb) >= MIN_CONTRAST) break
            l = (l + if (dark) 1.5f else -1.5f).coerceIn(0f, 100f)
        }
        return rgb
    }

    val fg = tone(if (dark) 93f else 26f, if (dark) 0.28f else 0.9f)
    val fg2 = tone(if (dark) 86f else 40f, if (dark) 0.5f else 1.5f)
    val surface = rgbColor(mean)
    return SmoothTheme(
        fg = rgbColor(fg),
        fg2 = rgbColor(fg2),
        surface = surface,
        deep = lerp(surface, if (dark) Color.Black else Color.White, 0.2f),
        dark = dark,
    )
}

/**
 * Theme of the current cover, or null when [enabled] is false ("Color player UI" off).
 * The (slow) palette analysis runs once per cover; moving a slider only re-runs [buildSmoothTheme].
 */
@Composable
fun rememberSmoothTheme(
    coverUri: Uri?,
    settings: SmoothSettings,
    appDark: Boolean,
    enabled: Boolean,
): SmoothTheme? {
    val context = LocalContext.current
    var palette by remember(coverUri) { mutableStateOf<CoverPalette?>(null) }
    LaunchedEffect(coverUri) { palette = extractCoverPalette(context, coverUri) }
    val dark = resolveSmoothDark(settings.colorUi, settings.themeMode, appDark)
    val target =
        remember(palette, settings.saturationPercent, settings.dimPercent, settings.textTintPercent, settings.themeMode, dark) {
            buildSmoothTheme(palette, settings, dark)
        }
    // smooth cross-fade between songs / when sliders move
    val spec = tween<Color>(durationMillis = 450)
    val fg by animateColorAsState(target.fg, spec, label = "smooth_fg")
    val fg2 by animateColorAsState(target.fg2, spec, label = "smooth_fg2")
    val surface by animateColorAsState(target.surface, spec, label = "smooth_surface")
    val deep by animateColorAsState(target.deep, spec, label = "smooth_deep")
    return if (enabled) SmoothTheme(fg, fg2, surface, deep, target.dark) else null
}

/**
 * Makes every Material text / icon / slider inside use the Smooth colours
 * (text = fg, secondary text = fg2, controls = fg on the gradient colour).
 */
@Composable
fun SmoothThemeScope(
    theme: SmoothTheme?,
    content: @Composable () -> Unit,
) {
    if (theme == null) {
        content()
        return
    }
    val base = MaterialTheme.colorScheme
    val scheme =
        remember(base, theme) {
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
                surfaceContainerLowest = theme.deep.copy(alpha = .5f),
                surfaceContainerLow = theme.deep.copy(alpha = .6f),
                surfaceContainer = theme.deep.copy(alpha = .78f),
                surfaceContainerHigh = theme.deep.copy(alpha = .86f),
                // seek-bar track: fg at 26 %
                surfaceContainerHighest = theme.fg.copy(alpha = .26f),
                inverseSurface = theme.fg,
                inverseOnSurface = theme.surface,
            )
        }
    MaterialTheme(
        colorScheme = scheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
