package me.spica27.spicamusic.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import me.spica27.spicamusic.R
import me.spica27.spicamusic.artwork.MusicArtwork
import me.spica27.spicamusic.player.api.PlayMode
import me.spica27.spicamusic.ui.widget.AudioCover
import me.spica27.spicamusic.ui.widget.rememberLyricsEdgePadding
import org.koin.compose.viewmodel.koinActivityViewModel
import java.util.Locale

/**
 * Bottom bar of the lyrics page:
 * [transliteration button] [pill: cover | artist + time | play/pause] [translation button].
 *
 * The two side buttons turn the transliteration / translation lines on and off. When the lyrics
 * have no such lines the icon is faded and tapping does nothing.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun LyricsBottomBar(
    artist: String,
    artwork: MusicArtwork?,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    seekPositionProvider: () -> Float,
    durationMs: Long,
    hazeState: HazeState,
    /** Height of the whole bar in px, so the "spread" blur layer can match it. */
    onHeightChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    /** New player UI: mix | next track pill with queue button | repeat, instead of the lyrics buttons. */
    player: PlayerDockState? = null,
) {
    val viewModel: LyricsViewModel = koinActivityViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val styleViewModel: LyricsStyleViewModel = koinActivityViewModel()
    val s by styleViewModel.style.collectAsStateWithLifecycle()

    // Re-evaluated only when the lyrics themselves change.
    val items = uiState.displayed?.items
    val hasTransliteration = remember(items) { items?.hasTransliteration() == true }
    val hasTranslation = remember(items) { items?.hasTranslation() == true }

    val edge = rememberLyricsEdgePadding()
    val size = s.dockSize.dp
    val pad = s.dockPillPadding.dp
    val inner = (size - pad * 2).coerceAtLeast(16.dp)

    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    // Blur on: translucent fill over the blurred lyrics. Blur off: a solid colour, never transparent.
    val fill = if (s.dockBlurEnabled) onSurface.copy(alpha = 0.14f) else lerp(surface, onSurface, 0.18f)
    val innerFill = if (s.dockBlurEnabled) onSurface.copy(alpha = 0.16f) else lerp(surface, onSurface, 0.28f)

    val blurAmount = s.dockBlurAmount
    val blurStyle =
        remember(blurAmount) {
            HazeBlurStyle {
                blurRadius(blurAmount.dp)
                noiseFactor(0f)
                backgroundColor(Color.Transparent)
                colorEffects(emptyList())
                fallbackColorEffect(HazeColorEffect.tint(Color.Transparent))
                alpha(1f)
            }
        }
    // Normal: every button and the pill blur on their own. Spread: one blur layer behind the whole bar
    // (drawn by the lyrics surface), so nothing blurs here.
    val glass: Modifier =
        if (s.dockBlurEnabled && blurAmount > 0 && !s.dockSpreadBlur) {
            Modifier.hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = blurStyle,
                performanceMode =
                    if (s.dockBlurOptimize) {
                        HazePerformanceMode.Fixed(s.dockBlurQuality / 100f)
                    } else {
                        HazePerformanceMode.Balanced
                    },
                expandLayerBounds = false,
            )
        } else {
            Modifier
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .onSizeChanged { onHeightChange(it.height) }
                .navigationBarsPadding()
                .padding(start = edge, end = edge, top = 10.dp, bottom = s.dockBottomPadding.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Left: transliteration (new player UI: mix)
        if (player != null) {
            DockCircleButton(
                size = size,
                fill = fill,
                glass = glass,
                on = player.playMode == PlayMode.SHUFFLE,
                available = true,
                description = stringResource(R.string.player_ui_mix),
                onClick = player.onMixClick,
            ) { tint ->
                Icon(
                    imageVector = Icons.Rounded.Shuffle,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(size * 0.46f),
                )
            }
        } else DockCircleButton(
            size = size,
            fill = fill,
            glass = glass,
            on = s.showTransliteration && hasTransliteration,
            available = hasTransliteration,
            description = stringResource(R.string.lyr_dock_transliteration),
            onClick = {
                if (hasTransliteration) {
                    styleViewModel.update { it.copy(showTransliteration = !it.showTransliteration) }
                }
            },
        ) { tint ->
            Text(
                text = "Aa",
                color = tint,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.33f).sp,
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.width(s.dockGapLeft.dp))

        // Middle: pill
        Row(
            modifier =
                Modifier
                    .weight(1f)
                    .height(size)
                    .clip(CircleShape)
                    .then(glass)
                    .background(fill)
                    .padding(pad),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(inner)
                        .clip(CircleShape)
                        .background(onSurface.copy(alpha = 0.08f)),
            ) {
                AudioCover(
                    artwork = artwork,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                val artistSize = (size.value * 0.27f).sp
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = artistSize,
                    lineHeight = artistSize * 1.2f,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (player != null) {
                    // New player UI: the title of the next track
                    val nextSize = (size.value * 0.23f).sp
                    Text(
                        text = player.nextTitle,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = nextSize,
                        lineHeight = nextSize * 1.2f,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    LyricsDockTime(
                        seekPositionProvider = seekPositionProvider,
                        durationMs = durationMs,
                        fontSize = (size.value * 0.23f).sp,
                    )
                }
            }

            Box(
                modifier =
                    Modifier
                        .size(inner)
                        .clip(CircleShape)
                        .background(innerFill)
                        .clickable(role = Role.Button, onClick = player?.onQueueClick ?: onPlayPauseClick),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector =
                        when {
                            player != null -> Icons.AutoMirrored.Filled.PlaylistPlay
                            isPlaying -> Icons.Rounded.Pause
                            else -> Icons.Rounded.PlayArrow
                        },
                    contentDescription =
                        if (player != null) {
                            stringResource(R.string.player_ui_queue)
                        } else {
                            stringResource(if (isPlaying) R.string.pause else R.string.play)
                        },
                    tint = onSurface,
                    modifier = Modifier.size(inner / 2),
                )
            }
        }

        Spacer(modifier = Modifier.width(s.dockGapRight.dp))

        // Right: translation (new player UI: repeat)
        if (player != null) {
            DockCircleButton(
                size = size,
                fill = fill,
                glass = glass,
                on = player.playMode == PlayMode.LIST,
                available = true,
                description = stringResource(R.string.player_ui_repeat),
                onClick = player.onRepeatClick,
            ) { tint ->
                Icon(
                    imageVector = if (player.playMode == PlayMode.LIST) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(size * 0.46f),
                )
            }
        } else DockCircleButton(
            size = size,
            fill = fill,
            glass = glass,
            on = s.showTranslation && hasTranslation,
            available = hasTranslation,
            description = stringResource(R.string.lyr_dock_translation),
            onClick = {
                if (hasTranslation) {
                    styleViewModel.update { it.copy(showTranslation = !it.showTranslation) }
                }
            },
        ) { tint ->
            Icon(
                imageVector = Icons.Rounded.Translate,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(size * 0.46f),
            )
        }
    }
}

