@file:Suppress("FunctionName")

package me.spica27.spicamusic.ui.home.page

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.ExperimentalHazeApi
import me.spica27.spicamusic.ui.home.player_bar.BottomBarAppearance
import me.spica27.spicamusic.common.entity.BottomBarLayout
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.WindowInsetsSides
import org.koin.compose.koinInject
import me.spica27.spicamusic.ui.home.player_bar.playerBarSurface
import me.spica27.spicamusic.ui.home.player_bar.BottomBarLabelMode
import me.spica27.spicamusic.ui.glass.liquidGlassV2Source
import me.spica27.spicamusic.ui.glass.liquidGlassSource
import me.spica27.spicamusic.ui.glass.LocalLiquidGlassConfig
import me.spica27.spicamusic.ui.glass.LocalGlassV2Backdrop
import me.spica27.spicamusic.core.preferences.PreferencesManager
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.HazeState
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Album
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.animation.core.AnimationSpec
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scanner
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import me.spica27.spicamusic.R
import me.spica27.spicamusic.artwork.artwork
import me.spica27.spicamusic.common.entity.Album
import me.spica27.spicamusic.common.entity.Artist
import me.spica27.spicamusic.common.entity.Song
import me.spica27.spicamusic.ui.dialog.SortMenuOption
import me.spica27.spicamusic.ui.home.HomeViewModel
import me.spica27.spicamusic.ui.home.player_bar.GlassNavigationIndicatorState
import me.spica27.spicamusic.ui.navigation.AlbumDetailRoute
import me.spica27.spicamusic.ui.navigation.ArtistDetailRoute
import me.spica27.spicamusic.ui.navigation.LocalBackStack
import me.spica27.spicamusic.ui.navigation.PopupAnchor
import me.spica27.spicamusic.ui.navigation.ScannerRoute
import me.spica27.spicamusic.ui.navigation.SongMenuRoute
import me.spica27.spicamusic.ui.navigation.SortMenuDialogRoute
import me.spica27.spicamusic.ui.player.LocalPlayerViewModel
import me.spica27.spicamusic.ui.theme.ENTRANCE_GATE_MILLIS
import me.spica27.spicamusic.ui.theme.EaseOutEmphasized
import me.spica27.spicamusic.ui.theme.LayoutTokens
import me.spica27.spicamusic.ui.theme.LocalReducedMotion
import me.spica27.spicamusic.ui.theme.ScaleEnterFrom
import me.spica27.spicamusic.ui.theme.ScaleExitTo
import me.spica27.spicamusic.ui.theme.Shapes
import me.spica27.spicamusic.ui.theme.Spacing
import me.spica27.spicamusic.ui.theme.entrance
import me.spica27.spicamusic.ui.widget.AnimatedCursorTextField
import me.spica27.spicamusic.ui.widget.AudioCover
import me.spica27.spicamusic.ui.widget.artistDisplay
import me.spica27.spicamusic.ui.widget.clickHighlight
import me.spica27.spicamusic.ui.widget.combinedClickHighlight
import me.spica27.spicamusic.ui.widget.displayName
import me.spica27.spicamusic.ui.widget.materialSharedAxisZ
import me.spica27.spicamusic.ui.widget.rememberAlbumArtColor
import me.spica27.spicamusic.ui.widget.rememberIOSOverScrollEffect
import org.koin.compose.viewmodel.koinActivityViewModel
import kotlin.math.abs

/** 音乐页：歌曲 / 专辑 / 歌手浏览 */

/** 分段控件高度与滑块内缩 */
private val SegmentedHeight = 44.dp
private val SegmentedInset = 4.dp

/** 列表行封面尺寸 */
private val RowCoverSize = 48.dp

/** 首屏入场槽位：刊头 0、分段 1、搜索行 2，条目从 3 起 */
private const val ENTRANCE_ORDER_ITEM_BASE = 0
private const val ENTRANCE_MAX_ORDER = 10

/** 搜索、排序等列表更新使用短淡入淡出；标签切换由 entrance 负责入场。 */
private val ItemFadeInSpec = tween<Float>(durationMillis = 160, easing = LinearOutSlowInEasing)
private val ItemFadeOutSpec = tween<Float>(durationMillis = 100, easing = LinearEasing)

/** 条目重排平稳归位，不越过目标位置或回弹。 */
private val ItemPlacementSpringSpec =
    spring<IntOffset>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium,
        visibilityThreshold = IntOffset.VisibilityThreshold,
    )

@Immutable
private enum class MusicBrowserTab(
    @param:StringRes val titleRes: Int,
    @param:StringRes val searchHintRes: Int,
    @param:StringRes val foundRes: Int,
    val icon: ImageVector,
) {
    Songs(
        titleRes = R.string.music_tab_songs,
        searchHintRes = R.string.music_search_songs_hint,
        foundRes = R.string.music_found_songs,
        icon = Icons.Rounded.MusicNote,
    ),
    Albums(
        titleRes = R.string.music_tab_albums,
        searchHintRes = R.string.music_search_albums_hint,
        foundRes = R.string.music_found_albums,
        icon = Icons.Filled.Album,
    ),
    Artists(
        titleRes = R.string.music_tab_artists,
        searchHintRes = R.string.music_search_artists_hint,
        foundRes = R.string.music_found_artists,
        icon = Icons.Default.Person,
    ),
}

// 各标签页的排序方式。

