package me.spica27.spicamusic.ui.glass

import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import kotlinx.coroutines.flow.map
import me.spica27.spicamusic.core.preferences.PreferencesManager
import kotlin.math.roundToInt

/** 液态玻璃 V2（Kyant Backdrop）调节项；ordinal 与 PreferencesManager 中的存储顺序一致。 */
enum class LiquidGlassV2Setting {
    Corner,
    Blur,
    RefractionHeight,
    RefractionAmount,
    Depth,
    Chromatic,
    Vibrancy,
    TintOpacity,
    TintColor,
    HighlightStyle,
    HighlightWidth,
    HighlightAlpha,
    HighlightAngle,
    HighlightFalloff,
    ShadowRadius,
    ShadowAlpha,
    InnerRadius,
    InnerAlpha,
    Press,
}

/** 默认值与演示页一致。开关类为 0/1；宽度、衰减为十分之一单位。 */
@Immutable
data class LiquidGlassV2Tuning(
    val cornerDp: Int = 28,
    val blurDp: Int = 4,
    val refractionHeightDp: Int = 16,
    val refractionAmountDp: Int = 32,
    val depth: Int = 0,
    val chromatic: Int = 0,
    val vibrancy: Int = 1,
    val tintPercent: Int = 0,
    /** 0 = 主题表面色，1 = 白，2 = 黑，3 = 橙。 */
    val tintColor: Int = 0,
    /** 0 = 关闭，1 = 默认（方向光），2 = 环境光，3 = 纯色。 */
    val highlightStyle: Int = 1,
    val highlightWidthTenths: Int = 5,
    val highlightAlphaPercent: Int = 100,
    val highlightAngle: Int = 45,
    val highlightFalloffTenths: Int = 10,
    val shadowRadiusDp: Int = 24,
    val shadowAlphaPercent: Int = 10,
    val innerShadowRadiusDp: Int = 0,
    val innerShadowAlphaPercent: Int = 15,
    val press: Int = 1,
) {
    companion object {
        val DefaultValues: List<Int> = LiquidGlassV2Tuning().toList()

        fun fromList(v: List<Int>): LiquidGlassV2Tuning =
            LiquidGlassV2Tuning(
                v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9],
                v[10], v[11], v[12], v[13], v[14], v[15], v[16], v[17], v[18],
            )
    }

    fun toList(): List<Int> =
        listOf(
            cornerDp, blurDp, refractionHeightDp, refractionAmountDp, depth, chromatic, vibrancy,
            tintPercent, tintColor, highlightStyle, highlightWidthTenths, highlightAlphaPercent,
            highlightAngle, highlightFalloffTenths, shadowRadiusDp, shadowAlphaPercent,
            innerShadowRadiusDp, innerShadowAlphaPercent, press,
        )
}

@Immutable
data class LiquidGlassV2Config(
    val enabled: Boolean = false,
    val tuning: LiquidGlassV2Tuning = LiquidGlassV2Tuning(),
)

val LocalLiquidGlassV2Config = staticCompositionLocalOf { LiquidGlassV2Config() }

/** 首页内容层录制的背景；悬浮栏从这里取样。 */
val LocalGlassV2Backdrop = staticCompositionLocalOf<LayerBackdrop?> { null }

@Composable
fun rememberLiquidGlassV2Tuning(preferencesManager: PreferencesManager): LiquidGlassV2Tuning {
    val flow =
        remember(preferencesManager) {
            preferencesManager
                .getLiquidGlassV2Tuning(LiquidGlassV2Tuning.DefaultValues)
                .map(LiquidGlassV2Tuning::fromList)
        }
    return flow.collectAsStateWithLifecycle(LiquidGlassV2Tuning()).value
}

/** 标记“被玻璃取样”的内容层。 */
@Composable
fun Modifier.liquidGlassV2Source(): Modifier {
    val config = LocalLiquidGlassV2Config.current
    val backdrop = LocalGlassV2Backdrop.current
    return if (config.enabled && backdrop != null) layerBackdrop(backdrop) else this
}

/** 设置页滑块的显示用：十分之一单位。 */
fun Int.tenthsText(): String = String.format(java.util.Locale.US, "%.1f", this / 10f)

private val OrangeTint = Color(0xFFFF8D28)

