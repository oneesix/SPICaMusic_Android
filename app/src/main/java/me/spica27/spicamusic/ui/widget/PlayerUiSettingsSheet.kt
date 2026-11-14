package me.spica27.spicamusic.ui.widget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.spica27.spicamusic.R
import me.spica27.spicamusic.core.preferences.LyricsStyleSettings

/**
 * Settings sheet of the new player UI (opened from the three-dots menu, below "Toggle lyrics").
 *
 * The blur of the top bar and the bottom bar uses the existing lyrics header and bottom bar settings,
 * and the bottom bar padding is the existing bottom bar setting, so they are not repeated here.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerUiSettingsSheet(
    settings: LyricsStyleSettings,
    onUpdate: ((LyricsStyleSettings) -> LyricsStyleSettings) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 24.dp),
        ) {
            LyricsSettingsSection(stringResource(R.string.player_ui_settings))

            LyricsSettingsToggle(
                label = stringResource(R.string.player_ui_new_title),
                description = stringResource(R.string.player_ui_new_subtitle),
                checked = settings.newPlayerUi,
                onCheckedChange = { v -> onUpdate { it.copy(newPlayerUi = v) } },
            )

            val dp: (Int) -> String = { "$it dp" }
            val percent: (Int) -> String = { "$it%" }

            LyricsSettingsSlider(
                label = stringResource(R.string.player_ui_set_top_padding),
                description = "",
                value = settings.playerUiTopPadding,
                range = LyricsStyleSettings.PLAYER_UI_TOP_PADDING_RANGE,
                step = 1,
                valueText = dp,
                onValueChange = { v -> onUpdate { it.copy(playerUiTopPadding = v) } },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.player_ui_set_info_position),
                description = "",
                value = settings.playerUiInfoPosition,
                range = LyricsStyleSettings.PLAYER_UI_INFO_POSITION_RANGE,
                step = 1,
                valueText = dp,
                onValueChange = { v -> onUpdate { it.copy(playerUiInfoPosition = v) } },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.player_ui_set_info_size),
                description = "",
                value = settings.playerUiInfoSize,
                range = LyricsStyleSettings.PLAYER_UI_INFO_SIZE_RANGE,
                step = 1,
                valueText = percent,
                onValueChange = { v -> onUpdate { it.copy(playerUiInfoSize = v) } },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.player_ui_set_flip_end),
                description = "",
                value = settings.playerUiFlipEnd,
                range = LyricsStyleSettings.PLAYER_UI_FLIP_END_RANGE,
                step = 1,
                valueText = percent,
                onValueChange = { v -> onUpdate { it.copy(playerUiFlipEnd = v) } },
            )
        }
    }
}