@Immutable
private enum class SongSortMode(
    val option: SortMenuOption,
    val comparator: Comparator<Song>,
) {
    TitleAsc(
        SortMenuOption("title_asc", R.string.sort_song_title_az, Icons.Default.SortByAlpha),
        compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayName },
    ),
    TitleDesc(
        SortMenuOption("title_desc", R.string.sort_song_title_za, Icons.Default.SortByAlpha),
        compareBy(String.CASE_INSENSITIVE_ORDER, Song::displayName).reversed(),
    ),
    ArtistAsc(
        SortMenuOption("artist_asc", R.string.sort_song_artist_az, Icons.Default.Person),
        compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist },
    ),
    ArtistDesc(
        SortMenuOption("artist_desc", R.string.sort_song_artist_za, Icons.Default.Person),
        compareBy(String.CASE_INSENSITIVE_ORDER, Song::artist).reversed(),
    ),
    DurationAsc(
        SortMenuOption("duration_asc", R.string.sort_song_duration_asc, Icons.Default.Schedule),
        compareBy { it.duration },
    ),
    DurationDesc(
        SortMenuOption("duration_desc", R.string.sort_song_duration_desc, Icons.Default.Schedule),
        compareByDescending { it.duration },
    ),
}

@Immutable
private enum class AlbumSortMode(
    val option: SortMenuOption,
    val comparator: Comparator<Album>,
) {
    TitleAsc(
        SortMenuOption("title_asc", R.string.sort_album_title_az, Icons.Default.SortByAlpha),
        compareBy(String.CASE_INSENSITIVE_ORDER) { it.title },
    ),
    TitleDesc(
        SortMenuOption("title_desc", R.string.sort_album_title_za, Icons.Default.SortByAlpha),
        compareBy(String.CASE_INSENSITIVE_ORDER, Album::title).reversed(),
    ),
    ArtistAsc(
        SortMenuOption("artist_asc", R.string.sort_album_artist_az, Icons.Default.Person),
        compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist },
    ),
    ArtistDesc(
        SortMenuOption("artist_desc", R.string.sort_album_artist_za, Icons.Default.Person),
        compareBy(String.CASE_INSENSITIVE_ORDER, Album::artist).reversed(),
    ),
    CountDesc(
        SortMenuOption("count_desc", R.string.sort_album_count_desc, Icons.Default.FormatListNumbered),
        compareByDescending { it.numberOfSongs },
    ),
    CountAsc(
        SortMenuOption("count_asc", R.string.sort_album_count_asc, Icons.Default.FormatListNumbered),
        compareBy { it.numberOfSongs },
    ),
}

@Immutable
private enum class ArtistSortMode(
    val option: SortMenuOption,
    val comparator: Comparator<Artist>,
) {
    NameAsc(
        SortMenuOption("name_asc", R.string.sort_artist_name_az, Icons.Default.SortByAlpha),
        compareBy(String.CASE_INSENSITIVE_ORDER) { it.name },
    ),
    NameDesc(
        SortMenuOption("name_desc", R.string.sort_artist_name_za, Icons.Default.SortByAlpha),
        compareBy(String.CASE_INSENSITIVE_ORDER, Artist::name).reversed(),
    ),
    CountDesc(
        SortMenuOption("count_desc", R.string.sort_artist_count_desc, Icons.Default.FormatListNumbered),
        compareByDescending { it.songCount },
    ),
    CountAsc(
        SortMenuOption("count_asc", R.string.sort_artist_count_asc, Icons.Default.FormatListNumbered),
        compareBy { it.songCount },
    ),
}

