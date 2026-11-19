package me.spica27.spicamusic.ui.player

import me.spica27.spicamusic.ui.motion.AnimatedContent
import androidx.compose.animation.core.Spring
import me.spica27.spicamusic.ui.motion.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FastForward
import androidx.compose.material.icons.rounded.FastRewind
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import me.spica27.spicamusic.R
import me.spica27.spicamusic.artwork.artwork
import me.spica27.spicamusic.player.api.PlayMode
import me.spica27.spicamusic.ui.widget.AudioCover
import me.spica27.spicamusic.ui.widget.audio_seekbar.PillRingGeometry
import me.spica27.spicamusic.ui.widget.audio_seekbar.PillRingSeekBar
import me.spica27.spicamusic.ui.widget.audio_seekbar.pillRingSeek
import me.spica27.spicamusic.ui.widget.clickHighlight
import me.spica27.spicamusic.ui.widget.materialSharedAxisYIn
import me.spica27.spicamusic.ui.widget.materialSharedAxisYOut
import androidx.compose.animation.togetherWith
import kotlin.math.max
import kotlin.math.min

private val RingMargin = 30.dp
private val SideInset = 20.dp
private val CircleButtonSize = 44.dp

/**
 * Pill player style: a tall flat album with a ring progress bar around its bottom, previous /
 * play-pause / next on one row, and mix + like just above the lyrics bar. Flat on purpose: no
 * blur and no depth effects (the liquid glass and background settings are handled elsewhere).
 */
