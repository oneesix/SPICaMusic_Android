package me.spica27.spicamusic.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import me.spica27.spicamusic.core.preferences.PreferencesManager

/** 液态玻璃调节项；ordinal 与 PreferencesManager 中的存储顺序一致。 */
enum class LiquidGlassSetting {
    Blur,
    Quality,
    Tint,
    Corner,
    Refraction,
    RefractionHeight,
    Falloff,
    Rim,
    RimWidth,
    Shadow,
    Mode,
    Saturation,
}

/** 默认值与演示页一致：磨砂模糊、模糊 14、色彩增强 1.7、色调 0.28、边缘高光 0.55、阴影 0.14。 */
@Immutable
data class LiquidGlassTuning(
    val blurDp: Int = 14,
    val quality: Int = 80,
    val tintPercent: Int = 28,
    val cornerDp: Int = 36,
    val refractionPercent: Int = 30,
    val refractionHeightDp: Int = 10,
    val falloffPercent: Int = 35,
    val rimPercent: Int = 55,
    val rimWidthDp: Int = 1,
    val shadowPercent: Int = 14,
    /** 0 = 磨砂模糊，1 = 折射透镜。 */
    val mode: Int = 0,
    val saturationPercent: Int = 170,
) {
    val isLens: Boolean get() = mode == 1

    companion object {
        val DefaultValues: List<Int> = LiquidGlassTuning().toList()

        fun fromList(v: List<Int>): LiquidGlassTuning =
            LiquidGlassTuning(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9], v[10], v[11])
    }

    fun toList(): List<Int> =
        listOf(
            blurDp,
            quality,
            tintPercent,
            cornerDp,
            refractionPercent,
            refractionHeightDp,
            falloffPercent,
            rimPercent,
            rimWidthDp,
            shadowPercent,
            mode,
            saturationPercent,
        )
}

@Composable
fun rememberLiquidGlassTuning(preferencesManager: PreferencesManager): LiquidGlassTuning {
    val flow =
        remember(preferencesManager) {
            preferencesManager
                .getLiquidGlassTuning(LiquidGlassTuning.DefaultValues)
                .map(LiquidGlassTuning::fromList)
        }
    return flow.collectAsStateWithLifecycle(LiquidGlassTuning()).value
}
