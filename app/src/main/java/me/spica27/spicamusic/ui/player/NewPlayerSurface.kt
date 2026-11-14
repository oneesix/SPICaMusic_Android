package me.spica27.spicamusic.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import me.spica27.spicamusic.R
import me.spica27.spicamusic.artwork.MusicArtwork
import me.spica27.spicamusic.artwork.artwork
import me.spica27.spicamusic.player.api.PlayMode
import me.spica27.spicamusic.ui.widget.AudioCover
import me.spica27.spicamusic.ui.widget.rememberLyricsEdgePadding
import org.koin.compose.viewmodel.koinActivityViewModel

private val NewPlayerControlButtonSize = 72.dp
private val NewPlayerControlIconSize = 58.dp

/**
 * New player UI (settings: "New player UI"). It shares the top bar and the bottom bar with the lyrics page:
 *
 * - Top bar: playlist name (or "Now playing") and the play time instead of the artist line.
 * - Album art: a full width square without rounded corners. A flipped copy fills the space above it (behind
 *   the top bar) and below it, so the existing top bar / bottom bar blur settings blur the cover.
 * - Under the art: song title and artist, the current lyric line, and the previous / pause / next buttons.
 * - Bottom bar: mix | next track pill (next cover, "Next track", next title, queue button) | repeat.
 */
