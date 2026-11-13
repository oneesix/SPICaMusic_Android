package me.spica27.spicamusic.ui.home.player_bar

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.glass.GlassStyle
import dev.chrisbanes.haze.glass.RefractionProfile
import dev.chrisbanes.haze.glass.hazeGlass
import me.spica27.spicamusic.ui.glass.LocalLiquidGlassConfig
import me.spica27.spicamusic.ui.theme.Spacing

internal object FloatingBarDefaults {
    val HorizontalMargin = Spacing.Medium
    val BottomMargin = Spacing.Medium
    val Gap = Spacing.Small
    val CornerRadius = 36.dp
    val Shape = RoundedCornerShape(CornerRadius)
}

/** 底栏共享玻璃参数，形状及图层范围由布局决定，前景控件保持清晰。参数来自设置页的液态玻璃调节项。 */
@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun Modifier.playerBarSurface(
    hazeState: HazeState?,
    containerColor: Color,
    glassEnabled: Boolean,
    shape: RoundedCornerShape,
    expandLayerBounds: Boolean,
): Modifier {
    val tuning = LocalLiquidGlassConfig.current.tuning
    // 圆角设置只作用于悬浮布局的胶囊形状。
    val glassShape = if (shape === FloatingBarDefaults.Shape) RoundedCornerShape(tuning.cornerDp.dp) else shape
    if (!glassEnabled || hazeState == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return clip(glassShape).background(containerColor)
    }

    // 外部投影（演示页的 drop shadow），画在裁剪之前，所以不会被裁掉。
    val shadowAlpha = (tuning.shadowPercent / 100f * 1.5f).coerceIn(0f, 1f)
    val shadowed =
        if (shadowAlpha > 0f) {
            shadow(
                elevation = 12.dp,
                shape = glassShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = shadowAlpha),
                spotColor = Color.Black.copy(alpha = shadowAlpha),
            )
        } else {
            this
        }
    val clipped = shadowed.clip(glassShape)

    // 质量 100 使用自动模式，低于 100 使用固定分辨率比例。
    val performanceMode =
        if (tuning.quality >= 100) HazePerformanceMode.Balanced else HazePerformanceMode.Fixed(tuning.quality / 100f)
    val tintColor = containerColor.copy(alpha = (tuning.tintPercent / 100f).coerceIn(0f, 1f))
    val rimAlpha = (tuning.rimPercent / 100f).coerceIn(0f, 1f)
    val rim =
        if (tuning.rimWidthDp > 0 && rimAlpha > 0f) {
            Modifier.border(
                tuning.rimWidthDp.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = rimAlpha),
                        Color.White.copy(alpha = rimAlpha * 0.25f),
                        Color.White.copy(alpha = rimAlpha * 0.6f),
                    ),
                ),
                glassShape,
            )
        } else {
            Modifier
        }

    // 折射透镜需要 Android 13+；低版本自动使用磨砂模糊。
    if (tuning.isLens && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val style =
            remember(containerColor, glassShape, tuning) {
                GlassStyle.clear.then {
                    shape(glassShape)
                    optics(
                        refractionStrength = tuning.refractionPercent / 100f,
                        refractionHeightFraction = tuning.falloffPercent / 100f,
                        refractionDisplacement = 4.dp,
                        refractionProfile = RefractionProfile.Edge(tuning.refractionHeightDp.dp),
                        blurRadius = tuning.blurDp.dp,
                    )
                    backgroundColor(containerColor.copy(alpha = 0.12f))
                    tint(tintColor)
                    specularIntensity(rimAlpha)
                    edgeShadow(Color.Black.copy(alpha = 0.08f))
                    lightPosition(Alignment.TopStart)
                }
            }
        return clipped
            .hazeGlass(
                input = HazeInput.Sources(hazeState),
                style = style,
                performanceMode = performanceMode,
                expandLayerBounds = expandLayerBounds,
            ).then(rim)
    }

    // 磨砂模糊：模糊 + 色彩增强（饱和度）+ 色调，与演示页的 Frosted 模式一致。
    val blurStyle =
        remember(containerColor, tuning) {
            val boost = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(tuning.saturationPercent / 100f) })
            HazeBlurStyle {
                blurRadius(tuning.blurDp.dp)
                noiseFactor(0f)
                backgroundColor(containerColor.copy(alpha = 0.12f))
                colorEffects(
                    listOf(
                        HazeColorEffect.colorFilter(boost),
                        HazeColorEffect.tint(tintColor),
                    ),
                )
                fallbackColorEffect(HazeColorEffect.tint(containerColor.copy(alpha = 0.9f)))
                alpha(1f)
            }
        }
    return clipped
        .hazeBlur(
            input = HazeInput.Sources(hazeState),
            style = blurStyle,
            performanceMode = performanceMode,
            expandLayerBounds = false,
        ).then(rim)
}
