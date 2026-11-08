package me.spica27.spicamusic.ui.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
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
import dev.chrisbanes.haze.hazeSource
import me.spica27.spicamusic.R
import me.spica27.spicamusic.artwork.MusicArtwork
import me.spica27.spicamusic.ui.navigation.LocalBackStack
import me.spica27.spicamusic.ui.navigation.LyricsSourceRoute
import me.spica27.spicamusic.ui.theme.Shapes
import me.spica27.spicamusic.ui.theme.Spacing
import me.spica27.spicamusic.ui.widget.AudioCover
import me.spica27.spicamusic.ui.widget.LyricsCircleButton
import me.spica27.spicamusic.ui.widget.LyricsSettingsSheet
import me.spica27.spicamusic.ui.widget.LyricsToolbarMenuContent
import me.spica27.spicamusic.ui.widget.audio_seekbar.GlowParticleSeekBar
import me.spica27.spicamusic.ui.widget.rememberLyricsEdgePadding
import org.koin.compose.viewmodel.koinActivityViewModel
import java.util.Locale

private val LyricsToolbarBottomInset = 112.dp
private val LyricsHeaderCoverSize = 56.dp
private val LyricsHeaderButtonSize = 44.dp

/**
 * 展开播放器内部的全屏歌词前景。
 *
 * 该组件只负责展示与交互，不拥有导航状态；关闭歌词由播放器宿主的返回手势处理。
 */
