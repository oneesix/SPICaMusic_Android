package me.spica27.spicamusic.ui.player

import android.os.Build
import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ChevronLeft
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import kotlinx.coroutines.delay
import me.spica27.spicamusic.R
import me.spica27.spicamusic.artwork.artwork
import me.spica27.spicamusic.player.api.PlayMode
import me.spica27.spicamusic.ui.glass.LocalLiquidGlassV2Config
import me.spica27.spicamusic.ui.glass.liquidGlassV2Surface
import me.spica27.spicamusic.ui.home.page.AlbumStyleTuning
import me.spica27.spicamusic.ui.home.page.albumHighlight
import me.spica27.spicamusic.ui.theme.LocalReducedMotion
import me.spica27.spicamusic.ui.widget.AudioCover
import me.spica27.spicamusic.ui.widget.audio_seekbar.GlowParticleSeekBar
import me.spica27.spicamusic.ui.widget.clickHighlight
import me.spica27.spicamusic.ui.widget.materialSharedAxisYIn
import me.spica27.spicamusic.ui.widget.materialSharedAxisYOut
import kotlin.math.min

private val SideInset = 20.dp
private const val HOLD_MS = 2000L
private const val PANEL_PADDING_DP = 12
private const val INFO_BAR_EXTRA_DP = 34

