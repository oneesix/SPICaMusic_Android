package me.spica27.spicamusic.ui.player

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.spica27.spicamusic.R
import me.spica27.spicamusic.ui.widget.LyricsSettingsChips
import me.spica27.spicamusic.ui.widget.LyricsSettingsResetButton
import me.spica27.spicamusic.ui.widget.LyricsSettingsSection
import me.spica27.spicamusic.ui.widget.LyricsSettingsSlider
import me.spica27.spicamusic.ui.widget.LyricsSettingsToggle
import java.util.Locale

/**
 * Settings of the Bubble player. Opened by holding the play button for 2 seconds. The page behind stays
 * visible (no dimming), so every change shows right away.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BubbleSettingsSheet(
    tuning: BubblePlayerTuning,
    albumMaxWidthDp: Int,
    onChange: (BubblePlayerTuning) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val values = tuning.toList()

    @Composable
    fun Slide(
        label: Int,
        index: Int,
        range: IntRange,
        step: Int = 1,
        text: (Int) -> String = { "$it dp" },
    ) = LyricsSettingsSlider(
        label = stringResource(label),
        description = "",
        value = values[index],
        range = range,
        step = step,
        valueText = text,
        onValueChange = { onChange(tuning.withValue(index, it)) },
    )

    @Composable
    fun Toggle(
        label: Int,
        description: Int,
        index: Int,
    ) = LyricsSettingsToggle(
        label = stringResource(label),
        description = stringResource(description),
        checked = values[index] == 1,
        onCheckedChange = { onChange(tuning.withValue(index, if (it) 1 else 0)) },
    )

    val spText: (Int) -> String = { String.format(Locale.US, "%.1f sp", it / 10f) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        scrimColor = Color.Transparent,
    ) {
        Column(
            modifier =
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
        ) {
            Text(
                text = stringResource(R.string.bp_settings_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            LyricsSettingsSection(stringResource(R.string.bp_sec_order))
            tuning.barOrder.forEachIndexed { position, bar ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text =
                            "${position + 1}. " +
                                stringResource(
                                    when (bar) {
                                        BubblePlayerTuning.BAR_PROGRESS -> R.string.bp_bar_progress
                                        BubblePlayerTuning.BAR_TITLE -> R.string.bp_bar_title
                                        else -> R.string.bp_bar_lyrics
                                    },
                                ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = { onChange(tuning.withMovedBar(bar, -1)) },
                        enabled = position > 0,
                    ) {
                        Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = stringResource(R.string.bp_move_up))
                    }
                    IconButton(
                        onClick = { onChange(tuning.withMovedBar(bar, 1)) },
                        enabled = position < 2,
                    ) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = stringResource(R.string.bp_move_down))
                    }
                }
            }

            LyricsSettingsSection(stringResource(R.string.bp_sec_heights))
            Slide(R.string.bp_h_top, BubbleSetting.TopBarHeight, 44..90)
            Slide(R.string.bp_h_progress, BubbleSetting.ProgressHeight, 52..110)
            Slide(R.string.bp_h_title, BubbleSetting.TitleBarHeight, 44..90)
            Slide(R.string.bp_h_lyrics, BubbleSetting.LyricsBarHeight, 32..80)

            LyricsSettingsSection(stringResource(R.string.bp_sec_text))
            Slide(R.string.bp_t_time, BubbleSetting.TimeText, 140..320, 5, spText)
            Slide(R.string.bp_t_next, BubbleSetting.NextText, 100..180, 5, spText)
            Slide(R.string.bp_t_time_label, BubbleSetting.TimeLabel, 80..160, 5, spText)
            Slide(R.string.bp_t_next_label, BubbleSetting.NextLabel, 80..160, 5, spText)
            Slide(R.string.bp_t_lyrics, BubbleSetting.LyricsText, 110..220, 5, spText)
            Slide(R.string.bp_t_title, BubbleSetting.TitleText, 120..220, 5, spText)
            Slide(R.string.bp_t_artist, BubbleSetting.ArtistText, 100..180, 5, spText)

            LyricsSettingsSection(stringResource(R.string.bp_sec_info))
            LyricsSettingsChips(
                label = stringResource(R.string.bp_info_pos),
                description = "",
                options = listOf(0, 1, 2),
                selected = tuning.infoBarPos,
                optionLabel = {
                    stringResource(
                        when (it) {
                            0 -> R.string.bp_info_above
                            1 -> R.string.bp_info_below
                            else -> R.string.bp_info_off
                        },
                    )
                },
                onSelect = { onChange(tuning.withValue(BubbleSetting.InfoBarPos, it)) },
            )
            if (tuning.infoBarPos != 2) {
                LyricsSettingsChips(
                    label = stringResource(R.string.bp_info_style),
                    description = "",
                    options = listOf(0, 1),
                    selected = tuning.infoBarStyle,
                    optionLabel = { stringResource(if (it == 0) R.string.bp_info_cols else R.string.bp_info_line) },
                    onSelect = { onChange(tuning.withValue(BubbleSetting.InfoBarStyle, it)) },
                )
                Slide(R.string.bp_h_info, BubbleSetting.InfoBarHeight, 28..64)
                Slide(R.string.bp_t_info, BubbleSetting.InfoText, 90..180, 5, spText)
            }

            LyricsSettingsSection(stringResource(R.string.bp_sec_blur))
            Toggle(R.string.bp_blur_on, R.string.bp_blur_on_desc, BubbleSetting.BlurEnabled)
            if (tuning.blurEnabled == 1) {
                Slide(R.string.bp_blur_amount, BubbleSetting.BlurAmount, 0..40)
                Toggle(R.string.bp_blur_optimize, R.string.bp_blur_optimize_desc, BubbleSetting.BlurOptimize)
                if (tuning.blurOptimize == 1) {
                    Slide(R.string.bp_blur_quality, BubbleSetting.BlurQuality, 5..100, 5) { "$it%" }
                }
            }

            LyricsSettingsSection(stringResource(R.string.bp_sec_seek))
            Toggle(R.string.bp_particles, R.string.bp_particles_desc, BubbleSetting.ParticleTrail)
            Slide(R.string.bp_seek_height, BubbleSetting.SeekHeight, 24..96)
            Slide(R.string.bp_seek_thickness, BubbleSetting.SeekThickness, 1..14)
            Slide(R.string.bp_seek_width, BubbleSetting.SeekWidth, 30..100) { "$it%" }

            LyricsSettingsSection(stringResource(R.string.bp_sec_album))
            Toggle(R.string.bp_album_bg, R.string.bp_album_bg_desc, BubbleSetting.AlbumBackground)
            if (tuning.albumBackground == 1) {
                Toggle(R.string.bp_album_info, R.string.bp_album_info_desc, BubbleSetting.YearInfo)
            }
            // The width can reach the left and right padding of the page.
            Slide(R.string.bp_album_width, BubbleSetting.AlbumWidth, 120..albumMaxWidthDp.coerceAtLeast(121))
            Slide(R.string.bp_album_height, BubbleSetting.AlbumHeight, 120..460)
            Slide(R.string.bp_album_corner, BubbleSetting.Corner, 0..130)

            LyricsSettingsSection(stringResource(R.string.bp_sec_hl))
            LyricsSettingsChips(
                label = stringResource(R.string.bp_hl_style),
                description = "",
                options = listOf(0, 1, 2, 3),
                selected = tuning.hlStyle,
                optionLabel = {
                    stringResource(
                        when (it) {
                            0 -> R.string.bp_hl_off
                            1 -> R.string.bp_hl_directional
                            2 -> R.string.bp_hl_ambient
                            else -> R.string.bp_hl_solid
                        },
                    )
                },
                onSelect = { onChange(tuning.withValue(BubbleSetting.HlStyle, it)) },
            )
            if (tuning.hlStyle != 0) {
                val tenthsDp: (Int) -> String = { String.format(Locale.US, "%.1f dp", it / 10f) }
                Slide(R.string.bp_hl_width, BubbleSetting.HlWidth, 1..60, 1, tenthsDp)
                Slide(R.string.bp_hl_alpha, BubbleSetting.HlAlpha, 1..100) { "$it%" }
                if (tuning.hlStyle == 1) {
                    Slide(R.string.bp_hl_angle, BubbleSetting.HlAngle, 0..360, 5) { "$it°" }
                    Slide(R.string.bp_hl_falloff, BubbleSetting.HlFalloff, 0..40, 1) { String.format(Locale.US, "%.1f", it / 10f) }
                }
            }

            LyricsSettingsSection(stringResource(R.string.bp_sec_buttons))
            Slide(R.string.bp_play_size, BubbleSetting.PlaySize, 32..96)
            Slide(R.string.bp_skip_size, BubbleSetting.SkipSize, 24..72)
            Slide(R.string.bp_btn_gap, BubbleSetting.ButtonGap, 0..40)
            Slide(R.string.bp_btn_bottom, BubbleSetting.ButtonBottom, 0..120)
            Slide(R.string.bp_btn_shift, BubbleSetting.ButtonShift, -100..100)

            LyricsSettingsResetButton(
                text = stringResource(R.string.bp_reset),
                onClick = { onChange(BubblePlayerTuning.Default) },
            )
        }
    }
}
