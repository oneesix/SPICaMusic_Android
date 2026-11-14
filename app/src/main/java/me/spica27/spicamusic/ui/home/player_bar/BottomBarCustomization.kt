package me.spica27.spicamusic.ui.home.player_bar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import me.spica27.spicamusic.core.preferences.PreferencesManager

/** 数值项；ordinal 与 PreferencesManager 中的存储顺序一致。 */
enum class BottomBarSetting {
    MiniHorizontal,
    MiniRaise,
    MiniWidth,
    MiniHeight,
    TabsHorizontal,
    TabsRaise,
    TabsWidth,
    TabsHeight,
    LabelMode,
    SearchTab,
    Bubble,
    BubbleSize,
    Blur,
    BlurAmount,
    BlurPosition,
    SoftEdge,
    EdgeFade,
    BlurOptimize,
    BlurQuality,
}

/** 标签显示方式。 */
object BottomBarLabelMode {
    const val IconAndText = 0
    const val IconOnly = 1
    const val TextOnly = 2
}

/**
 * 底栏自定义。默认值保持现有位置与尺寸：
 * - Horizontal：0（左）~100（右），Raise：在默认位置基础上向上抬高的 dp，Width：占可用宽度的百分比。
 * - Height：0 或小于 44 表示自动（沿用默认高度）。
 * - 图标为 Lucide 名称（如 "music"），找不到时回退为内置图标；文字留空表示使用默认文字。
 */
@Immutable
data class BottomBarCustomization(
    val miniHorizontal: Int = 50,
    val miniRaise: Int = 0,
    val miniWidth: Int = 100,
    val miniHeight: Int = 0,
    val tabsHorizontal: Int = 50,
    val tabsRaise: Int = 0,
    val tabsWidth: Int = 100,
    val tabsHeight: Int = 0,
    val labelMode: Int = BottomBarLabelMode.IconAndText,
    val searchTab: Int = 1,
    val bubble: Int = 1,
    val bubbleSize: Int = 135,
    val blur: Int = 1,
    val blurAmount: Int = 16,
    val blurPosition: Int = 8,
    val softEdge: Int = 1,
    val edgeFade: Int = 32,
    /** 1 = render the blur at a lower resolution ([blurQuality] percent). 0 = normal blur. */
    val blurOptimize: Int = 0,
    val blurQuality: Int = 45,
    val icons: List<String> = DefaultIcons,
    val labels: List<String> = DefaultLabels,
) {
    companion object {
        val DefaultIcons = listOf("compass", "music", "library", "search")
        val DefaultLabels = listOf("", "", "", "")

        val DefaultInts: List<Int> = BottomBarCustomization().toInts()

        /** 文本项按 图标0、文字0、图标1、文字1… 排列。 */
        val DefaultTexts: List<String> = DefaultIcons.flatMapIndexed { i, icon -> listOf(icon, DefaultLabels[i]) }

        fun from(
            ints: List<Int>,
            texts: List<String>,
        ) = BottomBarCustomization(
            miniHorizontal = ints[0],
            miniRaise = ints[1],
            miniWidth = ints[2],
            miniHeight = ints[3],
            tabsHorizontal = ints[4],
            tabsRaise = ints[5],
            tabsWidth = ints[6],
            tabsHeight = ints[7],
            labelMode = ints[8],
            searchTab = ints[9],
            bubble = ints[10],
            bubbleSize = ints[11],
            blur = ints[12],
            blurAmount = ints[13],
            blurPosition = ints[14],
            softEdge = ints[15],
            edgeFade = ints[16],
            blurOptimize = ints[17],
            blurQuality = ints[18],
            icons = List(4) { texts[it * 2] },
            labels = List(4) { texts[it * 2 + 1] },
        )
    }

    fun toInts(): List<Int> =
        listOf(
            miniHorizontal,
            miniRaise,
            miniWidth,
            miniHeight,
            tabsHorizontal,
            tabsRaise,
            tabsWidth,
            tabsHeight,
            labelMode,
            searchTab,
            bubble,
            bubbleSize,
            blur,
            blurAmount,
            blurPosition,
            softEdge,
            edgeFade,
            blurOptimize,
            blurQuality,
        )
}

val LocalBottomBarCustomization = staticCompositionLocalOf { BottomBarCustomization() }

@Composable
fun rememberBottomBarCustomization(preferencesManager: PreferencesManager): BottomBarCustomization {
    val flow =
        remember(preferencesManager) {
            preferencesManager
                .getBottomBarCustomization(BottomBarCustomization.DefaultInts, BottomBarCustomization.DefaultTexts)
                .map { (ints, texts) -> BottomBarCustomization.from(ints, texts) }
        }
    return flow.collectAsStateWithLifecycle(BottomBarCustomization()).value
}
