package me.spica27.spicamusic.ui.home.page

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.combine
import me.spica27.spicamusic.core.preferences.PreferencesManager
import me.spica27.spicamusic.ui.home.player_bar.BottomBarLabelMode

/** 音乐页悬浮顶栏的数值设置；ordinal 与 MusicTopBarCustomization.toInts 的顺序一致。 */
enum class MusicTopBarSetting {
    Horizontal,
    Top,
    Width,
    Height,
    LabelMode,
    Blur,
    BlurAmount,
    BlurPosition,
    SoftEdge,
    EdgeFade,
}

/**
 * 音乐页悬浮顶栏自定义，写法与底栏一致：
 * - Horizontal：0（左）~100（右），Top：距状态栏的偏移 dp，Width：占可用宽度的百分比。
 * - Height：0 或小于 44 表示自动（44 dp）。
 * - LabelMode 复用底栏的标签显示方式（图标+文字 / 仅图标 / 仅文字）。
 * - Blur：顶栏后面的渐进模糊（1 开 / 0 关）；BlurAmount 模糊半径 dp；BlurPosition 模糊下沿相对顶栏下沿的偏移 dp（可为负）；
 *   SoftEdge 1 时模糊在下沿渐隐，EdgeFade 为渐隐高度 dp。
 */
@Immutable
data class MusicTopBarCustomization(
    val horizontal: Int = 50,
    val top: Int = 8,
    val width: Int = 92,
    val height: Int = 0,
    val labelMode: Int = BottomBarLabelMode.IconAndText,
    val blur: Int = 1,
    val blurAmount: Int = 12,
    val blurPosition: Int = 8,
    val softEdge: Int = 1,
    val edgeFade: Int = 24,
) {
    fun valueOf(setting: MusicTopBarSetting): Int =
        when (setting) {
            MusicTopBarSetting.Horizontal -> horizontal
            MusicTopBarSetting.Top -> top
            MusicTopBarSetting.Width -> width
            MusicTopBarSetting.Height -> height
            MusicTopBarSetting.LabelMode -> labelMode
            MusicTopBarSetting.Blur -> blur
            MusicTopBarSetting.BlurAmount -> blurAmount
            MusicTopBarSetting.BlurPosition -> blurPosition
            MusicTopBarSetting.SoftEdge -> softEdge
            MusicTopBarSetting.EdgeFade -> edgeFade
        }

    companion object {
        fun from(values: List<Int>) =
            MusicTopBarCustomization(
                horizontal = values[0],
                top = values[1],
                width = values[2],
                height = values[3],
                labelMode = values[4],
                blur = values[5],
                blurAmount = values[6],
                blurPosition = values[7],
                softEdge = values[8],
                edgeFade = values[9],
            )
    }
}

internal fun MusicTopBarSetting.preferenceKey() =
    when (this) {
        MusicTopBarSetting.Horizontal -> PreferencesManager.Keys.MUSIC_TOP_BAR_H
        MusicTopBarSetting.Top -> PreferencesManager.Keys.MUSIC_TOP_BAR_TOP
        MusicTopBarSetting.Width -> PreferencesManager.Keys.MUSIC_TOP_BAR_W
        MusicTopBarSetting.Height -> PreferencesManager.Keys.MUSIC_TOP_BAR_HEIGHT
        MusicTopBarSetting.LabelMode -> PreferencesManager.Keys.MUSIC_TOP_BAR_LABEL_MODE
        MusicTopBarSetting.Blur -> PreferencesManager.Keys.MUSIC_TOP_BAR_BLUR
        MusicTopBarSetting.BlurAmount -> PreferencesManager.Keys.MUSIC_TOP_BAR_BLUR_AMOUNT
        MusicTopBarSetting.BlurPosition -> PreferencesManager.Keys.MUSIC_TOP_BAR_BLUR_POSITION
        MusicTopBarSetting.SoftEdge -> PreferencesManager.Keys.MUSIC_TOP_BAR_SOFT_EDGE
        MusicTopBarSetting.EdgeFade -> PreferencesManager.Keys.MUSIC_TOP_BAR_EDGE_FADE
    }

internal fun MusicTopBarSetting.defaultValue(): Int = MusicTopBarCustomization().valueOf(this)

@Composable
fun rememberMusicTopBarCustomization(preferencesManager: PreferencesManager): MusicTopBarCustomization {
    val flow =
        remember(preferencesManager) {
            combine(
                MusicTopBarSetting.entries.map { setting ->
                    preferencesManager.getInt(setting.preferenceKey(), setting.defaultValue())
                },
            ) { values -> MusicTopBarCustomization.from(values.toList()) }
        }
    return flow.collectAsStateWithLifecycle(MusicTopBarCustomization()).value
}
