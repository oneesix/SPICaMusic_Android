package me.spica27.spicamusic.ui.player

import android.net.Uri
import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.ExoPlayer
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import me.spica27.spicamusic.artwork.MusicArtwork
import me.spica27.spicamusic.ui.widget.AudioCover

/**
 * Full artwork mode, part 1 (drawn BEFORE the album): the flipped copies above and below the square album.
 * They are Haze sources of the shared [hazeState], as is the album itself, so the top copy, the album and
 * the bottom copy act as one picture. The blur comes from [FullArtworkBlur], drawn after the album.
 * Draws outside the parent box on purpose, so the parent must not clip.
 */
@Composable
internal fun FullArtworkFill(
    artwork: MusicArtwork?,
    side: Dp,
    hazeState: HazeState,
) {
    MirrorCopy(artwork, side, hazeState, above = true)
    MirrorCopy(artwork, side, hazeState, above = false)
}

@Composable
private fun MirrorCopy(
    artwork: MusicArtwork?,
    side: Dp,
    hazeState: HazeState,
    above: Boolean,
) {
    Box(
        modifier =
            Modifier
                .requiredSize(side)
                .offset(y = if (above) -side else side),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
                    .graphicsLayer { scaleY = -1f },
        ) {
            AudioCover(artwork = artwork, modifier = Modifier.fillMaxSize())
        }
    }
}

/**
 * Full artwork mode, part 2 (drawn AFTER the album, before the buttons): two progressive blur layers over
 * the shared source. The top one is fully blurred at the top and fades out inside the album, the bottom one
 * the other way round, so the main cover blurs into the copies with no seam. Amount, optimize and quality
 * come from the Bubble settings.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun FullArtworkBlur(
    side: Dp,
    tuning: BubblePlayerTuning,
    hazeState: HazeState,
) {
    val amount = tuning.fillBlurAmountDp
    if (amount <= 0) return
    val mode =
        if (tuning.fillBlurOptimize == 1) {
            HazePerformanceMode.Fixed(tuning.fillBlurQuality / 100f)
        } else {
            HazePerformanceMode.Balanced
        }
    // How far each blur reaches into the album comes from the Bubble settings (percent of its height).
    val reachTop = side * (tuning.fillReachTopPercent / 100f)
    val reachBottom = side * (tuning.fillReachBottomPercent / 100f)
    BlurBand(hazeState, amount, mode, height = side + reachTop, y = -side, top = true, width = side)
    BlurBand(hazeState, amount, mode, height = side + reachBottom, y = side - reachBottom, top = false, width = side)
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun BlurBand(
    hazeState: HazeState,
    amount: Int,
    mode: HazePerformanceMode,
    height: Dp,
    y: Dp,
    top: Boolean,
    width: Dp,
) {
    val style =
        remember(amount, top) {
            HazeBlurStyle {
                blurRadius(amount.dp)
                noiseFactor(0f)
                backgroundColor(Color.Transparent)
                colorEffects(emptyList())
                fallbackColorEffect(HazeColorEffect.tint(Color.Transparent))
                alpha(1f)
                progressive(
                    HazeProgressive.verticalGradient(
                        startIntensity = if (top) 1f else 0f,
                        endIntensity = if (top) 0f else 1f,
                    ),
                )
            }
        }
    Box(
        modifier =
            Modifier
                .requiredSize(width = width, height = height)
                .offset(y = y)
                .hazeBlur(
                    input = HazeInput.Sources(hazeState),
                    style = style,
                    performanceMode = mode,
                    expandLayerBounds = false,
                ),
    )
}

/**
 * Silent, looping video cover. It keeps the video's own shape (no zoom or crop) and plays only while the
 * music plays. It uses its own small player, so it never touches the music player.
 */
@Composable
internal fun ArtworkVideoCover(
    uri: Uri,
    playing: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var ratio by remember(uri) { mutableFloatStateOf(1f) }
    val player =
        remember(uri) {
            ExoPlayer.Builder(context).build().apply {
                volume = 0f
                repeatMode = Player.REPEAT_MODE_ALL
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
            }
        }
    DisposableEffect(player) {
        val listener =
            object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        ratio = videoSize.width * videoSize.pixelWidthHeightRatio / videoSize.height
                    }
                }
            }
        player.addListener(listener)
        onDispose {
            player.removeListener(listener)
            player.release()
        }
    }
    LaunchedEffect(player, playing) { player.playWhenReady = playing }
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        AndroidView(
            factory = { ctx -> TextureView(ctx).also { player.setVideoTextureView(it) } },
            update = { view -> player.setVideoTextureView(view) },
            modifier = Modifier.aspectRatio(ratio),
        )
    }
}
