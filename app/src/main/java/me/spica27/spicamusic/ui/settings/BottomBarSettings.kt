package me.spica27.spicamusic.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.spica27.spicamusic.R
import me.spica27.spicamusic.ui.home.player_bar.BottomBarCustomization
import me.spica27.spicamusic.ui.home.player_bar.BottomBarLabelMode
import me.spica27.spicamusic.ui.home.player_bar.BottomBarSetting
import me.spica27.spicamusic.ui.widget.LucideIcons
import me.spica27.spicamusic.ui.widget.LyricsSettingsChips
import me.spica27.spicamusic.ui.widget.LyricsSettingsResetButton
import me.spica27.spicamusic.ui.widget.LyricsSettingsSlider
import me.spica27.spicamusic.ui.widget.LyricsSettingsToggle

/** 底栏自定义：迷你播放条与导航栏的位置/尺寸、标签图标与文字、搜索标签、长按气泡。 */
@Composable
internal fun BottomBarSettingsContent(
    custom: BottomBarCustomization,
    onInt: (BottomBarSetting, Int) -> Unit,
    onText: (slot: Int, isIcon: Boolean, value: String) -> Unit,
    onReset: () -> Unit,
) {
    var resetToken by remember { mutableIntStateOf(0) }

    @Composable
    fun GeometrySlider(
        label: Int,
        setting: BottomBarSetting,
        value: Int,
        range: IntRange,
        unit: String,
        autoBelow: Int = 0,
    ) {
        val auto = stringResource(R.string.bb_auto)
        LyricsSettingsSlider(
            label = stringResource(label),
            description = "",
            value = value,
            range = range,
            step = 1,
            valueText = { if (it < autoBelow) auto else "$it$unit" },
            onValueChange = { onInt(setting, it) },
        )
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(stringResource(R.string.bb_mini), style = MaterialTheme.typography.titleSmall)
        GeometrySlider(R.string.bb_horizontal, BottomBarSetting.MiniHorizontal, custom.miniHorizontal, 0..100, "%")
        GeometrySlider(R.string.bb_raise, BottomBarSetting.MiniRaise, custom.miniRaise, 0..420, " dp")
        GeometrySlider(R.string.bb_width, BottomBarSetting.MiniWidth, custom.miniWidth, 55..100, "%")
        GeometrySlider(R.string.bb_height, BottomBarSetting.MiniHeight, custom.miniHeight, 0..120, " dp", autoBelow = 44)

        Text(
            stringResource(R.string.bb_tabs),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 12.dp),
        )
        GeometrySlider(R.string.bb_horizontal, BottomBarSetting.TabsHorizontal, custom.tabsHorizontal, 0..100, "%")
        GeometrySlider(R.string.bb_raise, BottomBarSetting.TabsRaise, custom.tabsRaise, 0..420, " dp")
        GeometrySlider(R.string.bb_width, BottomBarSetting.TabsWidth, custom.tabsWidth, 55..100, "%")
        GeometrySlider(R.string.bb_height, BottomBarSetting.TabsHeight, custom.tabsHeight, 0..130, " dp", autoBelow = 44)

        Text(
            stringResource(R.string.bb_icons),
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 12.dp),
        )
        val defaultLabels =
            listOf(
                stringResource(R.string.nav_tab_finder),
                stringResource(R.string.nav_tab_music),
                stringResource(R.string.nav_tab_library),
                stringResource(R.string.nav_tab_search),
            )
        for (slot in 0 until 4) {
            key(slot, resetToken) {
                BottomBarTabEditor(
                    defaultLabel = defaultLabels[slot],
                    icon = custom.icons[slot],
                    label = custom.labels[slot],
                    onIcon = { onText(slot, true, it) },
                    onLabel = { onText(slot, false, it) },
                )
            }
        }

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
            onSelect = { onInt(BottomBarSetting.LabelMode, it) },
        )
        LyricsSettingsToggle(
            label = stringResource(R.string.bb_search_tab),
            description = stringResource(R.string.bb_search_tab_sub),
            checked = custom.searchTab == 1,
            onCheckedChange = { onInt(BottomBarSetting.SearchTab, if (it) 1 else 0) },
        )
        LyricsSettingsToggle(
            label = stringResource(R.string.bb_bubble),
            description = stringResource(R.string.bb_bubble_sub),
            checked = custom.bubble == 1,
            onCheckedChange = { onInt(BottomBarSetting.Bubble, if (it) 1 else 0) },
        )
        if (custom.bubble == 1) {
            GeometrySlider(R.string.bb_bubble_size, BottomBarSetting.BubbleSize, custom.bubbleSize, 100..170, "%")
        }
        LyricsSettingsToggle(
            label = stringResource(R.string.bb_blur),
            description = stringResource(R.string.bb_blur_sub),
            checked = custom.blur == 1,
            onCheckedChange = { onInt(BottomBarSetting.Blur, if (it) 1 else 0) },
        )
        if (custom.blur == 1) {
            GeometrySlider(R.string.mtb_blur_amount, BottomBarSetting.BlurAmount, custom.blurAmount, 0..40, " dp")
            LyricsSettingsToggle(
                label = stringResource(R.string.lyr_set_blur_optimize),
                description = stringResource(R.string.lyr_set_blur_optimize_desc),
                checked = custom.blurOptimize == 1,
                onCheckedChange = { onInt(BottomBarSetting.BlurOptimize, if (it) 1 else 0) },
            )
            if (custom.blurOptimize == 1) {
                LyricsSettingsSlider(
                    label = stringResource(R.string.lyr_set_blur_quality),
                    description = stringResource(R.string.lyr_set_blur_quality_desc),
                    value = custom.blurQuality,
                    range = 5..100,
                    step = 5,
                    valueText = { "$it%" },
                    onValueChange = { onInt(BottomBarSetting.BlurQuality, it) },
                )
            }
            LyricsSettingsSlider(
                label = stringResource(R.string.mtb_blur_position),
                description = stringResource(R.string.bb_blur_position_sub),
                value = custom.blurPosition,
                range = -180..120,
                step = 1,
                valueText = { "$it dp" },
                onValueChange = { onInt(BottomBarSetting.BlurPosition, it) },
            )
            LyricsSettingsToggle(
                label = stringResource(R.string.mtb_blur_soft),
                description = stringResource(R.string.bb_blur_soft_sub),
                checked = custom.softEdge == 1,
                onCheckedChange = { onInt(BottomBarSetting.SoftEdge, if (it) 1 else 0) },
            )
            if (custom.softEdge == 1) {
                GeometrySlider(R.string.mtb_blur_fade, BottomBarSetting.EdgeFade, custom.edgeFade, 0..80, " dp")
            }
        }
        LyricsSettingsResetButton(
            text = stringResource(R.string.bb_reset),
            onClick = {
                resetToken++
                onReset()
            },
        )
    }
}

