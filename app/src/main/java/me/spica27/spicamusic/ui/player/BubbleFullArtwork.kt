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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
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
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.spica27.spicamusic.artwork.MusicArtwork
import me.spica27.spicamusic.ui.widget.AudioCover

/**
 * Full artwork mode: above and below the square album, a flipped copy of the same cover continues the
 * picture. Haze blurs it (amount, optimize and quality come from the Bubble settings) and it fades out.
 * Draws outside the parent box on purpose, so the parent must not clip.
 */
@Composable
internal fun FullArtworkFill(
    artwork: MusicArtwork?,
    side: Dp,
    tuning: BubblePlayerTuning,
) {
    MirrorFill(artwork, side, tuning, above = true)
    MirrorFill(artwork, side, tuning, above = false)
}

@OptIn(ExperimentalHazeApi::class)
@Composable
private fun MirrorFill(
    artwork: MusicArtwork?,
    side: Dp,
    tuning: BubblePlayerTuning,
    above: Boolean,
) {
    val haze = rememberHazeState()
    val amount = tuning.fillBlurAmountDp
    val style =
        remember(amount) {
            HazeBlurStyle {
                blurRadius(amount.dp)
                noiseFactor(0f)
                backgroundColor(Color.Transparent)
                colorEffects(emptyList())
                fallbackColorEffect(HazeColorEffect.tint(Color.Transparent))
                alpha(1f)
            }
        }
    val mode =
        if (tuning.fillBlurOptimize == 1) {
            HazePerformanceMode.Fixed(tuning.fillBlurQuality / 100f)
        } else {
            HazePerformanceMode.Balanced
        }
    // The mirror overlaps the sharp album by 10%, so the sharp edge dissolves into it.
    val shift = side * 0.9f
    Box(
        modifier =
            Modifier
                .requiredSize(side)
                .offset(y = if (above) -shift else shift)
                .fadeOutwards(above),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .hazeSource(haze)
                    .graphicsLayer { scaleY = -1f },
        ) {
            AudioCover(artwork = artwork, modifier = Modifier.fillMaxSize())
        }
        if (amount > 0) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .hazeBlur(
                            input = HazeInput.Sources(haze),
                            style = style,
                            performanceMode = mode,
                            expandLayerBounds = false,
                        ),
            )
        }
    }
}

/** Fades the content out away from the album: opaque next to it, transparent at the far end. */
private fun Modifier.fadeOutwards(above: Boolean): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val brush =
                if (above) {
                    Brush.verticalGradient(
                        0.15f to Color.Transparent,
                        0.65f to Color.Black.copy(alpha = 0.6f),
                        1f to Color.Black,
                    )
                } else {
                    Brush.verticalGradient(
                        0f to Color.Black,
                        0.35f to Color.Black.copy(alpha = 0.6f),
                        0.85f to Color.Transparent,
                    )
                }
            drawRect(brush = brush, blendMode = BlendMode.DstIn)
        }

/** Makes the top and bottom [fraction] of the content fade to transparent. */
internal fun Modifier.fadeVerticalEdges(fraction: Float): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush =
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        fraction to Color.Black,
                        1f - fraction to Color.Black,
                        1f to Color.Transparent,
                    ),
                blendMode = BlendMode.DstIn,
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