@Composable
internal fun PillPlayerPage(
    currentMediaItem: () -> MediaItem?,
    seekPositionProvider: () -> Float,
    realPositionProvider: () -> Float,
    duration: Long,
    isPlaying: Boolean,
    isLike: Boolean,
    playMode: PlayMode,
    isAppInForeground: Boolean,
    animationsEnabled: Boolean,
    tuning: PillPlayerTuning,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayModeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCollapse: () -> Unit,
    onPlaylistClick: () -> Unit,
    onLyricsClick: () -> Unit,
    modifier: Modifier = Modifier,
    coverSwipeModifier: Modifier = Modifier,
) {
    val item = currentMediaItem()
    val metadata = item?.mediaMetadata
    val artist =
        metadata?.artist?.toString()?.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.unknown_artist)
    val title = metadata?.title?.toString() ?: stringResource(R.string.unknown_song)
    val albumTitle = metadata?.albumTitle?.toString()?.takeIf { it.isNotBlank() }
    val year = metadata?.releaseYear ?: metadata?.recordingYear
    val onSurface = MaterialTheme.colorScheme.onSurface

    BoxWithConstraints(modifier = modifier.navigationBarsPadding()) {
        val pageWidth = maxWidth.value
        val pageHeight = maxHeight.value
        val ringPad = tuning.ringPaddingDp
        val outerPad = max(ringPad, 0).toFloat()

        // Keep the album clear of the skip buttons and inside the screen.
        val skipSpace = tuning.skipSideDp + tuning.skipSizeDp + 8
        val maxAlbumWidth = max(120f, pageWidth - 2f * (skipSpace + outerPad))
        val albumWidth = min(tuning.albumWidthDp.toFloat(), maxAlbumWidth)
        val radius = min(tuning.bottomRadiusDp.toFloat(), albumWidth / 2f)
        val headerHeight = if (year != null) 40f else 0f
        // top bar + info + bottom actions + lyrics bar
        val reserved = 64f + 96f + 44f + 12f + max(0, tuning.actionsHeightDp) + 46f
        val albumHeight =
            max(160f, min(tuning.albumHeightDp.toFloat(), pageHeight - reserved - headerHeight - outerPad))

        val playTop = headerHeight + albumHeight - tuning.playHeightDp - tuning.playSizeDp
        val playCenter = playTop + tuning.playSizeDp / 2f
        val skipTop = playCenter + tuning.skipHeightDp - tuning.skipSizeDp / 2f
        val stageHeight = headerHeight + albumHeight + outerPad + 4f

        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar: back and queue.
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SideInset)
                        .padding(top = 10.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillCircleButton(onClick = onCollapse) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }
                PillCircleButton(onClick = onPlaylistClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = stringResource(R.string.queue),
                        tint = onSurface,
                        modifier = Modifier.size(26.dp),
                    )
                }
            }

            // Album, ring, transport row.
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(stageHeight.dp)
                        .then(coverSwipeModifier),
            ) {
                if (year != null) {
                    Box(
                        modifier =
                            Modifier
                                .align(Alignment.TopCenter)
                                .width(albumWidth.dp)
                                .height(32.dp)
                                .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp),
                        )
                    }
                }

                val density = LocalDensity.current
                val geometry =
                    remember(albumWidth, albumHeight, radius, ringPad, density) {
                        with(density) {
                            PillRingGeometry(
                                originX = RingMargin.toPx(),
                                albumWidth = albumWidth.dp.toPx(),
                                albumHeight = albumHeight.dp.toPx(),
                                radius = radius.dp.toPx(),
                                pad = ringPad.dp.toPx(),
                            )
                        }
                    }
                val slopPx = with(density) { 28.dp.toPx() }
                val progress =
                    if (duration > 0) (seekPositionProvider() / duration).coerceIn(0f, 1f) else 0f

                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = headerHeight.dp)
                            .size(width = (albumWidth + 2 * RingMargin.value).dp, height = (albumHeight + RingMargin.value).dp)
                            .pillRingSeek(
                                geometry = geometry,
                                slopPx = slopPx,
                                onChange = onValueChange,
                                onFinished = onValueChangeFinished,
                            ),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .offset(x = RingMargin)
                                .size(width = albumWidth.dp, height = albumHeight.dp)
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 6.dp,
                                        topEnd = 6.dp,
                                        bottomEnd = radius.dp,
                                        bottomStart = radius.dp,
                                    ),
                                ),
                    ) {
                        AnimatedContent(
                            targetState = item,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                materialSharedAxisYIn(true) togetherWith materialSharedAxisYOut(true)
                            },
                            contentKey = { it?.mediaId ?: "-1" },
                            label = "pillCover",
                        ) { mediaItem ->
                            AudioCover(
                                artwork = mediaItem?.mediaMetadata?.artwork(),
                                modifier =
                                    Modifier
                                        .fillMaxSize()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                        .clickHighlight(onClick = onLyricsClick),
                            )
                        }
                    }
                    PillRingSeekBar(
                        geometry = geometry,
                        progress = progress,
                        animationsEnabled = animationsEnabled && isPlaying && isAppInForeground,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                // Album name, vertical, to the right of the album and above the skip row.
                val nameLength = skipTop - headerHeight - 8f
                if (albumTitle != null && nameLength > 40f) {
                    Text(
                        text = albumTitle,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier =
                            Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (pageWidth / 2f + albumWidth / 2f + outerPad + 8f).dp,
                                    y = headerHeight.dp,
                                ).widthIn(max = nameLength.dp)
                                .verticalText(),
                    )
                }

                // Previous, next and their times (outside the album, same row as play/pause).
                val remaining = (duration - realPositionProvider().toLong()).coerceAtLeast(0L)
                PillSkipColumn(
                    icon = Icons.Rounded.FastRewind,
                    description = stringResource(R.string.previous_track),
                    size = tuning.skipSizeDp.dp,
                    time = formatTime(realPositionProvider().toLong()),
                    onClick = onPreviousClick,
                    modifier =
                        Modifier
                            .align(Alignment.TopStart)
                            .offset(x = (tuning.skipSideDp + tuning.skipSizeDp / 2f - 32f).dp, y = skipTop.dp),
                )
                PillSkipColumn(
                    icon = Icons.Rounded.FastForward,
                    description = stringResource(R.string.next_track),
                    size = tuning.skipSizeDp.dp,
                    time = formatTime(remaining),
                    onClick = onNextClick,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = (-(tuning.skipSideDp + tuning.skipSizeDp / 2f - 32f)).dp, y = skipTop.dp),
                )

                // Play / pause inside the album, near the bottom.
                PillGlyphButton(
                    size = tuning.playSizeDp.dp,
                    onClick = onPlayPauseClick,
                    modifier = Modifier.align(Alignment.TopCenter).offset(y = playTop.dp),
                ) {
                    AnimatedContent(
                        targetState = isPlaying,
                        transitionSpec = { me.spica27.spicamusic.ui.motion.NoTransition },
                        label = "pillPlayPause",
                    ) { playing ->
                        Icon(
                            imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription =
                                if (playing) stringResource(R.string.pause) else stringResource(R.string.play),
                            tint = Color.White,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            // Artist and song.
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SideInset)
                        .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = artist,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Mix and like, sitting right above the lyrics bar.
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SideInset)
                        .offset(y = (-tuning.actionsHeightDp).dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillCircleButton(onClick = onPlayModeClick) {
                    AnimatedContent(
                        targetState = playMode,
                        transitionSpec = { me.spica27.spicamusic.ui.motion.NoTransition },
                        label = "pillPlayMode",
                    ) { mode ->
                        Icon(
                            imageVector =
                                when (mode) {
                                    PlayMode.LOOP -> Icons.Rounded.Repeat
                                    PlayMode.LIST -> Icons.Rounded.RepeatOne
                                    PlayMode.SHUFFLE -> Icons.Rounded.Shuffle
                                },
                            contentDescription = stringResource(R.string.play_mode),
                            tint = onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                PillCircleButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isLike) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = stringResource(R.string.favorite),
                        tint = if (isLike) Color(0xffcf1322) else onSurface,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }

            // Lyrics bar.
            Box(
                modifier =
                    Modifier
                        .padding(horizontal = SideInset)
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable(role = Role.Button, onClick = onLyricsClick),
            ) {
                Box(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                            .size(width = 86.dp, height = 4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)),
                )
                Text(
                    text = stringResource(R.string.pill_see_lyrics),
                    style = MaterialTheme.typography.bodySmall,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center).padding(top = 10.dp),
                )
            }
        }
    }
}

/** Outlined circle button used for back, queue, mix and like. */
@Composable
private fun PillCircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Box(
        modifier =
            modifier
                .size(CircleButtonSize)
                .clip(CircleShape)
                .background(onSurface.copy(alpha = 0.10f))
                .border(1.5.dp, onSurface.copy(alpha = 0.85f), CircleShape)
                .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Plain glyph button in the Apple Music style: no outline, the glyph shrinks and a soft round
 * highlight shows while pressed.
 */
@Composable
private fun PillGlyphButton(
    size: Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.86f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 1100f),
        label = "pillGlyphScale",
    )
    Box(
        modifier =
            modifier
                .size(size)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(CircleShape)
                .background(Color.White.copy(alpha = if (pressed) 0.22f else 0f))
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    role = Role.Button,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
private fun PillSkipColumn(
    icon: ImageVector,
    description: String,
    size: Dp,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.width(64.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PillGlyphButton(size = size, onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Text(
            text = time,
            style = MaterialTheme.typography.labelMedium.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** Rotates text 90 degrees clockwise and swaps its layout width and height. */
private fun Modifier.verticalText(): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(placeable.height, placeable.width) {
            placeable.place(
                x = -(placeable.width / 2 - placeable.height / 2),
                y = -(placeable.height / 2 - placeable.width / 2),
            )
        }
    }.rotate(90f)
