package me.spica27.spicamusic.ui.home.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import me.spica27.spicamusic.core.preferences.PreferencesManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** 新专辑样式调节项；ordinal 与 PreferencesManager 中的存储顺序一致。 */
enum class AlbumStyleSetting {
    ShadowOpacity,
    TextSize,
    Corner,
    TextPadding,
    Size,
    HighlightStyle,
    HighlightWidth,
    HighlightAlpha,
    HighlightAngle,
    HighlightFalloff,
}

/** 默认值等同于加入调节项之前的外观；宽度、衰减为十分之一单位。 */
@Immutable
data class AlbumStyleTuning(
    val shadowPercent: Int = 100,
    val textSizeSp: Int = 14,
    val cornerDp: Int = 20,
    val textPaddingDp: Int = 12,
    val sizePercent: Int = 100,
    /** 0 = 关闭，1 = 方向光，2 = 环境光，3 = 纯色。 */
    val highlightStyle: Int = 0,
    val highlightWidthTenths: Int = 12,
    val highlightAlphaPercent: Int = 50,
    val highlightAngle: Int = 45,
    val highlightFalloffTenths: Int = 20,
) {
    companion object {
        val DefaultValues: List<Int> = AlbumStyleTuning().toList()

        fun fromList(v: List<Int>): AlbumStyleTuning =
            AlbumStyleTuning(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9])
    }

    fun toList(): List<Int> =
        listOf(
            shadowPercent, textSizeSp, cornerDp, textPaddingDp, sizePercent, highlightStyle,
            highlightWidthTenths, highlightAlphaPercent, highlightAngle, highlightFalloffTenths,
        )
}

@Composable
fun rememberAlbumStyleTuning(preferencesManager: PreferencesManager): AlbumStyleTuning {
    val flow =
        remember(preferencesManager) {
            preferencesManager
                .getAlbumStyleTuning(AlbumStyleTuning.DefaultValues)
                .map(AlbumStyleTuning::fromList)
        }
    return flow.collectAsStateWithLifecycle(AlbumStyleTuning()).value
}

/** 在封面边缘画一圈高光（方向光 / 环境光 / 纯色），需放在 clip 之后。 */
fun Modifier.albumHighlight(
    tuning: AlbumStyleTuning,
    cornerDp: Int,
): Modifier {
    if (tuning.highlightStyle == 0 || tuning.highlightWidthTenths <= 0 || tuning.highlightAlphaPercent <= 0) return this
    return drawWithContent {
        drawContent()
        val strokeWidth = (tuning.highlightWidthTenths / 10f).dp.toPx()
        val alpha = (tuning.highlightAlphaPercent / 100f).coerceIn(0f, 1f)
        val strong = Color.White.copy(alpha = alpha)
        val clear = Color.White.copy(alpha = 0f)
        val half = strokeWidth / 2f
        val brush: Brush =
            when (tuning.highlightStyle) {
                1 -> {
                    val rad = Math.toRadians(tuning.highlightAngle.toDouble())
                    val dx = cos(rad).toFloat()
                    val dy = sin(rad).toFloat()
                    val reach = (abs(dx) * size.width + abs(dy) * size.height) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val stop = 0.2f + 0.8f * (tuning.highlightFalloffTenths / 40f).coerceIn(0f, 1f)
                    Brush.linearGradient(
                        0f to strong,
                        stop to clear,
                        1f to clear,
                        start = Offset(center.x - dx * reach, center.y - dy * reach),
                        end = Offset(center.x + dx * reach, center.y + dy * reach),
                    )
                }
                2 ->
                    Brush.linearGradient(
                        0f to strong,
                        0.5f to strong.copy(alpha = alpha * 0.25f),
                        1f to strong,
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    )
                else -> SolidColor(strong)
            }
        drawRoundRect(
            brush = brush,
            topLeft = Offset(half, half),
            size = Size(size.width - strokeWidth, size.height - strokeWidth),
            cornerRadius = CornerRadius((cornerDp.dp.toPx() - half).coerceAtLeast(0f)),
            style = Stroke(width = strokeWidth),
        )
    }
}