@Composable
fun MusicPage(bottomContentPadding: Dp = 0.dp) {
    val backStack = LocalBackStack.current
    val homeViewModel: HomeViewModel = koinActivityViewModel()
    val playerViewModel = LocalPlayerViewModel.current
    val reducedMotion = LocalReducedMotion.current
    val itemPlacementSpec = if (reducedMotion) null else ItemPlacementSpringSpec

    val allSongs by homeViewModel.allSongs.collectAsStateWithLifecycle()
    val currentMediaItem by playerViewModel.currentMediaItem.collectAsStateWithLifecycle()
    val playingMediaId = currentMediaItem?.mediaId

    // 专辑与艺人由应用按标签建立，同一位艺人的不同写法、多艺人署名都已合并
    val albums by homeViewModel.albums.collectAsStateWithLifecycle()
    val artists by homeViewModel.artists.collectAsStateWithLifecycle()

    var selectedTab by rememberSaveable { mutableStateOf(MusicBrowserTab.Songs) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var debouncedQuery by rememberSaveable { mutableStateOf("") }
    var songSortMode by rememberSaveable { mutableStateOf(SongSortMode.TitleAsc) }
    var albumSortMode by rememberSaveable { mutableStateOf(AlbumSortMode.TitleAsc) }
    var artistSortMode by rememberSaveable { mutableStateOf(ArtistSortMode.NameAsc) }

    // 刊头、标签和搜索行只在首屏入场；列表在每次切换标签时重新入场。
    var playEntrance by remember { mutableStateOf(true) }
    var playItemEntrance by remember { mutableStateOf(true) }
    var itemEntranceReplayKey by remember { mutableIntStateOf(0) }
    LaunchedEffect(itemEntranceReplayKey) {
        delay(ENTRANCE_GATE_MILLIS)
        playEntrance = false
        playItemEntrance = false
    }
    val itemEntranceOrderBase = if (itemEntranceReplayKey == 0) ENTRANCE_ORDER_ITEM_BASE else 0
    val itemFadeInSpec = if (playItemEntrance) null else ItemFadeInSpec

    val selectTab: (MusicBrowserTab) -> Unit = { tab ->
        if (tab != selectedTab) {
            playEntrance = false
            playItemEntrance = true
            itemEntranceReplayKey++
            selectedTab = tab
        }
    }

    @OptIn(FlowPreview::class)
    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery }
            .debounce(300)
            .collect { debouncedQuery = it }
    }

    val filteredSongs =
        remember(allSongs, debouncedQuery, songSortMode) {
            allSongs
                .filterSongsBy(debouncedQuery)
                .sortedWith(songSortMode.comparator)
        }
    val filteredAlbums =
        remember(albums, debouncedQuery, albumSortMode) {
            albums
                .filterAlbumsBy(debouncedQuery)
                .sortedWith(albumSortMode.comparator)
        }
    val filteredArtists =
        remember(artists, debouncedQuery, artistSortMode) {
            artists
                .filterArtistsBy(debouncedQuery)
                .sortedWith(artistSortMode.comparator)
        }
    val searching = debouncedQuery.isNotBlank()
    val visibleCount =
        when (selectedTab) {
            MusicBrowserTab.Songs -> filteredSongs.size
            MusicBrowserTab.Albums -> filteredAlbums.size
            MusicBrowserTab.Artists -> filteredArtists.size
        }
    val sortIsDefault =
        when (selectedTab) {
            MusicBrowserTab.Songs -> songSortMode == SongSortMode.TitleAsc
            MusicBrowserTab.Albums -> albumSortMode == AlbumSortMode.TitleAsc
            MusicBrowserTab.Artists -> artistSortMode == ArtistSortMode.NameAsc
        }

    val gridState = rememberLazyGridState()
    // 滚离顶部后，圆形按钮从排序图标变成搜索图标。
    val scrolledPastTop by remember {
        derivedStateOf { gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 40 }
    }
    val preferencesManager = koinInject<PreferencesManager>()
    val barCustom = rememberMusicTopBarCustomization(preferencesManager)
    val bottomBarLayout by homeViewModel.bottomBarLayout.collectAsStateWithLifecycle()
    val albumNewStyle by
        preferencesManager
            .getBoolean(PreferencesManager.Keys.ALBUM_NEW_STYLE)
            .collectAsStateWithLifecycle(false)
    val albumStyleTuning = rememberAlbumStyleTuning(preferencesManager)
    val glassEnabled = LocalLiquidGlassConfig.current.enabled
    val glassHazeState = rememberHazeState()
    val pageBackdrop = rememberLayerBackdrop()
    val scope = rememberCoroutineScope()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val dismissKeyboard = {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    // 排序选项在悬浮顶栏的圆形按钮里展开。
    val sortOptions: List<SortMenuOption> =
        when (selectedTab) {
            MusicBrowserTab.Songs -> SongSortMode.entries.map { it.option }
            MusicBrowserTab.Albums -> AlbumSortMode.entries.map { it.option }
            MusicBrowserTab.Artists -> ArtistSortMode.entries.map { it.option }
        }
    val selectedSortId: String =
        when (selectedTab) {
            MusicBrowserTab.Songs -> songSortMode.option.id
            MusicBrowserTab.Albums -> albumSortMode.option.id
            MusicBrowserTab.Artists -> artistSortMode.option.id
        }
    val onSortSelect: (String) -> Unit = { id ->
        when (selectedTab) {
            MusicBrowserTab.Songs ->
                SongSortMode.entries.firstOrNull { it.option.id == id }?.let { songSortMode = it }
            MusicBrowserTab.Albums ->
                AlbumSortMode.entries.firstOrNull { it.option.id == id }?.let { albumSortMode = it }
            MusicBrowserTab.Artists ->
                ArtistSortMode.entries.firstOrNull { it.option.id == id }?.let { artistSortMode = it }
        }
    }

    // 用户开始浏览时结束本轮入场，避免滚动中新出现或复用的条目继续等待入场。
    LaunchedEffect(gridState, itemEntranceReplayKey) {
        snapshotFlow { gridState.isScrollInProgress }
            .filter { it }
            .collect {
                playEntrance = false
                playItemEntrance = false
                dismissKeyboard()
            }
    }

    // 顶栏的玻璃只取样下面的列表，不取样自己；列表图层是页面内的独立背景源。
    CompositionLocalProvider(LocalGlassV2Backdrop provides pageBackdrop) {
    Box(modifier = Modifier.fillMaxSize()) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .hazeSource(glassHazeState)
                .liquidGlassV2Source()
                .background(MaterialTheme.colorScheme.background),
    ) {
        LazyVerticalGrid(
            state = gridState,
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    start = LayoutTokens.MusicHeaderHorizontalPadding,
                    end = LayoutTokens.MusicHeaderHorizontalPadding,
                    top = statusBarTop + barCustom.top.dp + maxOf(barCustom.height, 44).dp + Spacing.Large,
                    bottom = bottomContentPadding + Spacing.Large,
                ),
            horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
            overscrollEffect = rememberIOSOverScrollEffect(Orientation.Vertical),
        ) {
            when (selectedTab) {
                MusicBrowserTab.Songs -> {
                    if (filteredSongs.isEmpty()) {
                        item(key = "songs_empty", span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                            MusicEmptyState(
                                title =
                                    stringResource(
                                        if (allSongs.isEmpty()) {
                                            R.string.music_no_songs_title
                                        } else {
                                            R.string.music_empty_songs_title
                                        },
                                    ),
                                subtitle =
                                    stringResource(
                                        if (allSongs.isEmpty()) {
                                            R.string.music_no_songs_subtitle
                                        } else {
                                            R.string.music_empty_songs_subtitle
                                        },
                                    ),
                                actionLabel = stringResource(R.string.scan_local_music).takeIf { allSongs.isEmpty() },
                                onActionClick =
                                    {
                                        backStack.add(ScannerRoute)
                                        Unit
                                    }.takeIf { allSongs.isEmpty() },
                                modifier =
                                    Modifier
                                        .animateItem(
                                            fadeInSpec = itemFadeInSpec,
                                            placementSpec = null,
                                            fadeOutSpec = ItemFadeOutSpec,
                                        ).entrance(
                                            order = itemEntranceOrderBase,
                                            play = playItemEntrance,
                                            replayKey = itemEntranceReplayKey,
                                        ),
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = filteredSongs,
                            key = { _, song -> "song:${song.mediaStoreId}" },
                            span = { _, _ -> GridItemSpan(maxLineSpan) },
                            contentType = { _, _ -> "song" },
                        ) { index, song ->
                            MusicSongRow(
                                song = song,
                                isPlaying = playingMediaId == song.mediaStoreId.toString(),
                                onClick = {
                                    playerViewModel.updatePlaylistWithSongs(
                                        songs = filteredSongs,
                                        startSong = song,
                                        autoStart = true,
                                    )
                                },
                                onLongClick = { backStack.add(SongMenuRoute(song)) },
                                modifier =
                                    Modifier
                                        .animateItem(
                                            fadeInSpec = itemFadeInSpec,
                                            placementSpec = itemPlacementSpec,
                                            fadeOutSpec = ItemFadeOutSpec,
                                        ).entrance(
                                            order = minOf(index, ENTRANCE_MAX_ORDER - ENTRANCE_ORDER_ITEM_BASE) + itemEntranceOrderBase,
                                            play = playItemEntrance,
                                            replayKey = itemEntranceReplayKey,
                                        ),
                            )
                        }
                    }
                }

                MusicBrowserTab.Albums -> {
                    if (filteredAlbums.isEmpty()) {
                        item(key = "albums_empty", span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                            MusicEmptyState(
                                title =
                                    stringResource(
                                        if (albums.isEmpty()) {
                                            R.string.music_no_albums_title
                                        } else {
                                            R.string.music_empty_albums_title
                                        },
                                    ),
                                subtitle = stringResource(R.string.music_empty_albums_subtitle),
                                modifier =
                                    Modifier
                                        .animateItem(
                                            fadeInSpec = itemFadeInSpec,
                                            placementSpec = null,
                                            fadeOutSpec = ItemFadeOutSpec,
                                        ).entrance(
                                            order = itemEntranceOrderBase,
                                            play = playItemEntrance,
                                            replayKey = itemEntranceReplayKey,
                                        ),
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = filteredAlbums,
                            key = { _, album -> "album:${album.id}" },
                            contentType = { _, _ -> "album" },
                        ) { index, album ->
                            // 同排两张卡共用一个槽位
                            val row = index / 2
                            MusicAlbumCard(
                                album = album,
                                newStyle = albumNewStyle,
                                tuning = albumStyleTuning,
                                onClick = { backStack.add(AlbumDetailRoute(album)) },
                                modifier =
                                    Modifier
                                        .animateItem(
                                            fadeInSpec = itemFadeInSpec,
                                            placementSpec = itemPlacementSpec,
                                            fadeOutSpec = ItemFadeOutSpec,
                                        ).entrance(
                                            order = minOf(row, ENTRANCE_MAX_ORDER - ENTRANCE_ORDER_ITEM_BASE) + itemEntranceOrderBase,
                                            play = playItemEntrance,
                                            replayKey = itemEntranceReplayKey,
                                        ),
                            )
                        }
                    }
                }

                MusicBrowserTab.Artists -> {
                    if (filteredArtists.isEmpty()) {
                        item(key = "artists_empty", span = { GridItemSpan(maxLineSpan) }, contentType = "empty") {
                            MusicEmptyState(
                                title =
                                    stringResource(
                                        if (artists.isEmpty()) {
                                            R.string.music_no_artists_title
                                        } else {
                                            R.string.music_empty_artists_title
                                        },
                                    ),
                                subtitle = stringResource(R.string.music_empty_artists_subtitle),
                                modifier =
                                    Modifier
                                        .animateItem(
                                            fadeInSpec = itemFadeInSpec,
                                            placementSpec = null,
                                            fadeOutSpec = ItemFadeOutSpec,
                                        ).entrance(
                                            order = itemEntranceOrderBase,
                                            play = playItemEntrance,
                                            replayKey = itemEntranceReplayKey,
                                        ),
                            )
                        }
                    } else {
                        itemsIndexed(
                            items = filteredArtists,
                            key = { _, artist -> "artist:${artist.artistId}" },
                            span = { _, _ -> GridItemSpan(maxLineSpan) },
                            contentType = { _, _ -> "artist" },
                        ) { index, artist ->
                            MusicArtistRow(
                                artist = artist,
                                onClick = { backStack.add(ArtistDetailRoute(artist)) },
                                modifier =
                                    Modifier
                                        .animateItem(
                                            fadeInSpec = itemFadeInSpec,
                                            placementSpec = itemPlacementSpec,
                                            fadeOutSpec = ItemFadeOutSpec,
                                        ).entrance(
                                            order = minOf(index, ENTRANCE_MAX_ORDER - ENTRANCE_ORDER_ITEM_BASE) + itemEntranceOrderBase,
                                            play = playItemEntrance,
                                            replayKey = itemEntranceReplayKey,
                                        ),
                            )
                        }
                    }
                }
            }
        }

        } // 列表图层结束

        MusicTopBlur(hazeState = glassHazeState, custom = barCustom)

        MusicFloatingBar(
            selectedTab = selectedTab,
            onSelectTab = selectTab,
            showSearchIcon = scrolledPastTop,
            sortActive = !sortIsDefault,
            sortOptions = sortOptions,
            selectedSortId = selectedSortId,
            onSortSelect = onSortSelect,
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            custom = barCustom,
            hazeState = glassHazeState,
            glassEnabled = glassEnabled,
            layout = bottomBarLayout,
        )
    }
    }
}