@Composable
internal fun FullscreenLyricsSurface(
    title: String,
    artist: String,
    artwork: MusicArtwork?,
    isLike: Boolean,
    onLikeClick: () -> Unit,
    /** Shared with the player background: the header blurs background + lyrics together. */
    hazeState: HazeState,
    /** Album name (shown in upper case) and release year (year only) in the artist line. */
    album: String?,
    releaseYear: Int?,
    /** true while the progress bar is being dragged: the artist line turns into "Seeking". */
    isSeeking: Boolean,
    seekPositionProvider: () -> Float,
    /** Time (ms) the bar is dragged to, or null when not dragging: the lyrics follow it. */
    seekPreviewTimeProvider: () -> Long?,
    durationMs: Long,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    progressAnimationsEnabled: Boolean,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val styleViewModel: LyricsStyleViewModel = koinActivityViewModel()
    val style by styleViewModel.style.collectAsStateWithLifecycle()
    // Enhance header: blur ON  -> lyrics scroll behind the blurred header.
    //                 blur OFF -> lyrics start below the header; the line separates them.
    val blurBehind = style.enhanceHeader && style.headerBlurEnabled
    val separated = style.enhanceHeader && !style.headerBlurEnabled
    // The bottom bar also blurs the lyrics behind it, so the lyrics must be a blur source then.
    val dockBlur = style.enhanceHeader && style.dockBlurEnabled && style.dockBlurAmount > 0
    val dockSpread = dockBlur && style.dockSpreadBlur
    var dockHeightPx by remember { mutableIntStateOf(0) }
    val dockHeight = with(LocalDensity.current) { dockHeightPx.toDp() }
    var barHeightPx by remember { mutableIntStateOf(0) }
    val barHeight = with(LocalDensity.current) { barHeightPx.toDp() }

    Box(modifier = modifier.fillMaxSize()) {
        LyricsPanel(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = if (separated) barHeight else 0.dp)
                    // Blur off + soft edge: the lyrics fade in below the header instead of a hard cut.
                    .then(
                        if (separated && style.headerSoftEdge && style.headerEdgeFade > 0) {
                            Modifier.lyricsTopFade(style.headerEdgeFade.dp)
                        } else {
                            Modifier
                        },
                    )
                    // zIndex 1: drawn above the background source (zIndex 0) that shares this state, so the
                    // blurred header shows background + lyrics together and hides the sharp lyrics below it.
                    .then(if (blurBehind || dockBlur) Modifier.hazeSource(hazeState, zIndex = 1f) else Modifier),
            toolbarBottomInset = LyricsToolbarBottomInset,
            // 工具栏已移到顶栏右侧（三点按钮）
            showToolbar = false,
            seekPreviewTimeProvider = seekPreviewTimeProvider,
        )

        // Blur layer behind the header: strong behind the whole header, then it fades out softly
        // in a band below it (same Haze blur the queue top bar uses, plus a fade mask).
        if (blurBehind && style.headerBlurAmount > 0) {
            LyricsHeaderBlurLayer(
                hazeState = hazeState,
                blurRadius = style.headerBlurAmount.dp,
                barHeight = barHeight,
                fade = if (style.headerSoftEdge) style.headerEdgeFade.dp else 0.dp,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }

        FullscreenLyricsHeader(
            title = title,
            artist = artist,
            artwork = artwork,
            isLike = isLike,
            onLikeClick = onLikeClick,
            album = album,
            releaseYear = releaseYear,
            isSeeking = isSeeking,
            seekPositionProvider = seekPositionProvider,
            durationMs = durationMs,
            onSeekChange = onSeekChange,
            onSeekFinished = onSeekFinished,
            progressAnimationsEnabled = progressAnimationsEnabled,
            onBarHeightChange = { barHeightPx = it },
            modifier = Modifier.align(Alignment.TopCenter),
        )

        // Bottom bar, "spread" blur: one blur layer behind the whole bar that fades out above it.
        if (dockSpread) {
            LyricsHeaderBlurLayer(
                hazeState = hazeState,
                blurRadius = style.dockBlurAmount.dp,
                barHeight = dockHeight,
                fade = if (style.dockSoftEdge) style.dockEdgeFade.dp else 0.dp,
                fadeOnTop = true,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }

        // Bottom bar: transliteration button | pill (cover, artist + time, play/pause) | translation button
        if (style.enhanceHeader) {
            LyricsBottomBar(
                artist = artist,
                artwork = artwork,
                isPlaying = isPlaying,
                onPlayPauseClick = onPlayPauseClick,
                seekPositionProvider = seekPositionProvider,
                durationMs = durationMs,
                hazeState = hazeState,
                onHeightChange = { dockHeightPx = it },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun FullscreenLyricsHeader(
    title: String,
    artist: String,
    artwork: MusicArtwork?,
    isLike: Boolean,
    onLikeClick: () -> Unit,
    /** Album name (shown in upper case) and release year (year only) in the artist line. */
    album: String?,
    releaseYear: Int?,
    /** true while the progress bar is being dragged: the artist line turns into "Seeking". */
    isSeeking: Boolean,
    seekPositionProvider: () -> Float,
    durationMs: Long,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    progressAnimationsEnabled: Boolean,
    onBarHeightChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // 与歌词文字的左边距一致，左右对称
    val edge = rememberLyricsEdgePadding()

    // 歌词工具栏（偏移 / 歌词设置 / 切换歌词）：原来在右下角，现在从三点按钮向下展开
    val viewModel: LyricsViewModel = koinActivityViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val styleViewModel: LyricsStyleViewModel = koinActivityViewModel()
    val styleSettings by styleViewModel.style.collectAsStateWithLifecycle()
    val backStack = LocalBackStack.current
    var menuExpanded by remember { mutableStateOf(false) }
    var showLyricsSettings by remember { mutableStateOf(false) }

    // Enhance header off = the original header (default sizes, no line, no blur).
    val enhanced = styleSettings.enhanceHeader
    val coverSize: Dp = if (enhanced) styleSettings.headerAlbumSize.dp else LyricsHeaderCoverSize
    val buttonSize: Dp = if (enhanced) styleSettings.headerButtonSize.dp else LyricsHeaderButtonSize
    val titleSize = (if (enhanced) styleSettings.headerTitleSize else 22).sp
    val artistSize = (if (enhanced) styleSettings.headerArtistSize else 14).sp
    val bottomPadding: Dp = if (enhanced) styleSettings.headerBottomPadding.dp else Spacing.Medium

    // Artist • ALBUM • year
    val infoLine =
        remember(artist, album, releaseYear) {
            buildString {
                append(artist)
                if (!album.isNullOrBlank()) {
                    append(" \u2022 ")
                    append(album.uppercase(Locale.getDefault()))
                }
                if (releaseYear != null && releaseYear > 0) {
                    append(" \u2022 ")
                    append(releaseYear)
                }
            }
        }

    val likeButton: @Composable () -> Unit = {
        LyricsCircleButton(
            onClick = onLikeClick,
            size = buttonSize,
        ) {
            Icon(
                imageVector = if (isLike) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                contentDescription = stringResource(R.string.favorite),
                // Same colour as the outline heart; only the filled shape shows it is liked.
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(buttonSize / 2),
            )
        }
    }
    val dotsButton: @Composable () -> Unit = {
        LyricsCircleButton(
            onClick = { menuExpanded = !menuExpanded },
            size = buttonSize,
        ) {
            Icon(
                imageVector = Icons.Rounded.MoreHoriz,
                contentDescription =
                    if (menuExpanded) {
                        stringResource(R.string.collapse)
                    } else {
                        stringResource(R.string.lyrics_tool)
                    },
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(buttonSize / 2),
            )
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // The bar (status bar area + row + line). The menu below is not part of it.
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { onBarHeightChange(it.height) }
                    .statusBarsPadding(),
        ) {
            if (enhanced) {
                // Enhanced layout: like (left) | title + artist info (centre) | dots (right).
                // No album art here for now; its size / outline settings are kept for later.
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = edge, end = edge, top = Spacing.Medium, bottom = bottomPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
                ) {
                    likeButton()
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = titleSize,
                            lineHeight = titleSize * 1.25f,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        // While the progress bar is dragged the artist line shows "Seeking" (same size).
                        AnimatedContent(
                            targetState = isSeeking,
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center,
                            transitionSpec = { fadeIn(tween(160)) togetherWith fadeOut(tween(120)) },
                            label = "lyrics_info_or_seeking",
                        ) { seeking ->
                            Text(
                                text = if (seeking) stringResource(R.string.lyrics_seeking) else infoLine,
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = artistSize,
                                lineHeight = artistSize * 1.3f,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                    dotsButton()
                }

                // The glow-particle progress bar replaces the separator line
                LyricsHeaderProgressBar(
                    seekPositionProvider = seekPositionProvider,
                    durationMs = durationMs,
                    onSeekChange = onSeekChange,
                    onSeekFinished = onSeekFinished,
                    animationsEnabled = progressAnimationsEnabled,
                    modifier = Modifier.padding(horizontal = edge),
                )
            } else {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = edge, end = edge, top = Spacing.Medium, bottom = bottomPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(coverSize)
                                .clip(Shapes.MediumCornerBasedShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                                // Optional thin outline around the album (drawn above the cover image)
                                .then(
                                    if (enhanced && styleSettings.headerOutlineEnabled) {
                                        Modifier.border(
                                            width = (styleSettings.headerOutlineWeight / 2f).dp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = styleSettings.headerOutlineOpacity / 100f),
                                            shape = Shapes.MediumCornerBasedShape,
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                    ) {
                        AudioCover(
                            artwork = artwork,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontSize = titleSize,
                            lineHeight = titleSize * 1.25f,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = artistSize,
                            lineHeight = artistSize * 1.3f,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        likeButton()
                        dotsButton()
                    }
                }
            }
        }

        // 从三点按钮下方展开，右边距与顶栏一致
        AnimatedVisibility(
            visible = menuExpanded,
            modifier = Modifier.align(Alignment.End).padding(end = edge),
            enter =
                fadeIn(spring(stiffness = Spring.StiffnessMedium)) +
                    expandVertically(spring(stiffness = Spring.StiffnessMedium)),
            exit =
                fadeOut(spring(stiffness = Spring.StiffnessMedium)) +
                    shrinkVertically(spring(stiffness = Spring.StiffnessMedium)),
        ) {
            LyricsToolbarMenuContent(
                offsetEnabled = !uiState.lyricsSuppressed,
                offsetMs = uiState.lyricsOffsetMs,
                onOffsetChange = { viewModel.updateOffset(it) },
                onOpenLyricsSettings = {
                    menuExpanded = false
                    showLyricsSettings = true
                },
                onOpenLyricsSwitcher = {
                    menuExpanded = false
                    viewModel.openPanel()
                    backStack.add(LyricsSourceRoute)
                },
            )
        }
    }

    if (showLyricsSettings) {
        LyricsSettingsSheet(
            settings = styleSettings,
            onUpdate = styleViewModel::update,
            onDismiss = { showLyricsSettings = false },
        )
    }
}

/** The player's glow-particle seek bar, sized for the header. */
@Composable
private fun LyricsHeaderProgressBar(
    seekPositionProvider: () -> Float,
    durationMs: Long,
    onSeekChange: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    // Only this small composable reads the position, so the header does not recompose on every tick.
    val position = seekPositionProvider()
    val progress = if (durationMs > 0) (position / durationMs).coerceIn(0f, 1f) else 0f
    GlowParticleSeekBar(
        progress = progress,
        onProgressChange = onSeekChange,
        onProgressChangeFinished = onSeekFinished,
        animationsEnabled = animationsEnabled,
        // No particle trail in the header; the glowing head stays.
        showParticles = false,
        modifier = modifier.fillMaxWidth().height(40.dp),
    )
}

/** "1:23 / 3:45" shown in place of the title while the progress bar is dragged. */
@Composable
private fun LyricsSeekTimeText(
    seekPositionProvider: () -> Float,
    durationMs: Long,
    fontSize: TextUnit,
) {
    Text(
        text = "${formatTime(seekPositionProvider().toLong())} / ${formatTime(durationMs)}",
        style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
        fontSize = fontSize,
        lineHeight = fontSize * 1.25f,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Blur behind the lyrics header (fade band below) or the bottom bar (fade band above). Height = bar + fade
 * band; the band is masked out so the blur dissolves smoothly instead of ending at a hard line.
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
private fun LyricsHeaderBlurLayer(
    hazeState: HazeState,
    blurRadius: Dp,
    barHeight: Dp,
    fade: Dp,
    /** false: the fade band is below the bar (header). true: above it (bottom bar). */
    fadeOnTop: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val style =
        remember(blurRadius) {
            HazeBlurStyle {
                blurRadius(blurRadius)
                noiseFactor(0f)
                // Blur only: no tint, no darkening.
                backgroundColor(Color.Transparent)
                colorEffects(emptyList())
                fallbackColorEffect(HazeColorEffect.tint(Color.Transparent))
                alpha(1f)
            }
        }
    val total = barHeight + fade
    val solid = if (total > 0.dp) (barHeight / total).coerceIn(0f, 1f) else 1f
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(total)
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .drawWithContent {
                    drawContent()
                    if (fade > 0.dp) {
                        // Keep the blur fully opaque over the header, fade it out over the band.
                        drawRect(
                            brush =
                                if (fadeOnTop) {
                                    Brush.verticalGradient(
                                        0f to Color.Transparent,
                                        (1f - solid) to Color.Black,
                                        1f to Color.Black,
                                    )
                                } else {
                                    Brush.verticalGradient(
                                        0f to Color.Black,
                                        solid to Color.Black,
                                        1f to Color.Transparent,
                                    )
                                },
                            blendMode = BlendMode.DstIn,
                        )
                    }
                }.hazeBlur(
                    input = HazeInput.Sources(hazeState),
                    style = style,
                    performanceMode = HazePerformanceMode.Balanced,
                    expandLayerBounds = false,
                ),
    )
}

/** Fades the top [fade] of this content from transparent to opaque. */
private fun Modifier.lyricsTopFade(fade: Dp): Modifier =
    this
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
            drawContent()
            val f = fade.toPx().coerceAtMost(size.height)
            if (f > 0f && size.height > 0f) {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            (f / size.height) to Color.Black,
                            1f to Color.Black,
                        ),
                    blendMode = BlendMode.DstIn,
                )
            }
        }