/** Kyant Backdrop 玻璃表面：设置页的所有调节项都在这里生效。 */
@Composable
fun Modifier.liquidGlassV2Surface(
    backdrop: Backdrop,
    tuning: LiquidGlassV2Tuning,
    shape: Shape,
    surfaceColor: Color,
    pressScale: () -> Float = { 1f },
): Modifier {
    val tint =
        when (tuning.tintColor) {
            1 -> Color.White
            2 -> Color.Black
            3 -> OrangeTint
            else -> surfaceColor
        }
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            if (tuning.vibrancy == 1) vibrancy()
            if (tuning.blurDp > 0) blur(tuning.blurDp.dp.toPx())
            val maxHeight = size.minDimension / 2f
            val height = tuning.refractionHeightDp.dp.toPx().coerceAtMost(maxHeight)
            lens(
                refractionHeight = height,
                refractionAmount = tuning.refractionAmountDp.dp.toPx(),
                depthEffect = tuning.depth == 1,
                chromaticAberration = tuning.chromatic == 1,
            )
        },
        highlight = {
            if (tuning.highlightStyle == 0) {
                null
            } else {
                val style =
                    when (tuning.highlightStyle) {
                        2 -> HighlightStyle.Ambient()
                        3 -> HighlightStyle.Plain()
                        else ->
                            HighlightStyle.Default(
                                angle = tuning.highlightAngle.toFloat(),
                                falloff = tuning.highlightFalloffTenths / 10f,
                            )
                    }
                Highlight(
                    width = (tuning.highlightWidthTenths / 10f).dp,
                    alpha = (tuning.highlightAlphaPercent / 100f).coerceIn(0f, 1f),
                    style = style,
                )
            }
        },
        shadow = {
            if (tuning.shadowRadiusDp > 0 && tuning.shadowAlphaPercent > 0) {
                Shadow(
                    radius = tuning.shadowRadiusDp.dp,
                    color = Color.Black.copy(alpha = (tuning.shadowAlphaPercent / 100f).coerceIn(0f, 1f)),
                )
            } else {
                null
            }
        },
        innerShadow = {
            if (tuning.innerShadowRadiusDp > 0 && tuning.innerShadowAlphaPercent > 0) {
                InnerShadow(
                    radius = tuning.innerShadowRadiusDp.dp,
                    color = Color.Black.copy(alpha = (tuning.innerShadowAlphaPercent / 100f).coerceIn(0f, 1f)),
                )
            } else {
                null
            }
        },
        layerBlock = {
            val scale = pressScale()
            scaleX = scale
            scaleY = scale
        },
        onDrawSurface = {
            if (tuning.tintPercent > 0) {
                drawRect(tint.copy(alpha = (tuning.tintPercent / 100f).coerceIn(0f, 1f)))
            }
        },
    )
}

/** 设置页顶部固定的一行预览：调节任何选项都能立即看到效果；开启“按压”后可按住玻璃。 */
@Composable
fun LiquidGlassV2Preview(
    tuning: LiquidGlassV2Tuning,
    modifier: Modifier = Modifier,
) {
    val backdrop = rememberLayerBackdrop()
    val surface = MaterialTheme.colorScheme.surfaceContainerHigh
    val transition = rememberInfiniteTransition(label = "glassV2Preview")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000, easing = LinearEasing), RepeatMode.Reverse),
        label = "phase",
    )
    var pressed by remember { mutableStateOf(false) }
    val squish by animateFloatAsState(
        targetValue = if (pressed && tuning.press == 1) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
        label = "squish",
    )

    Box(
        modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        Box(Modifier.matchParentSize().layerBackdrop(backdrop)) {
            Canvas(Modifier.matchParentSize()) {
                drawRect(
                    Brush.linearGradient(
                        listOf(Color(0xFFFFB86C), Color(0xFFFF6FA5), Color(0xFF6C8CFF)),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height),
                    ),
                )
                val r = size.height * 0.3f
                drawCircle(Color(0xFFFFE27A), r, Offset(size.width * (0.1f + 0.8f * phase), size.height * 0.3f))
                drawCircle(Color(0xFF2B2B55), r * 0.8f, Offset(size.width * (0.9f - 0.8f * phase), size.height * 0.75f))
                drawCircle(Color(0xFF3DDC97), r * 0.6f, Offset(size.width * (0.5f + 0.3f * phase), size.height * 0.5f))
            }
            Text(
                text = "LIQUID GLASS V2 · LIQUID GLASS V2",
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false,
                modifier =
                    Modifier
                        .align(Alignment.TopStart)
                        .wrapContentWidth(align = Alignment.Start, unbounded = true)
                        .offset { IntOffset((-phase * 160.dp.toPx()).roundToInt(), 6.dp.roundToPx()) },
            )
        }
        Box(
            modifier =
                Modifier
                    .align(Alignment.Center)
                    .size(width = 220.dp, height = 64.dp)
                    .liquidGlassV2Surface(
                        backdrop = backdrop,
                        tuning = tuning,
                        shape = RoundedCornerShape(tuning.cornerDp.dp),
                        surfaceColor = surface,
                        pressScale = { squish },
                    ).pointerInput(Unit) {
                        detectTapGestures(onPress = {
                            pressed = true
                            tryAwaitRelease()
                            pressed = false
                        })
                    },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Liquid glass V2",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }
}

/** V2 的折射需要 Android 13+，模糊需要 Android 12+。 */
val isLiquidGlassV2Supported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
