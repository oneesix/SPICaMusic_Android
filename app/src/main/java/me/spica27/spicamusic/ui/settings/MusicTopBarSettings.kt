package me.spica27.spicamusic.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.spica27.spicamusic.R
import me.spica27.spicamusic.ui.home.page.MusicTopBarCustomization
import me.spica27.spicamusic.ui.home.page.MusicTopBarSetting
import me.spica27.spicamusic.ui.home.player_bar.BottomBarLabelMode
import me.spica27.spicamusic.ui.widget.LyricsSettingsChips
import me.spica27.spicamusic.ui.widget.LyricsSettingsResetButton
import me.spica27.spicamusic.ui.widget.LyricsSettingsSlider

/** 音乐页悬浮顶栏自定义：位置、尺寸、标签内容；玻璃效果沿用设置页的液态玻璃开关。 */
@Composable
internal fun MusicTopBarSettingsContent(
    custom: MusicTopBarCustomization,
    onInt: (MusicTopBarSetting, Int) -> Unit,
    onReset: () -> Unit,
) {
    val auto = stringResource(R.string.bb_auto)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        LyricsSettingsSlider(
            label = stringResource(R.string.bb_horizontal),
            description = "",
            value = custom.horizontal,
            range = 0..100,
            step = 1,
            valueText = { "$it%" },
            onValueChange = { onInt(MusicTopBarSetting.Horizontal, it) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.mtb_top),
            description = "",
            value = custom.top,
            range = 0..200,
            step = 1,
            valueText = { "$it dp" },
            onValueChange = { onInt(MusicTopBarSetting.Top, it) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.bb_width),
            description = "",
            value = custom.width,
            range = 55..100,
            step = 1,
            valueText = { "$it%" },
            onValueChange = { onInt(MusicTopBarSetting.Width, it) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.bb_height),
            description = "",
            value = custom.height,
            range = 0..90,
            step = 1,
            valueText = { if (it < 44) auto else "$it dp" },
            onValueChange = { onInt(MusicTopBarSetting.Height, it) },
        )
        LyricsSettingsChips(
            label = stringResource(R.string.bb_content),
            description = "",
            options =
                listOf(BottomBarLabelMode.IconAndText, BottomBarLabelMode.IconOnly, BottomBarLabelMode.TextOnly),
            selected = custom.labelMode,
            optionLabel = {
                stringResource(
                    when (it) {
                        BottomBarLabelMode.IconOnly -> R.string.bb_content_icon
                        BottomBarLabelMode.TextOnly -> R.string.bb_content_text
                        else -> R.string.bb_content_both
                    },
                )
            },
            onSelect = { onInt(MusicTopBarSetting.LabelMode, it) },
        )
        LyricsSettingsResetButton(
            text = stringResource(R.string.mtb_reset),
            onClick = onReset,
        )
    }
}
