package me.spica27.spicamusic.ui.dialog

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.spica27.spicamusic.R
import me.spica27.spicamusic.automix.AutoMixFailReason
import me.spica27.spicamusic.automix.AutoMixSkipReason
import me.spica27.spicamusic.automix.AutoMixTransfer
import me.spica27.spicamusic.automix.AutoMixTransferState
import me.spica27.spicamusic.ui.component.DialogContainer
import me.spica27.spicamusic.ui.navigation.LocalBackStack
import me.spica27.spicamusic.ui.theme.Spacing

/** Progress / result dialog shared by "Export for AutoMix" and "Import AutoMix". */
@Composable
fun AutoMixTransferDialogContent() {
    val backStack = LocalBackStack.current
    val context = LocalContext.current
    val state by AutoMixTransfer.state.collectAsStateWithLifecycle()

    val close: () -> Unit = {
        AutoMixTransfer.reset()
        backStack.removeLastOrNull()
    }
    val cancel: () -> Unit = {
        AutoMixTransfer.cancel()
        backStack.removeLastOrNull()
    }

    DialogContainer {
        Column(
            modifier = Modifier.padding(Spacing.ExtraLarge),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large),
        ) {
            when (val s = state) {
                is AutoMixTransferState.Idle -> {
                    // Nothing running (for example just cancelled): offer a way out.
                    ButtonRow { TextButton(onClick = close) { Text(stringResource(R.string.automix_done)) } }
                }

                is AutoMixTransferState.Exporting -> {
                    DialogTitle(stringResource(R.string.automix_exporting_title))
                    if (s.total > 0) {
                        LinearProgressIndicator(
                            progress = { s.done.toFloat() / s.total },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Text(
                            text =
                                stringResource(
                                    R.string.automix_exporting_progress,
                                    minOf(s.done + 1, s.total),
                                    s.total,
                                ),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            text = stringResource(R.string.automix_preparing),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    ButtonRow { TextButton(onClick = cancel) { Text(stringResource(R.string.cancel)) } }
                }

                is AutoMixTransferState.ExportDone -> {
                    DialogTitle(stringResource(R.string.automix_export_done_title))
                    Text(
                        text = stringResource(R.string.automix_export_done_location, s.fileName),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ButtonRow {
                        TextButton(
                            onClick = {
                                val send =
                                    Intent(Intent.ACTION_SEND).apply {
                                        type = "application/zip"
                                        putExtra(Intent.EXTRA_STREAM, s.uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                context.startActivity(
                                    Intent
                                        .createChooser(send, null)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                )
                            },
                        ) { Text(stringResource(R.string.automix_share)) }
                        TextButton(onClick = close) { Text(stringResource(R.string.automix_done)) }
                    }
                }

                is AutoMixTransferState.Importing -> {
                    DialogTitle(stringResource(R.string.automix_importing_title))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text(
                        text = stringResource(R.string.automix_importing_progress),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ButtonRow { TextButton(onClick = cancel) { Text(stringResource(R.string.cancel)) } }
                }

                is AutoMixTransferState.ImportDone -> {
                    DialogTitle(stringResource(R.string.automix_importing_title))
                    Text(
                        text = stringResource(R.string.automix_import_summary, s.importedCount, s.skippedCount),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Column(
                        modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small),
                    ) {
                        s.lines.forEach { line ->
                            val reason =
                                when (line.reason) {
                                    AutoMixSkipReason.SongNotFound -> stringResource(R.string.automix_reason_song_not_found)
                                    AutoMixSkipReason.ClipMissing -> stringResource(R.string.automix_reason_clip_missing)
                                    null -> null
                                }
                            Text(
                                text =
                                    (if (line.ok) "✓ " else "✕ ") + line.title +
                                        (if (reason != null) " — $reason" else ""),
                                style = MaterialTheme.typography.bodyMedium,
                                color =
                                    if (line.ok) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                            )
                        }
                    }
                    Text(
                        text = stringResource(R.string.automix_import_storage_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ButtonRow { TextButton(onClick = close) { Text(stringResource(R.string.automix_done)) } }
                }

                is AutoMixTransferState.Failed -> {
                    DialogTitle(stringResource(R.string.automix_failed_title))
                    Text(
                        text =
                            when (s.reason) {
                                AutoMixFailReason.EmptyPlaylist -> stringResource(R.string.automix_fail_empty)
                                AutoMixFailReason.InvalidZip -> stringResource(R.string.automix_fail_invalid_zip)
                                AutoMixFailReason.Generic -> stringResource(R.string.automix_fail_generic, s.detail)
                            },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ButtonRow { TextButton(onClick = close) { Text(stringResource(R.string.automix_done)) } }
                }
            }
        }
    }
}

@Composable
private fun DialogTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

@Composable
private fun ButtonRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        content()
    }
}
