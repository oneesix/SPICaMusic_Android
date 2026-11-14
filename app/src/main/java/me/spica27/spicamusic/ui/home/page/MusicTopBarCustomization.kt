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
}

/**
 * 音乐页悬浮顶栏自定义，写法与底栏一致：
 * - Horizontal：0（左）~100（右），Top：距状态栏的偏移 dp，Width：占可用宽度的百分比。
 * - Height：0 或小于 44 表示自动（44 dp）。
 * - LabelMode 复用底栏的标签显示方式（图标+文字 / 仅图标 / 仅文字）。
 */
@Immutable
data class MusicTopBarCustomization(
    val horizontal: Int = 50,
    val top: Int = 8,
    val width: Int = 92,
    val height: Int = 0,
    val labelMode: Int = BottomBarLabelMode.IconAndText,
) {
    fun valueOf(setting: MusicTopBarSetting): Int =
        when (setting) {
            MusicTopBarSetting.Horizontal -> horizontal
            MusicTopBarSetting.Top -> top
            MusicTopBarSetting.Width -> width
            MusicTopBarSetting.Height -> height
            MusicTopBarSetting.LabelMode -> labelMode
        }

    companion object {
        fun from(values: List<Int>) =
            MusicTopBarCustomization(
                horizontal = values[0],
                top = values[1],
                width = values[2],
                height = values[3],
                labelMode = values[4],
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
