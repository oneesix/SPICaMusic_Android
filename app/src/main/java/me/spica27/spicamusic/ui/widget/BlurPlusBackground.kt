package me.spica27.spicamusic.ui.widget

import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.core.graphics.ColorUtils
import com.skydoves.landscapist.components.rememberImageComponent
import com.skydoves.landscapist.image.LandscapistImage
import me.spica27.spicamusic.utils.blurhash.BlurHashTransformationPlugin
import kotlin.math.pow

/** Light / dark look of the Blur Plus player ("auto" follows the app theme). */
object BlurPlusAppearance {
    const val AUTO = "auto"
    const val LIGHT = "light"
    const val DARK = "dark"
}

/** Look of the player controls with "Color player UI" on ("auto" follows the app's Color style). */
object BlurPlusColorStyle {
    const val AUTO = "auto"
    const val TEXTURED = "textured"
    const val FLAT = "flat"
}

/** Settings of the "Blur Plus" player background. Sliders are -100..100 or 0..100. */
@Immutable
data class BlurPlusSettings(
    /** 0..100: how soft the cover is. */
    val blur: Int = 60,
    /** -100..100: exposure (brighter / darker, multiplies the colours). */
    val exposure: Int = 0,
    /** -100..100: brightness (adds to the colours). */
    val brightness: Int = 0,
    /** -100..100: contrast. */
    val contrast: Int = 0,
    /** 0..100: fine film grain. */
    val grain: Int = 15,
    /** 0..100: coarse colour noise. */
    val noise: Int = 0,
    /** Player text / icons / sliders take their colours from the album art. */
    val colorUi: Boolean = true,
    val appearance: String = BlurPlusAppearance.AUTO,
    val colorStyle: String = BlurPlusColorStyle.AUTO,
)

/** true = dark look. With "Color player UI" off the look simply follows the app theme. */
fun resolveBlurPlusDark(
    settings: BlurPlusSettings,
    appDark: Boolean,
): Boolean =
    if (!settings.colorUi) {
        appDark
    } else {
        when (settings.appearance) {
            BlurPlusAppearance.LIGHT -> false
            BlurPlusAppearance.DARK -> true
            else -> appDark
        }
    }

/** true = flat (solid) controls. [appFlat] is the app's own Color style setting. */
fun resolveBlurPlusFlat(
    settings: BlurPlusSettings,
    appFlat: Boolean,
): Boolean =
    when (settings.colorStyle) {
        BlurPlusColorStyle.FLAT -> true
        BlurPlusColorStyle.TEXTURED -> false
        else -> appFlat
    }

/**
 * Colours of the player UI for "Color player UI": the hue of the cover, in a light or dark tone.
 * Textured keeps more colour and translucent controls, Flat keeps less colour and solid controls.
 */
fun buildBlurPlusTheme(
    cover: Color,
    dark: Boolean,
    flat: Boolean,
): SmoothTheme {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(cover.toArgb(), hsl)
    val sat = (hsl[1] * if (flat) 0.35f else 0.85f).coerceIn(0f, 0.9f)

    fun tone(
        l: Float,
        s: Float,
    ) = Color(ColorUtils.HSLToColor(floatArrayOf(hsl[0], s.coerceIn(0f, 1f), l)))
    val fg = tone(if (dark) 0.93f else 0.12f, sat * 0.9f)
    val fg2 = tone(if (dark) 0.80f else 0.26f, sat)
    val surface = tone(if (dark) 0.10f else 0.94f, sat * 0.6f)
    return SmoothTheme(
        fg = fg,
        fg2 = fg2,
        surface = surface,
        deep = lerp(surface, if (dark) Color.Black else Color.White, 0.2f),
        dark = dark,
    )
}