/**
 * Bubble player style: a floating top bar (back, time listened | next song, queue), the album in the
 * middle with previous / play / next inside it, and three floating bars at the bottom (progress,
 * mix / title / like, one lyrics line). Holding the play button for 2 seconds opens the Bubble settings.
 * Every circle button has its own liquid glass when the app's liquid glass setting is on.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
internal fun BubblePlayerPage(
    currentMediaItem: () -> MediaItem?,
    nextTitle: String?,
    seekPositionProvider: () -> Float,
    realPositionProvider: () -> Float,
    duration: Long,
    isPlaying: Boolean,
    isLike: Boolean,
    playMode: PlayMode,
    sampleRate: Int,
    bitRate: Int,
    isLossless: Boolean,
    hazeState: HazeState,
    tuning: BubblePlayerTuning,
    onTuningChange: (BubblePlayerTuning) -> Unit,
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
    /** Swipe down on the top bars collapses the player (replaces the old handle strip). */
    dragModifier: Modifier = Modifier,
) {
    val item = currentMediaItem()
    val metadata = item?.mediaMetadata
    val artist =
        metadata?.artist?.toString()?.takeIf { it.isNotBlank() }
            ?: stringResource(R.string.unknown_artist)
    val title = metadata?.title?.toString() ?: stringResource(R.string.unknown_song)
    val albumTitle = metadata?.albumTitle?.toString()?.takeIf { it.isNotBlank() }
    val year = metadata?.releaseYear ?: metadata?.recordingYear
    val infoText = listOfNotNull(year?.toString(), albumTitle).joinToString(" \u2022 ").takeIf { it.isNotEmpty() }

    val discNumber = metadata?.discNumber?.takeIf { it > 0 }
    val trackNumber = metadata?.trackNumber?.takeIf { it > 0 }
    val discTrackText =
        listOfNotNull(
            discNumber?.let { stringResource(R.string.song_disc_format, it) },
            trackNumber?.let { stringResource(R.string.song_track_format, it) },
        ).joinToString(" \u00B7 ").takeIf { it.isNotEmpty() }
    val yearText = year?.toString()
    val showInfoBar = tuning.infoBarPos != 2 && (yearText != null || albumTitle != null || discTrackText != null)

    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface

    // Blur on: translucent fill over the blurred background. Blur off: a solid colour, never transparent.
    val blurOn = tuning.blurEnabled == 1
    val barFill = if (blurOn) onSurface.copy(alpha = 0.14f) else lerp(surface, onSurface, 0.18f)
    val circleFill = if (blurOn) onSurface.copy(alpha = 0.16f) else lerp(surface, onSurface, 0.28f)
    val blurAmount = tuning.blurAmountDp
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
    val glassBlur: Modifier =
        if (blurOn && blurAmount > 0) {
            Modifier.hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = blurStyle,
                performanceMode =
                    if (tuning.blurOptimize == 1) {
                        HazePerformanceMode.Fixed(tuning.blurQuality / 100f)
                    } else {
                        HazePerformanceMode.Balanced
                    },
                expandLayerBounds = false,
            )
        } else {
            Modifier
        }

    // Liquid glass on the bars: follows the app's liquid glass settings (null when it is off).
    val topGlass = lyricsGlassSurfaceOrNull(hazeState, CircleShape, surface)
    val titleGlass = lyricsGlassSurfaceOrNull(hazeState, CircleShape, surface)
    val lyricsGlass = lyricsGlassSurfaceOrNull(hazeState, CircleShape, surface)
    val infoBarGlass = lyricsGlassSurfaceOrNull(hazeState, CircleShape, surface)
    val progressGlass =
        lyricsGlassSurfaceOrNull(hazeState, RoundedCornerShape(22.dp), surface)
    val highlightTuning =
        remember(tuning.hlStyle, tuning.hlWidthTenths, tuning.hlAlphaPercent, tuning.hlAngle, tuning.hlFalloffTenths) {
            AlbumStyleTuning(
                highlightStyle = tuning.hlStyle,
                highlightWidthTenths = tuning.hlWidthTenths,
                highlightAlphaPercent = tuning.hlAlphaPercent,
                highlightAngle = tuning.hlAngle,
                highlightFalloffTenths = tuning.hlFalloffTenths,
            )
        }

    // Time listened to this song: counts only while playing, restarts when the song changes.
    val mediaId = item?.mediaId
    var listenedMs by remember(mediaId) { mutableLongStateOf(0L) }
    LaunchedEffect(mediaId, isPlaying) {
        if (isPlaying) {
            var last = SystemClock.elapsedRealtime()
            while (true) {
                delay(250)
                val now = SystemClock.elapsedRealtime()
                listenedMs += now - last
                last = now
            }
        }
    }

    // Second top bar: year | album | disc / track. Above or below the top bar (setting).
    val infoBar: @Composable () -> Unit = {
        BubbleInfoBar(
            year = yearText,
            album = albumTitle,
            discTrack = discTrackText,
            heightDp = tuning.infoBarHeightDp,
            textTenths = tuning.infoTextTenths,
            background = infoBarGlass ?: Modifier.clip(CircleShape).then(glassBlur).background(barFill),
            modifier = Modifier.padding(top = 6.dp).then(dragModifier),
        )
    }

    var showSettings by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val openSettings = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        showSettings = true
    }

    BoxWithConstraints(modifier = modifier.navigationBarsPadding()) {
        val backgroundPad = if (tuning.albumBackground == 1) PANEL_PADDING_DP else 0
        // The width slider may reach the left and right padding of the page (the panel counts too).
        val albumMaxWidth = (maxWidth.value - 2 * SideInset.value).toInt() - 2 * backgroundPad

        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = SideInset),
        ) {
            if (showInfoBar && tuning.infoBarPos == 0) infoBar()

            // Top bar.
            Row(
                modifier =
                    Modifier
                        .padding(top = 6.dp)
                        .then(dragModifier)
                        .fillMaxWidth()
                        .height(tuning.topBarHeightDp.dp)
                        .then(topGlass ?: Modifier.clip(CircleShape).then(glassBlur).background(barFill))
                        .clip(CircleShape)
                        .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val circle = (tuning.topBarHeightDp - 12).coerceAtLeast(24).dp
                BubbleCircle(
                    size = circle,
                    hazeState = hazeState,
                    blur = glassBlur,
                    fill = circleFill,
                    description = stringResource(R.string.back),
                    onClick = onCollapse,
                ) {
                    Icon(
                        imageVector = Icons.Filled.ChevronLeft,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(circle * 0.6f),
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = stringResource(R.string.bp_total_listening),
                            fontSize = (tuning.timeLabelTenths / 10f).sp,
                            lineHeight = (tuning.timeLabelTenths / 10f * 1.2f).sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = formatTime(listenedMs),
                            fontSize = (tuning.timeTextTenths / 10f).sp,
                            lineHeight = (tuning.timeTextTenths / 10f * 1.15f).sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = onSurface,
                            maxLines = 1,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        if (nextTitle != null) {
                            Text(
                                text = stringResource(R.string.bp_upcoming_song),
                                fontSize = (tuning.nextLabelTenths / 10f).sp,
                                lineHeight = (tuning.nextLabelTenths / 10f * 1.2f).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = nextTitle,
                                fontSize = (tuning.nextTextTenths / 10f).sp,
                                lineHeight = (tuning.nextTextTenths / 10f * 1.15f).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 6.dp),
                            )
                        }
                    }
                }
                BubbleCircle(
                    size = circle,
                    hazeState = hazeState,
                    blur = glassBlur,
                    fill = circleFill,
                    description = stringResource(R.string.queue),
                    onClick = onPlaylistClick,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = onSurface,
                        modifier = Modifier.size(circle * 0.55f),
                    )
                }
            }

            if (showInfoBar && tuning.infoBarPos == 1) infoBar()

            // Album in the middle.
            BoxWithConstraints(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                val panelOn = tuning.albumBackground == 1
                val infoOn = panelOn && tuning.yearInfo == 1 && infoText != null
                val pad = if (panelOn) PANEL_PADDING_DP else 0
                val infoExtra = if (infoOn) INFO_BAR_EXTRA_DP else 0
                val albumWidth = min(tuning.albumWidthDp.toFloat(), (maxWidth.value - 2 * pad).coerceAtLeast(120f))
                val albumHeight =
                    min(
                        tuning.albumHeightDp.toFloat(),
                        (maxHeight.value - 2 * pad - infoExtra).coerceAtLeast(120f),
                    )
                val radius = min(tuning.cornerDp.toFloat(), min(albumWidth, albumHeight) / 2f)
                val albumBackdrop = rememberLayerBackdrop()
                val panelGlass =
                    if (panelOn) {
                        lyricsGlassSurfaceOrNull(hazeState, RoundedCornerShape((radius + pad).dp), surface)
                    } else {
                        null
                    }
                val infoGlass =
                    if (infoOn) {
                        lyricsGlassSurfaceOrNull(hazeState, RoundedCornerShape(radius.dp), surface)
                    } else {
                        null
                    }
                val v2Enabled = LocalLiquidGlassV2Config.current.enabled

                Column(
                    modifier =
                        Modifier
                            .then(coverSwipeModifier)
                            .then(
                                if (panelOn) {
                                    Modifier
                                        .then(
                                            panelGlass
                                                ?: Modifier
                                                    .clip(RoundedCornerShape((radius + pad).dp))
                                                    .then(glassBlur)
                                                    .background(barFill),
                                        ).clip(RoundedCornerShape((radius + pad).dp))
                                        .padding(pad.dp)
                                } else {
                                    Modifier
                                },
                            ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (infoOn) {
                        Box(
                            modifier =
                                Modifier
                                    .width(albumWidth.dp)
                                    .height(26.dp)
                                    .then(
                                        infoGlass
                                            ?: Modifier
                                                .clip(RoundedCornerShape(radius.dp))
                                                .background(onSurface.copy(alpha = 0.12f)),
                                    ).clip(RoundedCornerShape(radius.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = infoText.orEmpty(),
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 12.dp),
                            )
                        }
                        Box(modifier = Modifier.height(8.dp))
                    }
                    Box(modifier = Modifier.size(width = albumWidth.dp, height = albumHeight.dp)) {
                        // The cover is recorded as its own layer, so the buttons on it can refract it.
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(radius.dp))
                                    .then(if (v2Enabled) Modifier.layerBackdrop(albumBackdrop) else Modifier)
                                    .albumHighlight(highlightTuning, radius.toInt()),
                        ) {
                            AnimatedContent(
                                targetState = item,
                                modifier = Modifier.fillMaxSize(),
                                transitionSpec = {
                                    materialSharedAxisYIn(true) togetherWith materialSharedAxisYOut(true)
                                },
                                contentKey = { it?.mediaId ?: "-1" },
                                label = "bubbleCover",
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
                        Row(
                            modifier =
                                Modifier
                                    .align(Alignment.BottomCenter)
                                    .offset(x = tuning.buttonShiftDp.dp, y = (-tuning.buttonBottomDp).dp),
                            horizontalArrangement = Arrangement.spacedBy(tuning.buttonGapDp.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AlbumGlassButton(
                                size = tuning.skipSizeDp.dp,
                                backdrop = albumBackdrop,
                                description = stringResource(R.string.previous_track),
                                onTap = onPreviousClick,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FastRewind,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size((tuning.skipSizeDp * 0.56f).dp),
                                )
                            }
                            AlbumGlassButton(
                                size = tuning.playSizeDp.dp,
                                backdrop = albumBackdrop,
                                description =
                                    if (isPlaying) stringResource(R.string.pause) else stringResource(R.string.play),
                                onTap = onPlayPauseClick,
                                onHold = openSettings,
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size((tuning.playSizeDp * 0.56f).dp),
                                )
                            }
                            AlbumGlassButton(
                                size = tuning.skipSizeDp.dp,
                                backdrop = albumBackdrop,
                                description = stringResource(R.string.next_track),
                                onTap = onNextClick,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FastForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size((tuning.skipSizeDp * 0.56f).dp),
                                )
                            }
                        }
                    }
                }
            }

            // Three bottom bars, in the order from the settings.
            Column(
                modifier = Modifier.padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                for (bar in tuning.barOrder) {
                    when (bar) {
                        BubblePlayerTuning.BAR_PROGRESS ->
                            BubbleProgressBar(
                                height = tuning.progressHeightDp.dp,
                                blur = glassBlur,
                                fill = barFill,
                                glass = progressGlass,
                                progress =
                                    if (duration > 0) (seekPositionProvider() / duration).coerceIn(0f, 1f) else 0f,
                                playedMs = realPositionProvider().toLong(),
                                durationMs = duration,
                                qualityText = qualityText(sampleRate, bitRate, isLossless),
                                textSizeSp = 12.5f,
                                seekHeight = tuning.seekHeightDp.dp,
                                seekThickness = tuning.seekThicknessDp.dp,
                                seekWidthFraction = tuning.seekWidthPercent / 100f,
                                animationsEnabled = isPlaying && !LocalReducedMotion.current,
                                onSeek = onValueChange,
                                onSeekFinished = onValueChangeFinished,
                            )

                        BubblePlayerTuning.BAR_TITLE ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                val size = tuning.titleBarHeightDp.dp
                                BubbleCircle(
                                    size = size,
                                    hazeState = hazeState,
                                    blur = glassBlur,
                                    fill = circleFill,
                                    description = stringResource(R.string.play_mode),
                                    onClick = onPlayModeClick,
                                ) {
                                    AnimatedContent(
                                        targetState = playMode,
                                        label = "bubblePlayMode",
                                    ) { mode ->
                                        Icon(
                                            imageVector =
                                                when (mode) {
                                                    PlayMode.LOOP -> Icons.Rounded.Repeat
                                                    PlayMode.LIST -> Icons.Rounded.RepeatOne
                                                    PlayMode.SHUFFLE -> Icons.Rounded.Shuffle
                                                },
                                            contentDescription = null,
                                            tint = onSurface,
                                            modifier = Modifier.size(size * 0.43f),
                                        )
                                    }
                                }
                                Column(
                                    modifier =
                                        Modifier
                                            .weight(1f)
                                            .height(size)
                                            .then(titleGlass ?: Modifier.clip(CircleShape).then(glassBlur).background(barFill))
                                            .clip(CircleShape)
                                            .padding(horizontal = 16.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = (tuning.titleTextTenths / 10f).sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                    Text(
                                        text = artist,
                                        fontSize = (tuning.artistTextTenths / 10f).sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                                BubbleCircle(
                                    size = size,
                                    hazeState = hazeState,
                                    blur = glassBlur,
                                    fill = circleFill,
                                    description = stringResource(R.string.favorite),
                                    onClick = onFavoriteClick,
                                ) {
                                    Icon(
                                        imageVector =
                                            if (isLike) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                                        contentDescription = null,
                                        tint = if (isLike) Color(0xffcf1322) else onSurface,
                                        modifier = Modifier.size(size * 0.43f),
                                    )
                                }
                            }

                        else ->
                            // One lyrics line. Only the text animates when the line changes, never the bar.
                            Box(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(tuning.lyricsBarHeightDp.dp)
                                        .then(lyricsGlass ?: Modifier.clip(CircleShape).then(glassBlur).background(barFill))
                                        .clip(CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                MiniLyric(
                                    onClick = onLyricsClick,
                                    textSize = (tuning.lyricsTextTenths / 10f).sp,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                    }
                }
            }
        }

        if (showSettings) {
            BubbleSettingsSheet(
                tuning = tuning,
                albumMaxWidthDp = albumMaxWidth,
                onChange = onTuningChange,
                onDismiss = { showSettings = false },
            )
        }
    }
}

@Composable
private fun qualityText(
    sampleRate: Int,
    bitRate: Int,
    isLossless: Boolean,
): String =
    buildList {
        if (sampleRate > 0) add("${sampleRate / 1000}kHz")
        if (bitRate > 0) add("${bitRate / 1000}kbps")
        if (isLossless) add(stringResource(R.string.lossless))
        if (bitRate >= 320000 && !isLossless) add(stringResource(R.string.high_quality))
    }.joinToString(" \u00b7 ")

/**
 * Circle button on a bar. With liquid glass on it has its own glass surface (same engine and settings
 * as the lyrics bars); otherwise it is a blurred or solid fill.
 */
@Composable
private fun BubbleCircle(
    size: Dp,
    hazeState: HazeState,
    blur: Modifier,
    fill: Color,
    description: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val liquid = lyricsGlassSurfaceOrNull(hazeState, CircleShape, MaterialTheme.colorScheme.surface)
    Box(
        modifier =
            Modifier
                .size(size)
                .then(
                    if (liquid != null) {
                        liquid
                    } else {
                        Modifier
                            .clip(CircleShape)
                            .then(blur)
                            .background(fill)
                    },
                ).clip(CircleShape)
                .semantics { role = Role.Button }
                .clickable(onClickLabel = description, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Second top bar: year, album and disc / track in three columns, no divider strips. */
@Composable
private fun BubbleInfoBar(
    year: String?,
    album: String?,
    discTrack: String?,
    heightDp: Int,
    textTenths: Int,
    background: Modifier,
    modifier: Modifier = Modifier,
) {
    val cells = listOfNotNull(year?.let { it to 0.7f }, album?.let { it to 1.6f }, discTrack?.let { it to 1.1f })
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .then(background)
                .clip(CircleShape)
                .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        cells.forEach { (text, weight) ->
            Text(
                text = text,
                fontSize = (textTenths / 10f).sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(weight).padding(horizontal = 4.dp),
            )
        }
    }
}

/**
 * Round button on the album. It has its own liquid glass that refracts the cover; with liquid glass off
 * it is a dark translucent circle. Passing [onHold] makes a 2 second hold call it instead of a tap.
 */
@Composable
private fun AlbumGlassButton(
    size: Dp,
    backdrop: Backdrop,
    description: String,
    onTap: () -> Unit,
    onHold: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val v2 = LocalLiquidGlassV2Config.current
    val glass =
        if (v2.enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.liquidGlassV2Surface(
                backdrop = backdrop,
                tuning = v2.tuning,
                shape = CircleShape,
                surfaceColor = MaterialTheme.colorScheme.surface,
            )
        } else {
            Modifier.clip(CircleShape).background(Color.Black.copy(alpha = 0.35f))
        }
    val tap by rememberUpdatedState(onTap)
    val hold by rememberUpdatedState(onHold)
    val input =
        if (onHold != null) {
            Modifier
                .semantics {
                    role = Role.Button
                    onClick(label = description) {
                        tap()
                        true
                    }
                    onLongClick(label = null) {
                        hold?.invoke()
                        true
                    }
                }.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        var released = false
                        val up =
                            withTimeoutOrNull(HOLD_MS) {
                                val result = waitForUpOrCancellation()
                                released = true
                                result
                            }
                        if (up != null) {
                            up.consume()
                            tap()
                        } else if (!released) {
                            // Held for 2 seconds: open the settings, then wait for the finger to lift.
                            hold?.invoke()
                            waitForUpOrCancellation()?.consume()
                        }
                    }
                }
        } else {
            Modifier.clickable(onClickLabel = description, role = Role.Button, onClick = onTap)
        }
    Box(
        modifier =
            Modifier
                .size(size)
                .then(glass)
                .clip(CircleShape)
                .then(input),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/** Progress line with time played (left), file info (middle) and time left (right). */
@Composable
private fun BubbleProgressBar(
    height: Dp,
    blur: Modifier,
    fill: Color,
    glass: Modifier?,
    progress: Float,
    playedMs: Long,
    durationMs: Long,
    qualityText: String,
    textSizeSp: Float,
    seekHeight: Dp,
    seekThickness: Dp,
    seekWidthFraction: Float,
    animationsEnabled: Boolean,
    onSeek: (Float) -> Unit,
    onSeekFinished: () -> Unit,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(height)
                .then(glass ?: Modifier.clip(RoundedCornerShape(22.dp)).then(blur).background(fill))
                .clip(RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        // The app's glow particles progress bar (same drawing as the player's progress bar style).
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            GlowParticleSeekBar(
                progress = progress,
                onProgressChange = onSeek,
                onProgressChangeFinished = onSeekFinished,
                animationsEnabled = animationsEnabled,
                trackThickness = seekThickness,
                modifier = Modifier.fillMaxWidth(seekWidthFraction.coerceIn(0.3f, 1f)).height(seekHeight),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val timeStyle = (textSizeSp - 0.5f).sp
            Text(
                text = formatTime(playedMs),
                fontSize = timeStyle,
                fontWeight = FontWeight.SemiBold,
                color = onSurface,
                maxLines = 1,
            )
            Text(
                text = qualityText,
                fontSize = timeStyle,
                color = muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            )
            Text(
                text = "-" + formatTime((durationMs - playedMs).coerceAtLeast(0L)),
                fontSize = timeStyle,
                fontWeight = FontWeight.SemiBold,
                color = onSurface,
                maxLines = 1,
            )
        }
    }
}
