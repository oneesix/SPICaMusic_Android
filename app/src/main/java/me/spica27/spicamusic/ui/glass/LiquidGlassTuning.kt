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
}

/** 默认值等于改动前底栏的固定参数，所以不动设置时外观不变。 */
@Immutable
data class LiquidGlassTuning(
    val blurDp: Int = 18,
    val quality: Int = 100,
    val tintPercent: Int = 40,
    val cornerDp: Int = 36,
    val refractionPercent: Int = 30,
    val refractionHeightDp: Int = 10,
    val falloffPercent: Int = 35,
    val rimPercent: Int = 40,
    val rimWidthDp: Int = 1,
    val shadowPercent: Int = 8,
) {
    companion object {
        val DefaultValues: List<Int> = LiquidGlassTuning().toList()

        fun fromList(v: List<Int>): LiquidGlassTuning =
            LiquidGlassTuning(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9])
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
