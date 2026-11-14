package me.spica27.spicamusic.ui.widget

import android.net.Uri
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import me.spica27.spicamusic.core.preferences.LyricsStyleSettings
import me.spica27.spicamusic.utils.rememberDominantColorFromUri

/** Player colours taken from the album art (Color player UI). */
@Immutable
data class DynamicPlayerColors(
    /** Main colour: text, icons, buttons, progress bar, lyrics. */
    val fg: Color,
    /** Softer colour for secondary text and icons. */
    val fg2: Color,
    /** Colour of content drawn on top of [fg] (for example the play icon). */
    val onFg: Color,
    /** Optional tint for the player background (null = background not coloured). */
    val backgroundTint: Color?,
)

/** The colours for the current song, or null when Color player UI is off. */
@Composable
fun rememberDynamicPlayerColors(
    settings: LyricsStyleSettings,
    artworkUri: Uri?,
): DynamicPlayerColors? {
    val enabled = settings.dynamicLyricsEnabled
    val album = rememberDominantColorFromUri(uri = if (enabled) artworkUri else null, fallbackColor = MaterialTheme.colorScheme.primary)
    val appDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return remember(
        enabled,
        settings.dynamicBackgroundEnabled,
        settings.dynamicLyricsMode,
        settings.dynamicLyricsOpacity,
        settings.dynamicLyricsSaturation,
        settings.dynamicLyricsContrast,
        album,
        appDark,
    ) {
        if (!enabled) {
            null
        } else {
            val dark =
                when (settings.dynamicLyricsMode) {
                    "dark" -> true
                    "light" -> false
                    else -> appDark
                }
            val hsl = FloatArray(3)
            ColorUtils.colorToHSL(album.toArgb(), hsl)
            val saturation = (hsl[1] * settings.dynamicLyricsSaturation / 100f).coerceIn(0f, 1f)
            // Light tones on a dark page, dark tones on a light page; contrast moves the tone away from the page.
            val contrast = settings.dynamicLyricsContrast * 0.0035f
            val lightness = (if (dark) 0.60f + contrast else 0.45f - contrast).coerceIn(0f, 1f)
            val solid = Color(ColorUtils.HSLToColor(floatArrayOf(hsl[0], saturation, lightness)))
            val opacity = settings.dynamicLyricsOpacity / 100f
            val tint =
                if (settings.dynamicBackgroundEnabled) {
                    Color(ColorUtils.HSLToColor(floatArrayOf(hsl[0], saturation, 0.5f))).copy(alpha = 0.22f * opacity)
                } else {
                    null
                }
            DynamicPlayerColors(
                fg = solid.copy(alpha = opacity),
                fg2 = solid.copy(alpha = opacity * 0.7f),
                onFg = if (solid.luminance() > 0.5f) Color(0xFF17121F) else Color.White,
                backgroundTint = tint,
            )
        }
    }
}

/** The Material colour roles that Color player UI recolours (text, icons, buttons); surfaces stay as they are. */
internal fun ColorScheme.withDynamicColors(colors: DynamicPlayerColors): ColorScheme =
    copy(
        primary = colors.fg,
        onPrimary = colors.onFg,
        primaryContainer = colors.fg.copy(alpha = colors.fg.alpha * 0.26f),
        onPrimaryContainer = colors.fg,
        secondary = colors.fg,
        onSecondary = colors.onFg,
        secondaryContainer = colors.fg.copy(alpha = colors.fg.alpha * 0.26f),
        onSecondaryContainer = colors.fg,
        tertiary = colors.fg,
        onTertiary = colors.onFg,
        onSurface = colors.fg,
        onSurfaceVariant = colors.fg2,
        onBackground = colors.fg,
        outline = colors.fg2,
        outlineVariant = colors.fg.copy(alpha = colors.fg.alpha * 0.26f),
    )

/** Recolours the foreground roles of the Material theme (text, icons, buttons); surfaces stay as they are. */
@Composable
fun DynamicPlayerColorScope(
    colors: DynamicPlayerColors?,
    content: @Composable () -> Unit,
) {
    if (colors == null) {
        content()
        return
    }
    val base = MaterialTheme.colorScheme
    val scheme = remember(base, colors) { base.withDynamicColors(colors) }
    MaterialTheme(
        colorScheme = scheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
