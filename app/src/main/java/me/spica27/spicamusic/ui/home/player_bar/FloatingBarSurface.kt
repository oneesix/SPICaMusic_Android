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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
    val clipped = clip(glassShape)
    if (!glassEnabled || hazeState == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return clipped.background(containerColor)
    }

    val isDark = containerColor.luminance() < 0.5f
    // 质量 100 保持原来的 Balanced，低于 100 使用固定分辨率比例。
    val performanceMode =
        if (tuning.quality >= 100) HazePerformanceMode.Balanced else HazePerformanceMode.Fixed(tuning.quality / 100f)
    val tintAlpha = (tuning.tintPercent / 100f + if (isDark) 0.12f else 0f).coerceIn(0f, 1f)
    val rimAlpha = tuning.rimPercent / 100f * 0.8f
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

    // Android 12/12L 没有折射着色器，改用纯模糊，不再退回实色。
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        val blurStyle =
            remember(containerColor, tuning) {
                HazeBlurStyle {
                    blurRadius(tuning.blurDp.dp)
                    noiseFactor(0f)
                    backgroundColor(containerColor.copy(alpha = 0.12f))
                    colorEffects(listOf(HazeColorEffect.tint(containerColor.copy(alpha = tintAlpha))))
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
                tint(containerColor.copy(alpha = tintAlpha))
                specularIntensity(tuning.rimPercent / 100f)
                edgeShadow(Color.Black.copy(alpha = tuning.shadowPercent / 100f))
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