@Composable
internal fun NewPlayerSurface(
    playerViewModel: PlayerViewModel,
    hazeState: HazeState,
    title: String,
    artist: String,
    artwork: MusicArtwork?,
    isLike: Boolean,
    onLikeClick: () -> Unit,
    isSeeking: Boolean,
    seekPositionProvider: () -> Float,
    durationMs: Long,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    progressAnimationsEnabled: Boolean,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onQueueClick: () -> Unit,
    onLyricsClick: () -> Unit,
    modifier: Modifier = Modifier,
    /** A swipe on the album art opens the lyrics, like on the original player page. */
    coverSwipeModifier: Modifier = Modifier,
) {
    val styleViewModel: LyricsStyleViewModel = koinActivityViewModel()
    val style by styleViewModel.style.collectAsStateWithLifecycle()
    val playMode by playerViewModel.playMode.collectAsStateWithLifecycle()
    val queue by playerViewModel.currentPlaylist.collectAsStateWithLifecycle()
    val queueIndex by playerViewModel.currentMediaItemIndex.collectAsStateWithLifecycle()
    val source by PlaybackSourceHolder.source.collectAsStateWithLifecycle()

    // Playlist name only while the queue is still that playlist; otherwise "Now playing".
    val playlistName =
        remember(source, queue) {
            source?.takeIf { it.matches(queue.map { item -> item.mediaId }) }?.name
        }
    val headerTitle = playlistName ?: stringResource(R.string.player_ui_now_playing)

    // The next track of the queue (wraps to the first one).
    val nextItem =
        remember(queue, queueIndex) {
            if (queue.isEmpty()) null else queue[(queueIndex + 1).mod(queue.size)]
        }
    val nextTitle = nextItem?.mediaMetadata?.title?.toString() ?: stringResource(R.string.unknown_song)
    val nextArtwork = nextItem?.mediaMetadata?.artwork()

    // Same blur switches as the lyrics page (the existing top bar / bottom bar blur settings).
    val blurBehind = style.enhanceHeader && style.headerBlurEnabled
    val dockSpread = style.enhanceHeader && style.dockBlurEnabled && style.dockBlurAmount > 0 && style.dockSpreadBlur

    val density = LocalDensity.current
    var barHeightPx by remember { mutableIntStateOf(0) }
    var dockHeightPx by remember { mutableIntStateOf(0) }
    val barHeight = with(density) { barHeightPx.toDp() }
    val dockHeight = with(density) { dockHeightPx.toDp() }
    val edge = rememberLyricsEdgePadding()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val artSize = maxWidth
        val belowArt = (maxHeight - barHeight - artSize).coerceAtLeast(0.dp)
        val flipBottomHeight = belowArt * (style.playerUiFlipEnd / 100f)

        // Album art block. It is a blur source, so the top bar / bottom bar blur settings apply to it.
        Column(modifier = Modifier.fillMaxSize().hazeSource(hazeState, zIndex = 1f)) {
            FlippedCover(artwork = artwork, size = artSize, height = barHeight, atTop = true)
            Box(modifier = Modifier.size(artSize).then(coverSwipeModifier)) {
                AudioCover(artwork = artwork, modifier = Modifier.fillMaxSize())
            }
            FlippedCover(artwork = artwork, size = artSize, height = flipBottomHeight, atTop = false)
        }

        // Song info, lyric line and buttons. The bottom bar is not moved: this area ends above it.
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = barHeight + artSize, bottom = dockHeight),
        ) {
            val scale = style.playerUiInfoSize / 100f
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .offset(y = style.playerUiInfoPosition.dp)
                        .padding(start = edge, end = edge, top = 12.dp),
            ) {
                Text(
                    text = title,
                    fontSize = (26f * scale).sp,
                    lineHeight = (32f * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = artist,
                    fontSize = (17f * scale).sp,
                    lineHeight = (22f * scale).sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                // Only the current lyric line. A tap opens the lyrics page.
                MiniLyric(
                    onClick = onLyricsClick,
                    textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 20.sp),
                    textAlign = TextAlign.Start,
                    textColor = MaterialTheme.colorScheme.onSurface,
                    horizontalPadding = edge,
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = edge),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onPreviousClick, modifier = Modifier.size(NewPlayerControlButtonSize)) {
                        Icon(
                            imageVector = Icons.Rounded.FastRewind,
                            contentDescription = stringResource(R.string.previous_track),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(NewPlayerControlIconSize),
                        )
                    }
                    IconButton(onClick = onPlayPauseClick, modifier = Modifier.size(NewPlayerControlButtonSize)) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = stringResource(if (isPlaying) R.string.pause else R.string.play),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(NewPlayerControlIconSize),
                        )
                    }
                    IconButton(onClick = onNextClick, modifier = Modifier.size(NewPlayerControlButtonSize)) {
                        Icon(
                            imageVector = Icons.Rounded.FastForward,
                            contentDescription = stringResource(R.string.next_track),
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(NewPlayerControlIconSize),
                        )
                    }
                }
            }
        }

        // Top bar blur layer (same as the lyrics page), then the shared top bar.
        if (blurBehind && style.headerBlurAmount > 0) {
            LyricsHeaderBlurLayer(
                hazeState = hazeState,
                blurRadius = style.headerBlurAmount.dp,
                barHeight = barHeight,
                fade = if (style.headerSoftEdge) style.headerEdgeFade.dp else 0.dp,
                optimizeQuality = if (style.headerBlurOptimize) style.headerBlurQuality else null,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        FullscreenLyricsHeader(
            title = title,
            artist = artist,
            artwork = artwork,
            isLike = isLike,
            onLikeClick = onLikeClick,
            album = null,
            releaseYear = null,
            isSeeking = isSeeking,
            seekPositionProvider = seekPositionProvider,
            durationMs = durationMs,
            onSeekChange = onSeekChange,
            onSeekFinished = onSeekFinished,
            progressAnimationsEnabled = progressAnimationsEnabled,
            onBarHeightChange = { barHeightPx = it },
            modifier = Modifier.align(Alignment.TopCenter),
            titleOverride = headerTitle,
            timeAsInfo = true,
            verticalPaddingOverride = style.playerUiTopPadding.dp,
        )

        // Bottom bar "spread" blur layer (same as the lyrics page), then the shared bottom bar.
        if (dockSpread) {
            LyricsHeaderBlurLayer(
                hazeState = hazeState,
                blurRadius = style.dockBlurAmount.dp,
                barHeight = dockHeight,
                fade = if (style.dockSoftEdge) style.dockEdgeFade.dp else 0.dp,
                fadeOnTop = true,
                optimizeQuality = if (style.dockBlurOptimize) style.dockBlurQuality else null,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        LyricsBottomBar(
            artist = stringResource(R.string.player_ui_next_track),
            artwork = nextArtwork,
            isPlaying = isPlaying,
            onPlayPauseClick = onPlayPauseClick,
            seekPositionProvider = seekPositionProvider,
            durationMs = durationMs,
            hazeState = hazeState,
            onHeightChange = { dockHeightPx = it },
            modifier = Modifier.align(Alignment.BottomCenter),
            player =
                PlayerDockState(
                    nextTitle = nextTitle,
                    playMode = playMode,
                    onMixClick = {
                        playerViewModel.setPlayMode(
                            if (playMode == PlayMode.SHUFFLE) PlayMode.LOOP else PlayMode.SHUFFLE,
                        )
                    },
                    onRepeatClick = {
                        playerViewModel.setPlayMode(
                            if (playMode == PlayMode.LIST) PlayMode.LOOP else PlayMode.LIST,
                        )
                    },
                    onQueueClick = onQueueClick,
                ),
        )
    }
}

/**
 * A flipped copy of the album art: [atTop] = the mirror image above the art (its bottom edge touches the art),
 * otherwise the mirror image below it (its top edge touches the art). Plain copy: no blur and no shadow,
 * the blur comes from the existing top bar / bottom bar blur settings.
 */
@Composable
private fun FlippedCover(
    artwork: MusicArtwork?,
    size: Dp,
    height: Dp,
    atTop: Boolean,
) {
    Box(
        modifier =
            Modifier
                .width(size)
                .height(height)
                .clipToBounds(),
    ) {
        AudioCover(
            artwork = artwork,
            modifier =
                Modifier
                    .align(if (atTop) Alignment.BottomCenter else Alignment.TopCenter)
                    .requiredSize(size, size)
                    .graphicsLayer { scaleY = -1f },
        )
    }
}
