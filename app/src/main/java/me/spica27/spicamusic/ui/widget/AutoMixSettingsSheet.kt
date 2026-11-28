package me.spica27.spicamusic.ui.widget

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.spica27.spicamusic.R
import me.spica27.spicamusic.automix.AutoMixMode
import me.spica27.spicamusic.automix.AutoMixSettings
import me.spica27.spicamusic.automix.MixingLibrary

/**
 * "AutoMix settings" sheet, opened from the three-dots menu of the lyrics window.
 *
 * Mode (auto / manual), where to look for the mix points, how many beats the blend lasts,
 * the vocal timing source, and the state of the Music/mixing folder.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoMixSettingsSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    remember(context) { AutoMixSettings.init(context) }
    val settings by AutoMixSettings.state.collectAsStateWithLifecycle()
    val songs by MixingLibrary.songs.collectAsStateWithLifecycle()
    val scanned by MixingLibrary.scanned.collectAsStateWithLifecycle()
    var hasAccess by remember { mutableStateOf(MixingLibrary.hasAllFilesAccess()) }

    LaunchedEffect(Unit) { MixingLibrary.refresh() }

    // Back from the system "All files access" screen: check again and rescan.
    val accessLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            hasAccess = MixingLibrary.hasAllFilesAccess()
            scope.launch { MixingLibrary.refresh() }
        }

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
            Text(
                text = stringResource(R.string.amx_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            LyricsSettingsSection(stringResource(R.string.amx_section_mix_points))
            LyricsSettingsChips(
                label = stringResource(R.string.amx_mode_label),
                description = stringResource(R.string.amx_mode_desc),
                options = AutoMixMode.entries.toList(),
                selected = settings.mode,
                optionLabel = {
                    when (it) {
                        AutoMixMode.Auto -> stringResource(R.string.amx_mode_auto)
                        AutoMixMode.Manual -> stringResource(R.string.amx_mode_manual)
                    }
                },
                onSelect = { mode -> AutoMixSettings.update(context) { it.copy(mode = mode) } },
            )
            if (settings.mode == AutoMixMode.Manual) {
                LyricsSettingsSlider(
                    label = stringResource(R.string.amx_range_out_label),
                    description = stringResource(R.string.amx_range_out_desc),
                    value = settings.outgoingRangePercent,
                    range = AutoMixSettings.RANGE_PERCENT_RANGE,
                    step = 5,
                    valueText = { "$it%" },
                    onValueChange = { v -> AutoMixSettings.update(context) { it.copy(outgoingRangePercent = v) } },
                )
                LyricsSettingsSlider(
                    label = stringResource(R.string.amx_range_in_label),
                    description = stringResource(R.string.amx_range_in_desc),
                    value = settings.incomingRangePercent,
                    range = AutoMixSettings.RANGE_PERCENT_RANGE,
                    step = 5,
                    valueText = { "$it%" },
                    onValueChange = { v -> AutoMixSettings.update(context) { it.copy(incomingRangePercent = v) } },
                )
            }

            LyricsSettingsSection(stringResource(R.string.amx_section_blend))
            LyricsSettingsChips(
                label = stringResource(R.string.amx_blend_label),
                description = stringResource(R.string.amx_blend_desc),
                options = AutoMixSettings.BLEND_BEATS_OPTIONS,
                selected = settings.blendBeats,
                optionLabel = { stringResource(R.string.amx_beats_format, it) },
                onSelect = { beats -> AutoMixSettings.update(context) { it.copy(blendBeats = beats) } },
            )

            LyricsSettingsSection(stringResource(R.string.amx_section_vocals))
            LyricsSettingsToggle(
                label = stringResource(R.string.amx_ttml_label),
                description = stringResource(R.string.amx_ttml_desc),
                checked = settings.useTtmlVocalTiming,
                onCheckedChange = { on -> AutoMixSettings.update(context) { it.copy(useTtmlVocalTiming = on) } },
            )

            LyricsSettingsSection(stringResource(R.string.amx_section_library))
            Text(
                text = stringResource(R.string.amx_library_folder, MixingLibrary.FOLDER_PATH),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            val ready = songs.count { it.stemsComplete }
            val status =
                when {
                    !MixingLibrary.isSupported() -> stringResource(R.string.amx_library_need_android11)
                    !hasAccess -> stringResource(R.string.amx_library_need_access)
                    !scanned -> stringResource(R.string.amx_library_scanning)
                    songs.isEmpty() -> stringResource(R.string.amx_library_empty)
                    else -> stringResource(R.string.amx_library_count, songs.size, ready)
                }
            Text(
                text = status,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (MixingLibrary.isSupported() && !hasAccess) {
                    Button(onClick = { accessLauncher.launch(MixingLibrary.accessSettingsIntent(context)) }) {
                        Text(stringResource(R.string.amx_library_allow))
                    }
                }
                TextButton(
                    onClick = {
                        hasAccess = MixingLibrary.hasAllFilesAccess()
                        scope.launch { MixingLibrary.refresh() }
                    },
                ) {
                    Text(stringResource(R.string.amx_library_refresh))
                }
            }
        }
    }
}