/** 让条目越过网格水平内边距通栏铺满，内容由自身 padding 对齐回页边距 */
private fun Modifier.bleedHorizontal(amount: Dp): Modifier =
    layout { measurable, constraints ->
        val extra = amount.roundToPx() * 2
        val placeable =
            measurable.measure(
                constraints.copy(
                    minWidth = constraints.minWidth + extra,
                    maxWidth = if (constraints.hasBoundedWidth) constraints.maxWidth + extra else constraints.maxWidth,
                ),
            )
        val width = (placeable.width - extra).coerceIn(constraints.minWidth, constraints.maxWidth)
        layout(width, placeable.height) {
            placeable.place(-extra / 2, 0)
        }
    }

/** 分段切换：胶囊轨道 + 弹性滑块，手势与运动复用底栏指示器状态 */
@Composable
private fun MusicSegmentedTabs(
    selectedTab: MusicBrowserTab,
    onSelect: (MusicBrowserTab) -> Unit,
    modifier: Modifier = Modifier,
    labelMode: Int = BottomBarLabelMode.IconAndText,
) {
    val tabs = MusicBrowserTab.entries
    val scope = rememberCoroutineScope()
    val indicator = remember(scope) { GlassNavigationIndicatorState(selectedTab.ordinal, tabs.size, scope) }
    val currentSelection by rememberUpdatedState(selectedTab)
    val onSelection by rememberUpdatedState(onSelect)
    val reducedMotion = LocalReducedMotion.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme
    var bounds by remember { mutableStateOf(IntSize.Zero) }
    val inset = with(density) { SegmentedInset.toPx() }
    val segmentWidth = ((bounds.width - 2f * inset) / tabs.size).coerceAtLeast(0f)
    val interacting = indicator.isInteracting && !reducedMotion
    val press by animateFloatAsState(
        targetValue = if (interacting) 1f else 0f,
        animationSpec =
            when {
                reducedMotion -> snap()
                interacting -> spring(dampingRatio = 0.85f, stiffness = 900f)
                else -> tween(140)
            },
        label = "musicSegmentPress",
    )

    LaunchedEffect(selectedTab, reducedMotion) { indicator.synchronize(selectedTab.ordinal, reducedMotion) }

    Box(
        modifier =
            modifier
                .selectableGroup()
                .onSizeChanged { bounds = it }
                .pointerInput(indicator, segmentWidth, isLtr, reducedMotion) {
                    if (segmentWidth <= 0f) return@pointerInput
                    val direction = if (isLtr) 1f else -1f
                    val lastIndex = tabs.lastIndex
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val tracker = VelocityTracker()
                        tracker.addPosition(down.uptimeMillis, down.position)
                        indicator.press()
                        try {
                            val dragStart =
                                awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                                    // 从滑块当前位置起步跟手
                                    indicator.beginDrag()
                                    tracker.addPosition(change.uptimeMillis, change.position)
                                    indicator.dragBy(direction * overSlop / segmentWidth, 0f)
                                    change.consume()
                                }
                            if (dragStart != null) {
                                val completed =
                                    horizontalDrag(dragStart.id) { change ->
                                        tracker.addPosition(change.uptimeMillis, change.position)
                                        indicator.dragBy(
                                            direction * change.positionChange().x / segmentWidth,
                                            direction * tracker.calculateVelocity().x / segmentWidth,
                                        )
                                        change.consume()
                                    }
                                val target =
                                    if (completed) {
                                        tabs[indicator.nearestIndex().coerceIn(0, lastIndex)]
                                    } else {
                                        currentSelection
                                    }
                                indicator.animateTo(target.ordinal, reducedMotion)
                                if (completed && target != currentSelection) onSelection(target)
                            }
                        } finally {
                            indicator.release()
                            if (indicator.isDragging) indicator.animateTo(currentSelection.ordinal, reducedMotion)
                        }
                    }
                },
    ) {
        // 滑块在文字下层，位置在图层阶段读取
        if (bounds.width > 0 && bounds.height > 0 && segmentWidth > 0f) {
            val thumbHeight = (bounds.height - 2f * inset).coerceAtLeast(1f)
            Box(
                modifier =
                    Modifier
                        .size(with(density) { segmentWidth.toDp() }, with(density) { thumbHeight.toDp() })
                        .graphicsLayer {
                            val stretch = (abs(indicator.velocity) / 6f).coerceIn(0f, 1f) * press
                            scaleX = 1f + 0.03f * press + 0.02f * stretch
                            scaleY = 1f + 0.05f * press - 0.02f * stretch
                            translationX =
                                if (isLtr) {
                                    inset + indicator.position * segmentWidth
                                } else {
                                    bounds.width - inset - (indicator.position + 1f) * segmentWidth
                                }
                            translationY = inset
                        }.clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.09f))
                        .clearAndSetSemantics {},
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(SegmentedInset),
        ) {
            tabs.forEach { tab ->
                val selected = tab == selectedTab
                val labelColor by animateColorAsState(
                    targetValue = if (selected) colors.primary else colors.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 200, easing = EaseOutEmphasized),
                    label = "musicSegmentLabel",
                )
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .selectable(
                                selected = selected,
                                role = Role.Tab,
                                interactionSource = null,
                                indication = null,
                                onClick = {
                                    indicator.animateTo(tab.ordinal, reducedMotion)
                                    if (tab != currentSelection) onSelection(tab)
                                },
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        if (labelMode != BottomBarLabelMode.TextOnly) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription =
                                    if (labelMode == BottomBarLabelMode.IconOnly) stringResource(tab.titleRes) else null,
                                tint = labelColor,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        if (labelMode != BottomBarLabelMode.IconOnly) {
                            Text(
                                text = stringResource(tab.titleRes),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = labelColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 顶栏后面的渐进模糊（和歌词页的顶栏模糊同一思路）：列表在模糊带后面滚动，
 * 软边开启时模糊在下沿渐隐。模糊带不含自己，只采样下面的列表。
 */
@OptIn(ExperimentalHazeApi::class)
@Composable
private fun MusicTopBlur(
    hazeState: HazeState,
    custom: MusicTopBarCustomization,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val background = colors.background
    val solidHeight =
        (statusBarTop + custom.top.dp + maxOf(custom.height, 44).dp + custom.blurPosition.dp).coerceAtLeast(0.dp)
    val blurOn = custom.blur == 1 && custom.blurAmount > 0
    val soft = custom.softEdge == 1 && custom.edgeFade > 0
    Box(modifier = modifier.fillMaxWidth()) {
        if (blurOn) {
            val amount = custom.blurAmount
            val hardStyle =
                remember(background, amount) {
                    HazeBlurStyle {
                        blurRadius(amount.dp)
                        noiseFactor(0f)
                        fallbackColorEffect(HazeColorEffect.tint(background.copy(alpha = 0.85f)))
                    }
                }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(solidHeight)
                        .hazeBlur(
                            input = HazeInput.Sources(hazeState),
                            style = hardStyle,
                            performanceMode = HazePerformanceMode.Balanced,
                        ),
            )
            if (soft) {
                val fadeStyle =
                    remember(background, amount) {
                        HazeBlurStyle {
                            blurRadius(amount.dp)
                            noiseFactor(0f)
                            fallbackColorEffect(HazeColorEffect.tint(background.copy(alpha = 0.85f)))
                            progressive(HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f))
                        }
                    }
                Box(
                    modifier =
                        Modifier
                            .offset(y = solidHeight)
                            .fillMaxWidth()
                            .height(custom.edgeFade.dp)
                            .hazeBlur(
                                input = HazeInput.Sources(hazeState),
                                style = fadeStyle,
                                performanceMode = HazePerformanceMode.Balanced,
                            ),
                )
            }
        }
        // 没有软边（或模糊关闭）时，用一条细线把顶栏和列表分开。
        if (!blurOn || !soft) {
            HorizontalDivider(
                modifier = Modifier.offset(y = solidHeight),
                color = colors.outlineVariant.copy(alpha = 0.14f),
            )
        }
    }
}

private enum class BarPanel { None, Sort, Search }

private val BarGap = 10.dp
private val MenuRowHeight = 44.dp
private val MenuWidth = 224.dp
private val MenuCorner = 24.dp
private val MenuGap = 8.dp

/**
 * 悬浮顶栏：左侧分段（歌曲 / 专辑 / 歌手），右侧独立的圆形按钮。
 * 圆形按钮在列表顶部是排序键，滚离顶部后变成搜索键；点击后它自己展开成排序菜单（在顶栏下方），
 * 或融进左边的分段，分段向右铺满变成搜索栏。玻璃材质复用底栏的 playerBarSurface，所以跟随设置页的液态玻璃开关与调节。
 */
@Composable
private fun MusicFloatingBar(
    selectedTab: MusicBrowserTab,
    onSelectTab: (MusicBrowserTab) -> Unit,
    showSearchIcon: Boolean,
    sortActive: Boolean,
    sortOptions: List<SortMenuOption>,
    selectedSortId: String,
    onSortSelect: (String) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    custom: MusicTopBarCustomization,
    hazeState: HazeState,
    glassEnabled: Boolean,
    layout: BottomBarLayout,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    // 与底栏同一套外观参数：同样的容器色（玻璃着色）、同样的左右边距，位置/宽度百分比才会得到相同的结果。
    val appearance = BottomBarAppearance.forLayout(layout)
    val container = appearance.navigationContainerColor(colors)
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val reducedMotion = LocalReducedMotion.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    var panel by remember { mutableStateOf(BarPanel.None) }
    val close = {
        panel = BarPanel.None
        keyboardController?.hide()
        focusManager.clearFocus()
    }
    BackHandler(enabled = panel != BarPanel.None) { close() }
    LaunchedEffect(panel) {
        if (panel == BarPanel.Search) {
            delay(180)
            runCatching { focusRequester.requestFocus() }
        }
    }

    val morphSpec: AnimationSpec<Float> =
        if (reducedMotion) snap() else spring(dampingRatio = 0.82f, stiffness = 520f)
    val sortP by animateFloatAsState(
        targetValue = if (panel == BarPanel.Sort) 1f else 0f,
        animationSpec = morphSpec,
        label = "musicBarSort",
    )
    val searchP by animateFloatAsState(
        targetValue = if (panel == BarPanel.Search) 1f else 0f,
        animationSpec = morphSpec,
        label = "musicBarSearch",
    )
    // 圆角和分段宽度不能越界（弹簧会略过冲），所以另取限幅后的进度。
    val sortC = sortP.coerceIn(0f, 1f)
    val searchC = searchP.coerceIn(0f, 1f)
    val sortIconAlpha by animateFloatAsState(
        targetValue = if (showSearchIcon) 0f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "musicBarSortIcon",
    )
    val searchIconAlpha by animateFloatAsState(
        targetValue = if (showSearchIcon) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "musicBarSearchIcon",
    )
    val triggerLabel = stringResource(if (showSearchIcon) R.string.search else R.string.music_sort_cd)
    val searchHint = stringResource(selectedTab.searchHintRes)

    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .padding(horizontal = appearance.horizontalMargin),
    ) {
        val barHeight = maxOf(custom.height, 44).dp
        val barWidth = maxWidth * (custom.width.coerceIn(55, 100) / 100f)
        val barLeft = (maxWidth - barWidth) * (custom.horizontal.coerceIn(0, 100) / 100f)
        val barTop = statusBarTop + custom.top.dp
        val barRight = barLeft + barWidth
        val menuHeight = MenuRowHeight * sortOptions.size + 12.dp
        val menuWidth = minOf(MenuWidth, barWidth)

        // 点面板外任意处收起。
        if (panel != BarPanel.None) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) { detectTapGestures { close() } },
            )
        }

        // 同一个玻璃分段：排序菜单或搜索展开时向右铺满；搜索时分段文字淡出、输入框淡入，圆形按钮融进去。
        val pillWidth = lerp(barWidth - barHeight - BarGap, barWidth, maxOf(sortC, searchC))
        Box(
            modifier =
                Modifier
                    .offset(x = barLeft, y = barTop)
                    .size(pillWidth, barHeight)
                    .playerBarSurface(hazeState, container, glassEnabled, RoundedCornerShape(percent = 50), false),
        ) {
            if (searchP < 0.99f) {
                MusicSegmentedTabs(
                    selectedTab = selectedTab,
                    onSelect = onSelectTab,
                    labelMode = custom.labelMode,
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = 1f - searchC },
                )
            }
            if (searchP > 0.01f) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(start = 18.dp, end = 4.dp)
                            .graphicsLayer { alpha = ((searchC - 0.5f) / 0.5f).coerceIn(0f, 1f) },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = colors.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(Spacing.Small))
                    AnimatedCursorTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.weight(1f),
                        textStyle =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = colors.onSurface,
                            ),
                        cursorColor = colors.primary,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        onImeAction = { close() },
                        focusRequester = focusRequester,
                        placeholder = {
                            Text(
                                text = searchHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                            )
                        },
                    )
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            close()
                        },
                        modifier = Modifier.size(40.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            tint = colors.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        // 圆形按钮：收起时是圆形，排序时长成菜单；搜索时淡出并融进左边的分段。
        val panelWidth =
            (barHeight + (menuWidth - barHeight) * sortP).coerceAtLeast(barHeight)
        val panelHeight = barHeight + (menuHeight - barHeight) * sortP
        val panelTop = barTop + (barHeight + MenuGap) * sortP
        val panelLeft = barRight - panelWidth
        val panelShape = RoundedCornerShape(lerp(barHeight / 2, MenuCorner, sortC))
        Box(
            modifier =
                Modifier
                    .offset(x = panelLeft, y = panelTop)
                    .size(panelWidth, panelHeight)
                    .graphicsLayer {
                        val melt = 1f - 0.4f * searchC
                        alpha = 1f - searchC
                        scaleX = melt
                        scaleY = melt
                        translationX = -16.dp.toPx() * searchC
                    }.playerBarSurface(hazeState, container, glassEnabled, panelShape, false),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(panelShape)
                        .then(
                            if (panel == BarPanel.None) {
                                Modifier.clickHighlight(
                                    onClickLabel = triggerLabel,
                                    onClick = { panel = if (showSearchIcon) BarPanel.Search else BarPanel.Sort },
                                )
                            } else {
                                Modifier
                            },
                        ),
            ) {
                // 圆形按钮上的图标：排序 / 搜索互相转换，面板展开时淡出。
                Box(
                    modifier =
                        Modifier
                            .size(barHeight)
                            .align(Alignment.TopEnd)
                            .graphicsLayer { alpha = (1f - sortC - searchC).coerceIn(0f, 1f) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = if (sortActive) colors.primary else colors.onSurfaceVariant,
                        modifier =
                            Modifier
                                .size(20.dp)
                                .graphicsLayer {
                                    alpha = sortIconAlpha
                                    rotationZ = (1f - sortIconAlpha) * -90f
                                },
                    )
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = if (query.isNotBlank()) colors.primary else colors.onSurfaceVariant,
                        modifier =
                            Modifier
                                .size(20.dp)
                                .graphicsLayer {
                                    alpha = searchIconAlpha
                                    rotationZ = (1f - searchIconAlpha) * 90f
                                },
                    )
                }

                if (sortP > 0.01f) {
                    Column(
                        modifier =
                            Modifier
                                .width(menuWidth)
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .graphicsLayer { alpha = ((sortC - 0.35f) / 0.65f).coerceIn(0f, 1f) },
                    ) {
                        sortOptions.forEach { option ->
                            val selected = option.id == selectedSortId
                            Row(
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .height(MenuRowHeight)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(if (selected) colors.primary.copy(alpha = 0.14f) else Color.Transparent)
                                        .clickHighlight(
                                            onClick = {
                                                onSortSelect(option.id)
                                                close()
                                            },
                                        ).padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Icon(
                                    imageVector = option.icon,
                                    contentDescription = null,
                                    tint = if (selected) colors.primary else colors.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = stringResource(option.labelRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                    color = if (selected) colors.primary else colors.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

/** 通栏歌曲行：播放中标题转主题色、行尾换律动条 */
@Composable
private fun MusicSongRow(
    song: Song,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val highlight by animateColorAsState(
        targetValue = if (isPlaying) colors.primaryContainer.copy(alpha = 0.35f) else colors.primaryContainer.copy(alpha = 0.0f),
        label = "musicSongHighlight",
    )
    val titleColor by animateColorAsState(
        targetValue = if (isPlaying) colors.primary else colors.onSurface,
        label = "musicSongTitle",
    )
    Row(
        modifier =
            modifier
                .bleedHorizontal(LayoutTokens.MusicHeaderHorizontalPadding)
                .fillMaxWidth()
                .drawBehind { drawRect(highlight) }
                .combinedClickHighlight(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ).padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding, vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        AudioCover(
            artwork = song.artwork(),
            modifier =
                Modifier
                    .size(RowCoverSize)
                    .clip(Shapes.MediumCornerBasedShape),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text =
                    listOf(song.artist.ifBlank { stringResource(R.string.unknown_artist) }, song.album)
                        .filter(String::isNotBlank)
                        .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AnimatedContent(
            targetState = isPlaying,
            transitionSpec = {
                (
                    fadeIn(tween(durationMillis = 200, easing = EaseOutEmphasized)) +
                        scaleIn(
                            animationSpec = tween(durationMillis = 200, easing = EaseOutEmphasized),
                            initialScale = ScaleEnterFrom,
                        )
                ) togetherWith
                    (
                        fadeOut(tween(durationMillis = 120)) +
                            scaleOut(animationSpec = tween(durationMillis = 120), targetScale = ScaleExitTo)
                    )
            },
            label = "musicSongTrailing",
        ) { playing ->
            if (playing) {
                PlayingBarsIndicator(modifier = Modifier.widthIn(min = 36.dp))
            } else {
                Text(
                    text = song.getFormattedDuration(),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.widthIn(min = 36.dp),
                )
            }
        }
    }
}

/**
 * 专辑卡：方形封面 + 标题 + 「歌手 · N 首」。
 * newStyle 开启时信息放进封面底部，渐变色取自封面主色；阴影、文字、圆角、边距、大小与高光由 tuning 调节。
 */
@Composable
private fun MusicAlbumCard(
    album: Album,
    newStyle: Boolean,
    tuning: AlbumStyleTuning,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = "${album.artistDisplay()} · ${stringResource(R.string.songs_count_format, album.numberOfSongs)}"
    val shape = if (newStyle) RoundedCornerShape(tuning.cornerDp.dp) else Shapes.ExtraLargeCornerBasedShape
    val sizeFraction = if (newStyle) (tuning.sizePercent / 100f).coerceIn(0.3f, 1f) else 1f
    Box(
        modifier = modifier.padding(bottom = Spacing.Medium),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(sizeFraction)
                    .clip(shape)
                    .clickHighlight(onClick = onClick),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(shape)
                        .then(if (newStyle) Modifier.albumHighlight(tuning, tuning.cornerDp) else Modifier),
            ) {
                AudioCover(
                    artwork = album.artwork(),
                    modifier = Modifier.fillMaxSize(),
                )
                if (newStyle) {
                    val artColor = rememberAlbumArtColor(album.artwork())
                    val shade =
                        androidx.compose.ui.graphics
                            .lerp(artColor ?: Color.Black, Color.Black, 0.4f)
                    val textPad = tuning.textPaddingDp.dp
                    val titleSize = tuning.textSizeSp.coerceIn(8, 30).sp
                    val subtitleSize = (tuning.textSizeSp - 2).coerceAtLeast(8).sp
                    Column(
                        modifier =
                            Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(
                                    androidx.compose.ui.graphics.Brush.verticalGradient(
                                        listOf(
                                            Color.Transparent,
                                            shade.copy(alpha = (tuning.shadowPercent / 100f).coerceIn(0f, 1f)),
                                        ),
                                    ),
                                ).padding(
                                    start = textPad,
                                    end = textPad,
                                    top = 28.dp,
                                    bottom = (tuning.textPaddingDp - 2).coerceAtLeast(0).dp,
                                ),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = album.title,
                            style =
                                MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = titleSize,
                                    lineHeight = TextUnit.Unspecified,
                                ),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = subtitle,
                            style =
                                MaterialTheme.typography.bodySmall.copy(
                                    fontSize = subtitleSize,
                                    lineHeight = TextUnit.Unspecified,
                                ),
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            if (!newStyle) {
                Column(
                    modifier = Modifier.padding(bottom = Spacing.Small),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = album.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** 通栏歌手行：圆形封面 + 名称 + 歌曲数 */
@Composable
private fun MusicArtistRow(
    artist: Artist,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .bleedHorizontal(LayoutTokens.MusicHeaderHorizontalPadding)
                .fillMaxWidth()
                .clickHighlight(onClick = onClick)
                .padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding, vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        AudioCover(
            artwork = artist.artwork(),
            modifier =
                Modifier
                    .size(RowCoverSize)
                    .clip(CircleShape),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.displayName(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(R.string.songs_count_format, artist.songCount),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(20.dp),
        )
    }
}

/** 播放中指示：三根错相起伏的竖条，降级动效时静止 */
@Composable
private fun PlayingBarsIndicator(modifier: Modifier = Modifier) {
    val reducedMotion = LocalReducedMotion.current
    val transition = rememberInfiniteTransition(label = "musicPlayingBars")
    val barHeights =
        List(3) { i ->
            transition.animateFloat(
                initialValue = 4f,
                targetValue = 14f,
                animationSpec =
                    infiniteRepeatable(
                        animation =
                            tween(
                                durationMillis = 420 + i * 130,
                                easing = FastOutSlowInEasing,
                            ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                label = "bar$i",
            )
        }
    val staticHeights = listOf(8f, 13f, 6f)
    Row(
        modifier = modifier.height(16.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.End),
        verticalAlignment = Alignment.Bottom,
    ) {
        barHeights.forEachIndexed { index, heightAnim ->
            val barHeight = if (reducedMotion) staticHeights[index] else heightAnim.value
            Box(
                Modifier
                    .width(3.dp)
                    .height(barHeight.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

/** 空态：浮动音符 + 文案，可选引导药丸 */
@Composable
private fun MusicEmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val reducedMotion = LocalReducedMotion.current
    val floatTransition = rememberInfiniteTransition(label = "musicEmptyFloat")
    val bob by floatTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "musicEmptyBob",
    )
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = Spacing.ExtraLarge, bottom = Spacing.Huge),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        Box(
            modifier =
                Modifier
                    .size(56.dp)
                    .graphicsLayer {
                        if (!reducedMotion) {
                            translationY = bob * 5.dp.toPx()
                            rotationZ = bob * 6f
                        }
                    }.clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        if (actionLabel != null && onActionClick != null) {
            Row(
                modifier =
                    Modifier
                        .padding(top = Spacing.ExtraSmall)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickHighlight(onClick = onActionClick)
                        .padding(horizontal = Spacing.Large, vertical = Spacing.Small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
            ) {
                Icon(
                    imageVector = Icons.Default.Scanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}

private fun List<Song>.filterSongsBy(query: String): List<Song> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter { song ->
        song.displayName.contains(normalized, ignoreCase = true) ||
            song.artist.contains(normalized, ignoreCase = true) ||
            song.album.contains(normalized, ignoreCase = true)
    }
}

private fun List<Album>.filterAlbumsBy(query: String): List<Album> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter { album ->
        album.title.contains(normalized, ignoreCase = true) ||
            album.artist.contains(normalized, ignoreCase = true)
    }
}

private fun List<Artist>.filterArtistsBy(query: String): List<Artist> {
    val normalized = query.trim()
    if (normalized.isEmpty()) return this
    return filter { artist ->
        artist.name.contains(normalized, ignoreCase = true)
    }
}