/** New player UI: what the bottom bar shows (and does) instead of the lyrics buttons. */
internal class PlayerDockState(
    val nextTitle: String,
    val playMode: PlayMode,
    val onMixClick: () -> Unit,
    val onRepeatClick: () -> Unit,
    val onQueueClick: () -> Unit,
)

/**
 * Round button. The circle never changes; only the icon / text does:
 * on = full strength, off = dimmed, not available = faded more (and tapping does nothing).
 */
@Composable
private fun DockCircleButton(
    size: Dp,
    fill: Color,
    glass: Modifier,
    on: Boolean,
    available: Boolean,
    description: String,
    onClick: () -> Unit,
    content: @Composable (tint: Color) -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val tint =
        when {
            !available -> onSurface.copy(alpha = 0.35f)
            on -> onSurface
            else -> onSurface.copy(alpha = 0.55f)
        }
    Box(
        modifier =
            Modifier
                .size(size)
                .clip(CircleShape)
                .then(glass)
                .background(fill)
                .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content(tint)
    }
}

/** "01:30 / 02:55". Only this small composable reads the position, so the bar doesn't recompose per tick. */
@Composable
private fun LyricsDockTime(
    seekPositionProvider: () -> Float,
    durationMs: Long,
    fontSize: TextUnit,
) {
    Text(
        text = "${clock(seekPositionProvider().toLong())} / ${clock(durationMs)}",
        style = MaterialTheme.typography.bodySmall.copy(fontFeatureSettings = "tnum"),
        fontSize = fontSize,
        lineHeight = fontSize * 1.2f,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun clock(ms: Long): String {
    val total = ms.coerceAtLeast(0L) / 1000L
    return String.format(Locale.US, "%02d:%02d", total / 60L, total % 60L)
}