/** 单个标签：图标预览 + 文字输入，下方是 Lucide 图标搜索框和搜索结果。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BottomBarTabEditor(
    defaultLabel: String,
    icon: String,
    label: String,
    onIcon: (String) -> Unit,
    onLabel: (String) -> Unit,
) {
    val context = LocalContext.current
    var labelText by remember { mutableStateOf(label) }
    var query by remember { mutableStateOf(icon) }
    val resolved = LucideIcons.resolve(context, icon)
    val results = if (query.isNotBlank() && query.trim() != icon) LucideIcons.searchNames(context, query) else emptyList()

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (resolved != 0) {
                Icon(painterResource(resolved), null, Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            }
            OutlinedTextField(
                value = labelText,
                onValueChange = {
                    labelText = it
                    onLabel(it)
                },
                label = { Text(stringResource(R.string.bb_label_hint)) },
                placeholder = { Text(defaultLabel) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                // 输入的名称恰好是现有 Lucide 图标时立即生效。
                if (LucideIcons.resolve(context, it) != 0) onIcon(it.trim().lowercase().replace('_', '-').replace(' ', '-'))
            },
            label = { Text(stringResource(R.string.bb_icon_hint)) },
            supportingText = {
                if (query.isNotBlank() && resolved == 0 && results.isEmpty()) Text(stringResource(R.string.bb_icon_missing))
            },
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
        if (results.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                results.forEach { name ->
                    IconButton(onClick = {
                        query = name
                        onIcon(name)
                    }) {
                        Icon(painterResource(LucideIcons.resolve(context, name)), name)
                    }
                }
            }
        }
    }
}