private fun noiseBitmap(
    size: Int,
    colored: Boolean,
    scale: Int,
): Bitmap {
    val px = IntArray(size * size)
    val rnd = java.util.Random(7L)
    for (i in px.indices) {
        px[i] =
            if (colored) {
                android.graphics.Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            } else {
                val v = rnd.nextInt(256)
                android.graphics.Color.argb(255, v, v, v)
            }
    }
    val bmp = Bitmap.createBitmap(px, size, size, Bitmap.Config.ARGB_8888)
    return if (scale > 1) Bitmap.createScaledBitmap(bmp, size * scale, size * scale, false) else bmp
}

/** Exposure (gain), contrast (around mid grey) and brightness (offset) as one colour matrix. */
private fun blurPlusColorMatrix(s: BlurPlusSettings): ColorMatrix {
    val gain = 2f.pow(s.exposure / 100f)
    val k = 1f + s.contrast / 100f
    val scale = gain * k
    val offset = 127.5f * (1f - k) + s.brightness / 100f * 0.4f * 255f
    return ColorMatrix(
        floatArrayOf(
            scale, 0f, 0f, 0f, offset,
            0f, scale, 0f, 0f, offset,
            0f, 0f, scale, 0f, offset,
            0f, 0f, 0f, 1f, 0f,
        ),
    )
}

/**
 * "Blur Plus": the blurred cover with blur, exposure, brightness, contrast, grain and noise controls.
 * Blur needs Android 12+ (RenderEffect); older versions use the same cheap blur as "Blurred Cover".
 */
@Composable
fun BlurPlusBackground(
    coverUri: Uri?,
    settings: BlurPlusSettings,
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val surface = MaterialTheme.colorScheme.surface
    val radiusPx = settings.blur.coerceIn(0, 100) * 0.8f * density.density
    val canBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val paint =
        remember(settings.exposure, settings.brightness, settings.contrast) {
            Paint().apply { colorFilter = ColorFilter.colorMatrix(blurPlusColorMatrix(settings)) }
        }
    val grainBrush = remember { ShaderBrush(ImageShader(noiseBitmap(256, false, 1).asImageBitmap(), TileMode.Repeated, TileMode.Repeated)) }
    val noiseBrush = remember { ShaderBrush(ImageShader(noiseBitmap(64, true, 3).asImageBitmap(), TileMode.Repeated, TileMode.Repeated)) }
    val grainAlpha = settings.grain.coerceIn(0, 100) / 100f * 0.6f
    val noiseAlpha = settings.noise.coerceIn(0, 100) / 100f * 0.6f

    val imageEffects =
        Modifier
            .graphicsLayer {
                if (canBlur && radiusPx > 0.5f) {
                    renderEffect = BlurEffect(radiusPx, radiusPx, TileMode.Clamp)
                }
            }.drawWithContent {
                drawIntoCanvas { canvas ->
                    canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
                    drawContent()
                    canvas.restore()
                }
            }

    Box(
        modifier =
            modifier
                .clipToBounds()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
    ) {
        Box(Modifier.matchParentSize().background(surface))
        if (canBlur) {
            LandscapistImage(
                modifier = Modifier.matchParentSize().then(imageEffects),
                imageModel = { coverUri },
                component =
                    rememberImageComponent {
                    },
            )
        } else {
            LandscapistImage(
                modifier = Modifier.matchParentSize().then(imageEffects),
                imageModel = { coverUri },
                component =
                    rememberImageComponent {
                        +BlurHashTransformationPlugin()
                    },
            )
        }
        // Keeps text readable whatever the sliders do.
        Box(
            Modifier
                .matchParentSize()
                .background(if (dark) Color.Black.copy(alpha = 0.38f) else Color.White.copy(alpha = 0.4f)),
        )
        Box(
            Modifier
                .matchParentSize()
                .drawBehind {
                    if (grainAlpha > 0f) drawRect(brush = grainBrush, alpha = grainAlpha, blendMode = BlendMode.Overlay)
                    if (noiseAlpha > 0f) drawRect(brush = noiseBrush, alpha = noiseAlpha, blendMode = BlendMode.Softlight)
                },
        )
    }
}
