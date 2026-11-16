package me.spica27.spicamusic.ui.player

import android.net.Uri
import android.view.TextureView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.spica27.spicamusic.artwork.MusicArtwork
import me.spica27.spicamusic.ui.widget.AudioCover

/** How much of the album (top and bottom) fades into the blur, as a share of its height. */
private const val FADE_FRACTION = 0.35f

/**
 * Full artwork mode, one blur layer: a stack of three covers (flipped, normal, flipped) is blurred once
 * by Haze. The sharp album is drawn on top and fades out at its top and bottom (see [fullArtworkFade]),
 * so it melts into the blurred stack with no seam and no strip line. Amount, optimize and quality come
 * from the Bubble settings. It replaces the player background (not the lyrics background). Draws outside the parent box on purpose, so the parent must not clip.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun FullArtworkFill(
    artwork: MusicArtwork?,
    side: Dp,
    tuning: BubblePlayerTuning,
    hazeState: HazeState,
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
    Box(
        modifier =
            Modifier
                .requiredSize(width = side, height = side * 3)
                .offset(y = -side)
                // The player bars blur this stack, since the dynamic background is not drawn here.
                .hazeSource(hazeState),
    ) {
        Column(modifier = Modifier.fillMaxSize().hazeSource(haze)) {
            StackCover(artwork, side, flipped = true)
            StackCover(artwork, side, flipped = false)
            StackCover(artwork, side, flipped = true)
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

@Composable
private fun StackCover(
    artwork: MusicArtwork?,
    side: Dp,
    flipped: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(side)
                .graphicsLayer { if (flipped) scaleY = -1f },
    ) {
        AudioCover(artwork = artwork, modifier = Modifier.fillMaxSize())
    }
}

/** Fades the album to transparent at its top and bottom edge, so the blur below shows through. */
internal fun Modifier.fullArtworkFade(): Modifier =
    graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            drawRect(
                brush =
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        FADE_FRACTION to Color.Black,
                        1f - FADE_FRACTION to Color.Black,
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
