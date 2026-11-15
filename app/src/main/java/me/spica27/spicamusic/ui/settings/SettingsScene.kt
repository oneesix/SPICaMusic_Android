package me.spica27.spicamusic.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.LensBlur
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.MusicOff
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.common.collect.ImmutableList
import me.spica27.spicamusic.R
import me.spica27.spicamusic.common.entity.BottomBarLayout
import me.spica27.spicamusic.common.entity.DynamicCoverType
import me.spica27.spicamusic.common.entity.DynamicSpectrumBackground
import me.spica27.spicamusic.common.entity.ProgressBarStyle
import me.spica27.spicamusic.common.entity.ThemeColorStyle
import me.spica27.spicamusic.ui.glass.LiquidGlassSetting
import me.spica27.spicamusic.ui.glass.LiquidGlassTuning
import me.spica27.spicamusic.ui.glass.LiquidGlassV2Preview
import me.spica27.spicamusic.ui.glass.LiquidGlassV2Setting
import me.spica27.spicamusic.ui.glass.LiquidGlassV2Tuning
import me.spica27.spicamusic.ui.glass.tenthsText
import me.spica27.spicamusic.ui.home.page.AlbumStyleSetting
import me.spica27.spicamusic.ui.home.page.AlbumStyleTuning
import me.spica27.spicamusic.ui.navigation.AboutRoute
import me.spica27.spicamusic.ui.navigation.AudioEffectsRoute
import me.spica27.spicamusic.ui.navigation.IgnoredSongsRoute
import me.spica27.spicamusic.ui.navigation.LocalBackStack
import me.spica27.spicamusic.ui.navigation.SleepTimerRoute
import me.spica27.spicamusic.ui.player.LocalPlayerViewModel
import me.spica27.spicamusic.ui.player.PillPlayerTuning
import me.spica27.spicamusic.ui.player.formatSleepTimerRemaining
import me.spica27.spicamusic.ui.theme.EaseOutEmphasized
import me.spica27.spicamusic.ui.theme.LayoutTokens
import me.spica27.spicamusic.ui.theme.ScaleEnterFrom
import me.spica27.spicamusic.ui.theme.Shapes
import me.spica27.spicamusic.ui.theme.Spacing
import me.spica27.spicamusic.ui.theme.entrance
import me.spica27.spicamusic.ui.widget.LyricsSettingsChips
import me.spica27.spicamusic.ui.widget.LyricsSettingsResetButton
import me.spica27.spicamusic.ui.widget.LyricsSettingsSection
import me.spica27.spicamusic.ui.widget.LyricsSettingsSlider
import me.spica27.spicamusic.ui.widget.LyricsSettingsToggle
import me.spica27.spicamusic.ui.widget.SmoothSettings
import me.spica27.spicamusic.ui.widget.SmoothThemeMode
import me.spica27.spicamusic.ui.widget.clickHighlight
import me.spica27.spicamusic.ui.widget.rememberIOSOverScrollEffect
import org.koin.compose.viewmodel.koinViewModel

/**
 * 设置页
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen() {
    val backStack = LocalBackStack.current
    val viewModel: SettingsViewModel = koinViewModel()

    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val liquidGlassEnabled by viewModel.liquidGlassEnabled.collectAsStateWithLifecycle()
    val liquidGlassTuning by viewModel.liquidGlassTuning.collectAsStateWithLifecycle()
    val liquidGlassV2Enabled by viewModel.liquidGlassV2Enabled.collectAsStateWithLifecycle()
    val liquidGlassV2Tuning by viewModel.liquidGlassV2Tuning.collectAsStateWithLifecycle()
    val bottomBarCustomization by viewModel.bottomBarCustomization.collectAsStateWithLifecycle()
    val bottomBarLayout by viewModel.bottomBarLayout.collectAsStateWithLifecycle()
    val musicTopBarCustomization by viewModel.musicTopBarCustomization.collectAsStateWithLifecycle()
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val albumNewStyle by viewModel.albumNewStyle.collectAsStateWithLifecycle()
    val albumStyleTuning by viewModel.albumStyleTuning.collectAsStateWithLifecycle()
    var albumStyleExpanded by rememberSaveable { mutableStateOf(false) }
    val backgroundValue by viewModel.dynamicSpectrumBackground.collectAsStateWithLifecycle()
    val smooth by viewModel.smooth.collectAsStateWithLifecycle()
    val coverTapValue by viewModel.dynamicCoverType.collectAsStateWithLifecycle()
    val progressWaveformValue by viewModel.progressBarStyle.collectAsStateWithLifecycle()
    val playerStyleValue by viewModel.playerStyle.collectAsStateWithLifecycle()
    val pillTuning by viewModel.pillPlayerTuning.collectAsStateWithLifecycle()
    val colorStyleValue by viewModel.themeColorStyle.collectAsStateWithLifecycle()
    val playerViewModel = LocalPlayerViewModel.current
    val sleepTimer by playerViewModel.sleepTimer.collectAsStateWithLifecycle()

    // 只在页面首次呈现时播放一次入场
    var entrancePlayed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        entrancePlayed = true
    }

    // 一次只展开一个选项组：避免整个页面同时膨胀成一大片选项海
    var expandedRowKey by rememberSaveable { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val mastheadGone by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding =
                PaddingValues(
                    top = statusBarTop + 56.dp,
                    bottom = 96.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.Large),
            overscrollEffect = rememberIOSOverScrollEffect(Orientation.Vertical),
        ) {
            item(key = "settings_masthead") {
                SettingsMasthead(
                    modifier =
                        Modifier
                            .padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding)
                            .padding(top = Spacing.Large)
                            .entrance(order = 0, play = !entrancePlayed)
                            .graphicsLayer {
                                val t = mastheadCollapse(listState)
                                transformOrigin = TransformOrigin(0f, 0f)
                                alpha = 1f - t
                                translationY = -t * 16.dp.toPx()
                                scaleX = 1f - 0.18f * t
                                scaleY = 1f - 0.18f * t
                            },
                )
            }

            // 液态玻璃 V2 预览：开启后固定在顶部，调节任何选项都能立即看到。
            if (liquidGlassV2Enabled) {
                stickyHeader(key = "settings_glass_v2_preview") {
                    LiquidGlassV2Preview(
                        tuning = liquidGlassV2Tuning,
                        modifier = Modifier.padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding),
                    )
                }
            }

            item(key = "settings_appearance") {
                val colorStyleOptions = rememberColorStyleOptions()
                val bottomBarLayoutOptions = rememberBottomBarLayoutOptions()
                SettingsSectionCard(
                    title = stringResource(R.string.settings_appearance),
                    subtitle = stringResource(R.string.settings_appearance_subtitle),
                    modifier =
                        Modifier
                            .padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding)
                            .entrance(order = 1, play = !entrancePlayed),
                ) {
                    InlineSelectRow(
                        rowKey = "color_style",
                        title = stringResource(R.string.settings_color_style),
                        summary = stringResource(R.string.settings_color_style_subtitle),
                        icon = Icons.Default.Palette,
                        options = colorStyleOptions,
                        currentValue = colorStyleValue,
                        expandedKey = expandedRowKey,
                        onExpandChange = { expandedRowKey = it },
                        onValueChange = viewModel::setThemeColorStyle,
                    )
                    SettingsItemDivider()
                    InlineSelectRow(
                        rowKey = "bottom_bar_layout",
                        title = stringResource(R.string.settings_bottom_bar_layout_title),
                        summary = stringResource(R.string.settings_bottom_bar_layout_subtitle),
                        icon = Icons.Default.ViewAgenda,
                        options = bottomBarLayoutOptions,
                        currentValue = bottomBarLayout.value,
                        expandedKey = expandedRowKey,
                        onExpandChange = { expandedRowKey = it },
                        onValueChange = { viewModel.setBottomBarLayout(BottomBarLayout.fromString(it)) },
                    )
                    SettingsItemDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_album_new_style_title),
                        summary = stringResource(R.string.settings_album_new_style_subtitle),
                        icon = Icons.Default.Album,
                        checked = albumNewStyle,
                        onCheckedChange = viewModel::setAlbumNewStyle,
                        expanded = albumStyleExpanded,
                        onExpandClick = { albumStyleExpanded = !albumStyleExpanded },
                    )
                    AnimatedVisibility(visible = albumNewStyle && albumStyleExpanded) {
                        AlbumStyleControls(
                            tuning = albumStyleTuning,
                            onChange = viewModel::setAlbumStyleTuning,
                            onReset = viewModel::resetAlbumStyleTuning,
                        )
                    }
                    SettingsItemDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_dark_mode_title),
                        summary = stringResource(R.string.settings_dark_mode_toggle_subtitle),
                        icon = Icons.Default.DarkMode,
                        checked = darkMode,
                        onCheckedChange = viewModel::setDarkMode,
                    )
                    SettingsItemDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_liquid_glass_title),
                        summary = stringResource(R.string.settings_liquid_glass_subtitle),
                        icon = Icons.Default.LensBlur,
                        checked = liquidGlassEnabled,
                        onCheckedChange = viewModel::setLiquidGlassEnabled,
                    )
                    AnimatedVisibility(visible = liquidGlassEnabled) {
                        LiquidGlassTuningControls(
                            tuning = liquidGlassTuning,
                            onChange = viewModel::setLiquidGlassTuning,
                            onReset = viewModel::resetLiquidGlassTuning,
                        )
                    }
                    SettingsItemDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_liquid_glass_v2_title),
                        summary = stringResource(R.string.settings_liquid_glass_v2_subtitle),
                        icon = Icons.Default.BlurOn,
                        checked = liquidGlassV2Enabled,
                        onCheckedChange = viewModel::setLiquidGlassV2Enabled,
                    )
                    AnimatedVisibility(visible = liquidGlassV2Enabled) {
                        LiquidGlassV2Controls(
                            tuning = liquidGlassV2Tuning,
                            onChange = viewModel::setLiquidGlassV2Tuning,
                            onReset = viewModel::resetLiquidGlassV2Tuning,
                        )
                    }
                }
            }

            item(key = "settings_bottom_bar") {
                SettingsSectionCard(
                    title = stringResource(R.string.bb_title),
                    subtitle = stringResource(R.string.bb_subtitle),
                    modifier = Modifier.padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding),
                ) {
                    BottomBarSettingsContent(
                        custom = bottomBarCustomization,
                        onInt = viewModel::setBottomBarInt,
                        onText = viewModel::setBottomBarText,
                        onReset = viewModel::resetBottomBarCustomization,
                    )
                }
            }

            item(key = "settings_music_top_bar") {
                SettingsSectionCard(
                    title = stringResource(R.string.mtb_title),
                    subtitle = stringResource(R.string.mtb_subtitle),
                    modifier = Modifier.padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding),
                ) {
                    MusicTopBarSettingsContent(
                        custom = musicTopBarCustomization,
                        onInt = viewModel::setMusicTopBarInt,
                        onReset = viewModel::resetMusicTopBar,
                    )
                }
            }

            item(key = "settings_now_playing") {
                val backgroundOptions = rememberBackgroundOptions()
                val coverTapOptions = rememberCoverTapOptions()
                val progressWaveformOptions = rememberProgressWaveformOptions()
                val playerStyleOptions = rememberPlayerStyleOptions()
                SettingsSectionCard(
                    title = stringResource(R.string.settings_section_player_visual),
                    subtitle = stringResource(R.string.settings_section_player_visual_subtitle),
                    modifier =
                        Modifier
                            .padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding)
                            .entrance(order = 2, play = !entrancePlayed),
                ) {
                    InlineSelectRow(
                        rowKey = "player_background",
                        title = stringResource(R.string.settings_player_background),
                        summary = stringResource(R.string.settings_player_background_subtitle),
                        icon = Icons.Default.Landscape,
                        options = backgroundOptions,
                        currentValue = backgroundValue,
                        expandedKey = expandedRowKey,
                        onExpandChange = { expandedRowKey = it },
                        onValueChange = viewModel::setDynamicSpectrumBackground,
                    )
                    AnimatedVisibility(
                        visible = backgroundValue == DynamicSpectrumBackground.Smooth.value,
                        enter =
                            expandVertically(
                                animationSpec = tween(durationMillis = 220, easing = EaseOutEmphasized),
                            ) + fadeIn(tween(durationMillis = 180)),
                        exit =
                            shrinkVertically(
                                animationSpec = tween(durationMillis = 180, easing = EaseOutEmphasized),
                            ) + fadeOut(tween(durationMillis = 140)),
                    ) {
                        SmoothControls(
                            settings = smooth,
                            onChange = viewModel::updateSmooth,
                            onReset = viewModel::resetSmooth,
                        )
                    }
//                        SettingsItemDivider()
//                        InlineSelectRow(
//                            rowKey = "cover_tap",
//                            title = stringResource(R.string.settings_cover_tap_effect),
//                            summary = stringResource(R.string.settings_cover_tap_effect_subtitle),
//                            icon = Icons.Default.Album,
//                            options = coverTapOptions,
//                            currentValue = coverTapValue,
//                            expandedKey = expandedRowKey,
//                            onExpandChange = { expandedRowKey = it },
//                            onValueChange = viewModel::setDynamicCoverType,
//                        )
                    SettingsItemDivider()
                    InlineSelectRow(
                        rowKey = "progress_waveform",
                        title = stringResource(R.string.settings_progress_waveform),
                        summary = stringResource(R.string.settings_progress_waveform_subtitle),
                        icon = Icons.Default.GraphicEq,
                        options = progressWaveformOptions,
                        currentValue = progressWaveformValue,
                        expandedKey = expandedRowKey,
                        onExpandChange = { expandedRowKey = it },
                        onValueChange = viewModel::setProgressBarStyle,
                    )
                    SettingsItemDivider()
                    InlineSelectRow(
                        rowKey = "player_style",
                        title = stringResource(R.string.settings_player_style),
                        summary = stringResource(R.string.settings_player_style_subtitle),
                        icon = Icons.Default.Album,
                        options = playerStyleOptions,
                        currentValue = playerStyleValue,
                        expandedKey = expandedRowKey,
                        onExpandChange = { expandedRowKey = it },
                        onValueChange = viewModel::setPlayerStyle,
                    )
                    AnimatedVisibility(visible = playerStyleValue == PillPlayerTuning.STYLE_PILL) {
                        PillPlayerControls(
                            tuning = pillTuning,
                            onChange = viewModel::setPillPlayerTuning,
                            onReset = viewModel::resetPillPlayerTuning,
                        )
                    }
                }
            }

            item(key = "settings_playback") {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_playback),
                    subtitle = stringResource(R.string.settings_section_playback_subtitle),
                    modifier =
                        Modifier
                            .padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding)
                            .entrance(order = 3, play = !entrancePlayed),
                ) {
                    NavigationRow(
                        title = stringResource(R.string.settings_sound_effects),
                        summary = stringResource(R.string.settings_sound_effects_subtitle),
                        icon = Icons.Default.Tune,
                        onClick = { backStack.add(AudioEffectsRoute) },
                    )
                    SettingsItemDivider()
                    NavigationRow(
                        title = stringResource(R.string.settings_sleep_timer),
                        summary =
                            sleepTimer?.let {
                                stringResource(
                                    R.string.settings_sleep_timer_active,
                                    formatSleepTimerRemaining(it.remainingMs),
                                )
                            } ?: stringResource(R.string.settings_sleep_timer_subtitle),
                        icon = Icons.Default.Bedtime,
                        onClick = { backStack.add(SleepTimerRoute) },
                    )
                    SettingsItemDivider()
                    SwitchRow(
                        title = stringResource(R.string.settings_keep_screen_on),
                        summary = stringResource(R.string.settings_keep_screen_on_subtitle),
                        icon = Icons.Default.Visibility,
                        checked = keepScreenOn,
                        onCheckedChange = viewModel::setKeepScreenOn,
                    )
                }
            }

            item(key = "settings_media_library") {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_media_library),
                    subtitle = null,
                    modifier =
                        Modifier
                            .padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding)
                            .entrance(order = 4, play = !entrancePlayed),
                ) {
                    NavigationRow(
                        title = stringResource(R.string.setting_ignore_music),
                        summary = stringResource(R.string.settings_ignored_songs_subtitle),
                        icon = Icons.Default.MusicOff,
                        onClick = { backStack.add(IgnoredSongsRoute) },
                    )
                }
            }

            // 临时屏蔽导入导入功能
//            item(key = "settings_library_transfer") {
//                LibraryTransferSettings(
//                    modifier =
//                        Modifier
//                            .padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding)
//                            .entrance(order = 5, play = !entrancePlayed),
//                )
//            }

            item(key = "settings_about") {
                SettingsSectionCard(
                    title = stringResource(R.string.settings_about),
                    subtitle = null,
                    modifier =
                        Modifier
                            .padding(horizontal = LayoutTokens.MusicHeaderHorizontalPadding)
                            .entrance(order = 6, play = !entrancePlayed),
                ) {
                    NavigationRow(
                        title = stringResource(R.string.settings_about),
                        summary = stringResource(R.string.settings_about_subtitle),
                        icon = Icons.Default.Info,
                        onClick = { backStack.add(AboutRoute) },
                    )
                }
            }
        }

        SettingsTopBar(
            title = stringResource(R.string.finder_settings_title),
            listState = listState,
            solid = mastheadGone,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.align(Alignment.TopStart),
        )
    }
}

// -- 页面结构 --

/** 大标题刊头 */
@Composable
private fun SettingsMasthead(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.finder_settings_title),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.settings_masthead_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 固定顶栏：背景透明度跟随刊头收缩进度，收起后出现分隔线。 */
@Composable
private fun SettingsTopBar(
    title: String,
    listState: LazyListState,
    solid: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val backgroundColor = MaterialTheme.colorScheme.background
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(statusBarTop + 56.dp)
                .drawBehind {
                    drawRect(color = backgroundColor.copy(alpha = mastheadCollapse(listState)))
                },
    ) {
        if (solid) {
            HorizontalDivider(
                modifier = Modifier.align(Alignment.BottomStart),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.14f),
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(top = statusBarTop)
                    .padding(horizontal = Spacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    Modifier
                        .weight(1f)
                        .graphicsLayer { alpha = mastheadCollapse(listState) },
            )
        }
    }
}

/** 圆角容器分组卡 */
@Composable
internal fun SettingsSectionCard(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(Shapes.ExtraLargeCornerBasedShape)
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.86f))
                .padding(vertical = Spacing.Large),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.Small))
        content()
    }
}

// -- 行类型 --

/**
 * 内联选择行
 */
@Composable
private fun InlineSelectRow(
    rowKey: String,
    title: String,
    summary: String,
    icon: ImageVector,
    options: ImmutableList<SettingsOption>,
    currentValue: String,
    expandedKey: String?,
    onExpandChange: (String?) -> Unit,
    onValueChange: (String) -> Unit,
) {
    val expanded = expandedKey == rowKey
    val currentLabel =
        remember(options, currentValue) {
            options.firstOrNull { it.value == currentValue }?.label
        }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 220, easing = EaseOutEmphasized),
        label = "settings_row_chevron",
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsRowFrame(
            icon = icon,
            highlighted = expanded,
            onClick = { onExpandChange(if (expanded) null else rowKey) },
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    // 当前选中值直接摆在标题下：一眼看得到，不必展开
                    text = currentLabel ?: summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (currentLabel != null) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (currentLabel != null) FontWeight.Medium else FontWeight.Normal,
                )
            }
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription =
                    if (expanded) {
                        stringResource(R.string.settings_collapse_options)
                    } else {
                        stringResource(R.string.settings_expand_options)
                    },
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.graphicsLayer { rotationZ = chevronRotation },
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter =
                expandVertically(
                    animationSpec = tween(durationMillis = 220, easing = EaseOutEmphasized),
                ) + fadeIn(tween(durationMillis = 180)),
            exit =
                shrinkVertically(
                    animationSpec = tween(durationMillis = 180, easing = EaseOutEmphasized),
                ) + fadeOut(tween(durationMillis = 140)),
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(
                            start = SettingsRowContentInset,
                            end = Spacing.Large,
                            top = Spacing.ExtraSmall,
                            bottom = Spacing.Small,
                        ),
                verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
            ) {
                options.forEach { option ->
                    OptionCard(
                        option = option,
                        selected = option.value == currentValue,
                        onClick = { onValueChange(option.value) },
                    )
                }
            }
        }
    }
}

/** 开关行：整行可点，图标在开启时轻微放大，动作快速收敛不喧宾。 */
@Composable
private fun SwitchRow(
    title: String,
    summary: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    expanded: Boolean = false,
    onExpandClick: (() -> Unit)? = null,
) {
    val iconScale by animateFloatAsState(
        targetValue = if (checked) 1.08f else 1f,
        animationSpec = tween(durationMillis = 180, easing = EaseOutEmphasized),
        label = "settings_switch_icon_scale",
    )
    SettingsRowFrame(
        icon = icon,
        highlighted = checked,
        onClick = { onCheckedChange(!checked) },
        iconModifier = Modifier.graphicsLayer(scaleX = iconScale, scaleY = iconScale),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (checked && onExpandClick != null) {
            val expandRotation by animateFloatAsState(
                targetValue = if (expanded) 180f else 0f,
                animationSpec = tween(durationMillis = 220, easing = EaseOutEmphasized),
                label = "settings_switch_expand",
            )
            IconButton(
                onClick = onExpandClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription =
                        if (expanded) {
                            stringResource(R.string.settings_collapse_options)
                        } else {
                            stringResource(R.string.settings_expand_options)
                        },
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.graphicsLayer { rotationZ = expandRotation },
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedTrackColor = MaterialTheme.colorScheme.primary,
                ),
        )
    }
}

/** 跳转行：整行可点，右侧显示 chevron。 */
@Composable
internal fun NavigationRow(
    title: String,
    summary: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true,
) {
    SettingsRowFrame(
        icon = icon,
        highlighted = false,
        onClick = onClick,
        enabled = enabled,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** 所有设置行共享的骨架：46dp 圆角图标筐 + 内容槽。整行 clickHighlight。 */
@Composable
private fun SettingsRowFrame(
    icon: ImageVector,
    highlighted: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconModifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val iconBackground by animateColorAsState(
        targetValue =
            if (highlighted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
        animationSpec = tween(durationMillis = 220, easing = EaseOutEmphasized),
        label = "settings_row_icon_bg",
    )
    val iconTint by animateColorAsState(
        targetValue =
            if (highlighted) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(durationMillis = 220, easing = EaseOutEmphasized),
        label = "settings_row_icon_tint",
    )
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickHighlight(enabled = enabled, onClick = onClick)
                .graphicsLayer { alpha = if (enabled) 1f else 0.45f }
                .padding(horizontal = Spacing.Large, vertical = Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        Box(
            modifier =
                Modifier
                    .size(SettingsRowIconSize)
                    .clip(Shapes.LargeCornerBasedShape)
                    .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = iconModifier,
            )
        }
        content()
    }
}

@Composable
internal fun SettingsItemDivider() {
    Box(
        modifier =
            Modifier
                .padding(start = SettingsRowContentInset, end = Spacing.Large)
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    )
}

// -- 选项卡（展开态里的单个选项）--

@Composable
private fun OptionCard(
    option: SettingsOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val background by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.55f)
            },
        animationSpec = tween(durationMillis = 200, easing = EaseOutEmphasized),
        label = "option_card_background",
    )
    val titleColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = tween(durationMillis = 200, easing = EaseOutEmphasized),
        label = "option_card_title",
    )
    val descriptionColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(durationMillis = 200, easing = EaseOutEmphasized),
        label = "option_card_desc",
    )

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(Shapes.MediumCornerBasedShape)
                .background(background)
                .clickHighlight(onClick = onClick)
                .padding(horizontal = Spacing.Medium, vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
    ) {
        Icon(
            imageVector = option.icon,
            contentDescription = null,
            tint = titleColor,
            modifier = Modifier.size(20.dp),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = option.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = titleColor,
            )
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = descriptionColor,
            )
        }
        // 固定尾部槽位，避免选中动画改变文案宽度和 Row 间距
        OptionSelectedIndicator(selected = selected)
    }
}

@Composable
private fun OptionSelectedIndicator(selected: Boolean) {
    Box(
        modifier = Modifier.size(22.dp),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = selected,
            enter =
                fadeIn(tween(durationMillis = 180)) +
                    scaleIn(
                        animationSpec = tween(durationMillis = 220, easing = EaseOutEmphasized),
                        initialScale = ScaleEnterFrom,
                    ),
            exit = fadeOut(tween(durationMillis = 120)),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

// -- 选项配置：每个选项配一个直观的图标 + 一句话说明 --

@Immutable
private data class SettingsOption(
    val value: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
)

@Composable
private fun rememberBottomBarLayoutOptions(): ImmutableList<SettingsOption> {
    val traditionalLabel = stringResource(R.string.bottom_bar_layout_traditional)
    val traditionalDescription = stringResource(R.string.bottom_bar_layout_traditional_desc)
    val floatingLabel = stringResource(R.string.bottom_bar_layout_floating)
    val floatingDescription = stringResource(R.string.bottom_bar_layout_floating_desc)
    return remember(traditionalLabel, traditionalDescription, floatingLabel, floatingDescription) {
        ImmutableList.copyOf(
            listOf(
                SettingsOption(
                    BottomBarLayout.Traditional.value,
                    traditionalLabel,
                    traditionalDescription,
                    Icons.Default.ViewAgenda,
                ),
                SettingsOption(
                    BottomBarLayout.Floating.value,
                    floatingLabel,
                    floatingDescription,
                    Icons.Default.Layers,
                ),
            ),
        )
    }
}

@Composable
private fun rememberColorStyleOptions(): ImmutableList<SettingsOption> {
    val texturedLabel = stringResource(R.string.theme_color_style_textured)
    val texturedDesc = stringResource(R.string.color_style_textured_desc)
    val flatLabel = stringResource(R.string.theme_color_style_flat)
    val flatDesc = stringResource(R.string.color_style_flat_desc)
    return remember(texturedLabel, texturedDesc, flatLabel, flatDesc) {
        ImmutableList.copyOf(
            listOf(
                SettingsOption(
                    ThemeColorStyle.Textured.value,
                    texturedLabel,
                    texturedDesc,
                    Icons.Default.AutoAwesome,
                ),
                SettingsOption(
                    ThemeColorStyle.Flat.value,
                    flatLabel,
                    flatDesc,
                    Icons.Default.Layers,
                ),
            ),
        )
    }
}

@Composable
private fun rememberBackgroundOptions(): ImmutableList<SettingsOption> {
    val off = stringResource(R.string.settings_option_off)
    val offDesc = stringResource(R.string.bg_off_desc)
    val topGlow = stringResource(R.string.bg_top_glow)
    val topGlowDesc = stringResource(R.string.bg_top_glow_desc)
    val aurora = stringResource(R.string.bg_liquid_aurora)
    val auroraDesc = stringResource(R.string.bg_liquid_aurora_desc)
    val shader = stringResource(R.string.bg_effect_shader)
    val shaderDesc = stringResource(R.string.bg_effect_shader_desc)
    val warp = stringResource(R.string.bg_fluid_warp)
    val warpDesc = stringResource(R.string.bg_fluid_warp_desc)
    val blur = stringResource(R.string.bg_blur_cover)
    val blurDesc = stringResource(R.string.bg_blur_cover_desc)
    val smooth = stringResource(R.string.bg_smooth)
    val smoothDesc = stringResource(R.string.bg_smooth_desc)
    return remember(
        smooth,
        smoothDesc,
        off,
        offDesc,
        topGlow,
        topGlowDesc,
        aurora,
        auroraDesc,
        shader,
        shaderDesc,
        warp,
        warpDesc,
        blur,
        blurDesc,
    ) {
        ImmutableList.copyOf(
            listOf(
                SettingsOption(
                    DynamicSpectrumBackground.Smooth.value,
                    smooth,
                    smoothDesc,
                    Icons.Default.AutoAwesome,
                ),
                SettingsOption(
                    DynamicSpectrumBackground.FluidWarp.value,
                    warp,
                    warpDesc,
                    Icons.Default.LensBlur,
                ),
                SettingsOption(
                    DynamicSpectrumBackground.TopGlow.value,
                    topGlow,
                    topGlowDesc,
                    Icons.Default.WbSunny,
                ),
                SettingsOption(
                    DynamicSpectrumBackground.LiquidAurora.value,
                    aurora,
                    auroraDesc,
                    Icons.Default.Waves,
                ),
                SettingsOption(
                    DynamicSpectrumBackground.EffectShader.value,
                    shader,
                    shaderDesc,
                    Icons.Default.BlurOn,
                ),
                SettingsOption(
                    DynamicSpectrumBackground.BlurCover.value,
                    blur,
                    blurDesc,
                    Icons.Default.BlurOn,
                ),
                SettingsOption(
                    DynamicSpectrumBackground.OFF.value,
                    off,
                    offDesc,
                    Icons.Default.PowerSettingsNew,
                ),
            ),
        )
    }
}

@Composable
private fun rememberCoverTapOptions(): ImmutableList<SettingsOption> {
    val off = stringResource(R.string.settings_option_off)
    val offDesc = stringResource(R.string.cover_off_desc)
    val stars = stringResource(R.string.cover_shining_stars)
    val starsDesc = stringResource(R.string.cover_shining_stars_desc)
    val city = stringResource(R.string.cover_audio_city)
    val cityDesc = stringResource(R.string.cover_audio_city_desc)
    return remember(off, offDesc, stars, starsDesc, city, cityDesc) {
        ImmutableList.copyOf(
            listOf(
                SettingsOption(
                    DynamicCoverType.ShiningStars.value,
                    stars,
                    starsDesc,
                    Icons.Default.AutoAwesome,
                ),
                SettingsOption(
                    DynamicCoverType.AudioCity.value,
                    city,
                    cityDesc,
                    Icons.Default.LocationCity,
                ),
                SettingsOption(
                    DynamicCoverType.OFF.value,
                    off,
                    offDesc,
                    Icons.Default.PowerSettingsNew,
                ),
            ),
        )
    }
}

@Composable
private fun rememberProgressWaveformOptions(): ImmutableList<SettingsOption> {
    val dynamic = stringResource(R.string.progress_bar_style_dynamic_waveform)
    val dynamicDesc = stringResource(R.string.waveform_dynamic_desc)
    val timeDomain = stringResource(R.string.progress_bar_style_time_domain_waveform)
    val timeDomainDesc = stringResource(R.string.waveform_time_domain_desc)
    val glow = stringResource(R.string.progress_bar_style_glow_particles)
    val glowDesc = stringResource(R.string.waveform_glow_particles_desc)
    return remember(dynamic, dynamicDesc, timeDomain, timeDomainDesc, glow, glowDesc) {
        ImmutableList.copyOf(
            listOf(
                SettingsOption(
                    ProgressBarStyle.DynamicWaveform.value,
                    dynamic,
                    dynamicDesc,
                    Icons.Default.GraphicEq,
                ),
                SettingsOption(
                    ProgressBarStyle.TimeDomainWaveform.value,
                    timeDomain,
                    timeDomainDesc,
                    Icons.AutoMirrored.Filled.ShowChart,
                ),
                SettingsOption(
                    ProgressBarStyle.GlowParticles.value,
                    glow,
                    glowDesc,
                    Icons.Default.AutoAwesome,
                ),
            ),
        )
    }
}

@Composable
private fun rememberPlayerStyleOptions(): ImmutableList<SettingsOption> {
    val classic = stringResource(R.string.player_style_classic)
    val classicDesc = stringResource(R.string.player_style_classic_desc)
    val pill = stringResource(R.string.player_style_pill)
    val pillDesc = stringResource(R.string.player_style_pill_desc)
    val bubble = stringResource(R.string.player_style_bubble)
    val bubbleDesc = stringResource(R.string.player_style_bubble_desc)
    return remember(classic, classicDesc, pill, pillDesc, bubble, bubbleDesc) {
        ImmutableList.copyOf(
            listOf(
                SettingsOption(PillPlayerTuning.STYLE_CLASSIC, classic, classicDesc, Icons.Default.Album),
                SettingsOption(PillPlayerTuning.STYLE_PILL, pill, pillDesc, Icons.Default.ViewAgenda),
                SettingsOption(PillPlayerTuning.STYLE_BUBBLE, bubble, bubbleDesc, Icons.Default.BlurOn),
            ),
        )
    }
}

/** Pill 播放器的布局调节：专辑、圆环、播放键、上下曲和混合/喜欢按钮位置。 */
@Composable
private fun PillPlayerControls(
    tuning: PillPlayerTuning,
    onChange: (Int, Int) -> Unit,
    onReset: () -> Unit,
) {
    @Composable
    fun PillSlider(
        label: Int,
        index: Int,
        value: Int,
        range: IntRange,
        step: Int = 1,
    ) = LyricsSettingsSlider(
        label = stringResource(label),
        description = "",
        value = value,
        range = range,
        step = step,
        valueText = { "$it dp" },
        onValueChange = { onChange(index, it) },
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = SettingsRowContentInset,
                    end = Spacing.Large,
                    top = Spacing.ExtraSmall,
                    bottom = Spacing.Small,
                ),
    ) {
        PillSlider(R.string.pill_album_width, 0, tuning.albumWidthDp, 120..300, 2)
        PillSlider(R.string.pill_album_height, 1, tuning.albumHeightDp, 200..420, 2)
        PillSlider(R.string.pill_bottom_radius, 2, tuning.bottomRadiusDp, 0..150, 2)
        PillSlider(R.string.pill_ring_padding, 3, tuning.ringPaddingDp, -30..30)
        PillSlider(R.string.pill_actions_height, 4, tuning.actionsHeightDp, -20..60)
        PillSlider(R.string.pill_play_size, 5, tuning.playSizeDp, 40..90)
        PillSlider(R.string.pill_play_height, 6, tuning.playHeightDp, -20..80)
        PillSlider(R.string.pill_skip_size, 7, tuning.skipSizeDp, 20..50)
        PillSlider(R.string.pill_skip_side, 8, tuning.skipSideDp, 0..60)
        PillSlider(R.string.pill_skip_height, 9, tuning.skipHeightDp, -60..60)
        LyricsSettingsResetButton(
            text = stringResource(R.string.pill_reset),
            onClick = onReset,
        )
    }
}

// -- 布局常量 & 刊头收缩计算 --

/** 图标筐尺寸；行内其他元素的横向对齐（分隔线、展开选项区域）以它为基准。 */
private val SettingsRowIconSize = 46.dp

/**
 * 让分隔线和展开出来的选项都对齐到标题的起始位置。
 */
private val SettingsRowContentInset = Spacing.Large + SettingsRowIconSize + Spacing.Medium

private val MastheadCollapseDistance = 140.dp

/** 刊头收缩进度：0f = 完全展开，1f = 完全滚进顶栏。 */
private fun Density.mastheadCollapse(listState: LazyListState): Float =
    if (listState.firstVisibleItemIndex > 0) {
        1f
    } else {
        (listState.firstVisibleItemScrollOffset / MastheadCollapseDistance.toPx()).coerceIn(0f, 1f)
    }


/** 液态玻璃调节项：与演示页的滑块一一对应，仅在液态玻璃开启时显示。 */
@Composable
private fun LiquidGlassTuningControls(
    tuning: LiquidGlassTuning,
    onChange: (LiquidGlassSetting, Int) -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = SettingsRowContentInset,
                    end = Spacing.Large,
                    top = Spacing.ExtraSmall,
                    bottom = Spacing.Small,
                ),
    ) {
        LyricsSettingsChips(
            label = stringResource(R.string.glass_mode),
            description = stringResource(R.string.glass_mode_desc),
            options = listOf(0, 1),
            selected = tuning.mode,
            optionLabel = { stringResource(if (it == 1) R.string.glass_mode_lens else R.string.glass_mode_frosted) },
            onSelect = { onChange(LiquidGlassSetting.Mode, it) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.glass_blur),
            description = stringResource(R.string.glass_blur_desc),
            value = tuning.blurDp,
            range = 1..40,
            step = 1,
            valueText = { "$it dp" },
            onValueChange = { onChange(LiquidGlassSetting.Blur, it) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.glass_quality),
            description = stringResource(R.string.glass_quality_desc),
            value = tuning.quality,
            range = 5..100,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { onChange(LiquidGlassSetting.Quality, it) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.glass_tint),
            description = stringResource(R.string.glass_tint_desc),
            value = tuning.tintPercent,
            range = 0..80,
            step = 2,
            valueText = { "$it%" },
            onValueChange = { onChange(LiquidGlassSetting.Tint, it) },
        )
        if (!tuning.isLens) {
            LyricsSettingsSlider(
                label = stringResource(R.string.glass_saturation),
                description = stringResource(R.string.glass_saturation_desc),
                value = tuning.saturationPercent,
                range = 50..300,
                step = 10,
                valueText = { "$it%" },
                onValueChange = { onChange(LiquidGlassSetting.Saturation, it) },
            )
        }
        LyricsSettingsSlider(
            label = stringResource(R.string.glass_corner),
            description = stringResource(R.string.glass_corner_desc),
            value = tuning.cornerDp,
            range = 0..36,
            step = 1,
            valueText = { "$it dp" },
            onValueChange = { onChange(LiquidGlassSetting.Corner, it) },
        )
        if (tuning.isLens) {
            LyricsSettingsSlider(
                label = stringResource(R.string.glass_refraction),
                description = stringResource(R.string.glass_refraction_desc),
                value = tuning.refractionPercent,
                range = 0..100,
                step = 5,
                valueText = { "$it%" },
                onValueChange = { onChange(LiquidGlassSetting.Refraction, it) },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.glass_refraction_height),
                description = stringResource(R.string.glass_refraction_height_desc),
                value = tuning.refractionHeightDp,
                range = 2..40,
                step = 1,
                valueText = { "$it dp" },
                onValueChange = { onChange(LiquidGlassSetting.RefractionHeight, it) },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.glass_falloff),
                description = stringResource(R.string.glass_falloff_desc),
                value = tuning.falloffPercent,
                range = 5..100,
                step = 5,
                valueText = { "$it%" },
                onValueChange = { onChange(LiquidGlassSetting.Falloff, it) },
            )
        }
        LyricsSettingsSlider(
            label = stringResource(R.string.glass_rim),
            description = stringResource(R.string.glass_rim_desc),
            value = tuning.rimPercent,
            range = 0..100,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { onChange(LiquidGlassSetting.Rim, it) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.glass_rim_width),
            description = stringResource(R.string.glass_rim_width_desc),
            value = tuning.rimWidthDp,
            range = 0..6,
            step = 1,
            valueText = { "$it dp" },
            onValueChange = { onChange(LiquidGlassSetting.RimWidth, it) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.glass_shadow),
            description = stringResource(R.string.glass_shadow_desc),
            value = tuning.shadowPercent,
            range = 0..50,
            step = 2,
            valueText = { "$it%" },
            onValueChange = { onChange(LiquidGlassSetting.Shadow, it) },
        )
        LyricsSettingsResetButton(
            text = stringResource(R.string.glass_reset),
            onClick = onReset,
        )
    }
}

/** 液态玻璃 V2（Kyant Backdrop）的全部调节项；上方固定的预览行实时显示效果。 */
@Composable
private fun LiquidGlassV2Controls(
    tuning: LiquidGlassV2Tuning,
    onChange: (LiquidGlassV2Setting, Int) -> Unit,
    onReset: () -> Unit,
) {
    @Composable
    fun GlassSlider(
        label: Int,
        setting: LiquidGlassV2Setting,
        value: Int,
        range: IntRange,
        step: Int = 1,
        text: (Int) -> String,
    ) = LyricsSettingsSlider(
        label = stringResource(label),
        description = "",
        value = value,
        range = range,
        step = step,
        valueText = text,
        onValueChange = { onChange(setting, it) },
    )

    @Composable
    fun GlassToggle(
        label: Int,
        setting: LiquidGlassV2Setting,
        value: Int,
    ) = LyricsSettingsToggle(
        label = stringResource(label),
        description = "",
        checked = value == 1,
        onCheckedChange = { onChange(setting, if (it) 1 else 0) },
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = SettingsRowContentInset,
                    end = Spacing.Large,
                    top = Spacing.ExtraSmall,
                    bottom = Spacing.Small,
                ),
    ) {
        GlassSlider(R.string.glass_v2_corner, LiquidGlassV2Setting.Corner, tuning.cornerDp, 0..60) { "$it dp" }
        GlassSlider(R.string.glass_v2_refraction_height, LiquidGlassV2Setting.RefractionHeight, tuning.refractionHeightDp, 0..48) { "$it dp" }
        GlassSlider(R.string.glass_v2_refraction_amount, LiquidGlassV2Setting.RefractionAmount, tuning.refractionAmountDp, 0..80) { "$it dp" }
        GlassToggle(R.string.glass_v2_depth, LiquidGlassV2Setting.Depth, tuning.depth)
        GlassToggle(R.string.glass_v2_chromatic, LiquidGlassV2Setting.Chromatic, tuning.chromatic)
        GlassSlider(R.string.glass_v2_blur, LiquidGlassV2Setting.Blur, tuning.blurDp, 0..32) { "$it dp" }
        GlassToggle(R.string.glass_v2_vibrancy, LiquidGlassV2Setting.Vibrancy, tuning.vibrancy)
        GlassSlider(R.string.glass_v2_tint_opacity, LiquidGlassV2Setting.TintOpacity, tuning.tintPercent, 0..100, 2) { "$it%" }
        LyricsSettingsChips(
            label = stringResource(R.string.glass_v2_tint_color),
            description = "",
            options = listOf(0, 1, 2, 3),
            selected = tuning.tintColor,
            optionLabel = {
                stringResource(
                    when (it) {
                        1 -> R.string.glass_v2_tint_white
                        2 -> R.string.glass_v2_tint_black
                        3 -> R.string.glass_v2_tint_orange
                        else -> R.string.glass_v2_tint_surface
                    },
                )
            },
            onSelect = { onChange(LiquidGlassV2Setting.TintColor, it) },
        )
        LyricsSettingsChips(
            label = stringResource(R.string.glass_v2_highlight_style),
            description = "",
            options = listOf(0, 1, 2, 3),
            selected = tuning.highlightStyle,
            optionLabel = {
                stringResource(
                    when (it) {
                        1 -> R.string.glass_v2_hl_default
                        2 -> R.string.glass_v2_hl_ambient
                        3 -> R.string.glass_v2_hl_plain
                        else -> R.string.glass_v2_hl_off
                    },
                )
            },
            onSelect = { onChange(LiquidGlassV2Setting.HighlightStyle, it) },
        )
        if (tuning.highlightStyle != 0) {
            GlassSlider(R.string.glass_v2_highlight_width, LiquidGlassV2Setting.HighlightWidth, tuning.highlightWidthTenths, 0..40) { "${it.tenthsText()} dp" }
            GlassSlider(R.string.glass_v2_highlight_alpha, LiquidGlassV2Setting.HighlightAlpha, tuning.highlightAlphaPercent, 0..100, 5) { "$it%" }
        }
        if (tuning.highlightStyle == 1) {
            GlassSlider(R.string.glass_v2_highlight_angle, LiquidGlassV2Setting.HighlightAngle, tuning.highlightAngle, 0..360, 5) { "$it°" }
            GlassSlider(R.string.glass_v2_highlight_falloff, LiquidGlassV2Setting.HighlightFalloff, tuning.highlightFalloffTenths, 0..40) { it.tenthsText() }
        }
        GlassSlider(R.string.glass_v2_shadow_radius, LiquidGlassV2Setting.ShadowRadius, tuning.shadowRadiusDp, 0..48) { "$it dp" }
        GlassSlider(R.string.glass_v2_shadow_alpha, LiquidGlassV2Setting.ShadowAlpha, tuning.shadowAlphaPercent, 0..100, 2) { "$it%" }
        GlassSlider(R.string.glass_v2_inner_radius, LiquidGlassV2Setting.InnerRadius, tuning.innerShadowRadiusDp, 0..48) { "$it dp" }
        GlassSlider(R.string.glass_v2_inner_alpha, LiquidGlassV2Setting.InnerAlpha, tuning.innerShadowAlphaPercent, 0..100, 5) { "$it%" }
        GlassToggle(R.string.glass_v2_press, LiquidGlassV2Setting.Press, tuning.press)
        LyricsSettingsResetButton(
            text = stringResource(R.string.glass_v2_reset),
            onClick = onReset,
        )
    }
}

/** 新专辑样式的调节项：阴影、文字、圆角、边距、大小与高光（高光选项与液态玻璃 V2 一致）。 */
@Composable
private fun AlbumStyleControls(
    tuning: AlbumStyleTuning,
    onChange: (AlbumStyleSetting, Int) -> Unit,
    onReset: () -> Unit,
) {
    @Composable
    fun StyleSlider(
        label: Int,
        setting: AlbumStyleSetting,
        value: Int,
        range: IntRange,
        step: Int = 1,
        text: (Int) -> String,
    ) = LyricsSettingsSlider(
        label = stringResource(label),
        description = "",
        value = value,
        range = range,
        step = step,
        valueText = text,
        onValueChange = { onChange(setting, it) },
    )

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = SettingsRowContentInset,
                    end = Spacing.Large,
                    top = Spacing.ExtraSmall,
                    bottom = Spacing.Small,
                ),
    ) {
        StyleSlider(R.string.album_style_shadow_opacity, AlbumStyleSetting.ShadowOpacity, tuning.shadowPercent, 0..100, 5) { "$it%" }
        StyleSlider(R.string.album_style_text_size, AlbumStyleSetting.TextSize, tuning.textSizeSp, 10..20) { "$it sp" }
        StyleSlider(R.string.glass_v2_corner, AlbumStyleSetting.Corner, tuning.cornerDp, 0..60, 2) { "$it dp" }
        StyleSlider(R.string.album_style_text_padding, AlbumStyleSetting.TextPadding, tuning.textPaddingDp, 0..32) { "$it dp" }
        StyleSlider(R.string.album_style_size, AlbumStyleSetting.Size, tuning.sizePercent, 50..100, 5) { "$it%" }
        LyricsSettingsChips(
            label = stringResource(R.string.glass_v2_highlight_style),
            description = "",
            options = listOf(0, 1, 2, 3),
            selected = tuning.highlightStyle,
            optionLabel = {
                stringResource(
                    when (it) {
                        1 -> R.string.glass_v2_hl_default
                        2 -> R.string.glass_v2_hl_ambient
                        3 -> R.string.glass_v2_hl_plain
                        else -> R.string.glass_v2_hl_off
                    },
                )
            },
            onSelect = { onChange(AlbumStyleSetting.HighlightStyle, it) },
        )
        if (tuning.highlightStyle != 0) {
            StyleSlider(R.string.glass_v2_highlight_width, AlbumStyleSetting.HighlightWidth, tuning.highlightWidthTenths, 0..40) { "${it.tenthsText()} dp" }
            StyleSlider(R.string.glass_v2_highlight_alpha, AlbumStyleSetting.HighlightAlpha, tuning.highlightAlphaPercent, 0..100, 5) { "$it%" }
        }
        if (tuning.highlightStyle == 1) {
            StyleSlider(R.string.glass_v2_highlight_angle, AlbumStyleSetting.HighlightAngle, tuning.highlightAngle, 0..360, 5) { "$it°" }
            StyleSlider(R.string.glass_v2_highlight_falloff, AlbumStyleSetting.HighlightFalloff, tuning.highlightFalloffTenths, 0..40) { it.tenthsText() }
        }
        LyricsSettingsResetButton(
            text = stringResource(R.string.album_style_reset),
            onClick = onReset,
        )
    }
}

/** Smooth (ColorfulX style) background: every setting of the gradient, plus colour UI and freeze. */
@Composable
private fun SmoothControls(
    settings: SmoothSettings,
    onChange: (SmoothSettings) -> Unit,
    onReset: () -> Unit,
) {
    fun tenths(v: Int) = "${v / 10}.${v % 10}"
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = SettingsRowContentInset,
                    end = Spacing.Large,
                    top = Spacing.ExtraSmall,
                    bottom = Spacing.Small,
                ),
    ) {
        LyricsSettingsToggle(
            label = stringResource(R.string.smooth_color_ui),
            description = stringResource(R.string.smooth_color_ui_desc),
            checked = settings.colorUi,
            onCheckedChange = { onChange(settings.copy(colorUi = it)) },
        )
        LyricsSettingsToggle(
            label = stringResource(R.string.smooth_freeze),
            description = stringResource(R.string.smooth_freeze_desc),
            checked = settings.freeze,
            onCheckedChange = { onChange(settings.copy(freeze = it)) },
        )
        if (settings.colorUi) {
            LyricsSettingsChips(
                label = stringResource(R.string.smooth_theme_mode),
                description = stringResource(R.string.smooth_theme_mode_desc),
                options = listOf(SmoothThemeMode.AUTO, SmoothThemeMode.VIVID, SmoothThemeMode.LIGHT, SmoothThemeMode.DARK),
                selected = settings.themeMode,
                optionLabel = {
                    when (it) {
                        SmoothThemeMode.VIVID -> stringResource(R.string.smooth_theme_mode_vivid)
                        SmoothThemeMode.LIGHT -> stringResource(R.string.smooth_theme_mode_light)
                        SmoothThemeMode.DARK -> stringResource(R.string.smooth_theme_mode_dark)
                        else -> stringResource(R.string.smooth_theme_mode_auto)
                    }
                },
                onSelect = { onChange(settings.copy(themeMode = it)) },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.smooth_text_tint),
                description = stringResource(R.string.smooth_text_tint_desc),
                value = settings.textTintPercent,
                range = 0..100,
                step = 5,
                valueText = { "$it%" },
                onValueChange = { onChange(settings.copy(textTintPercent = it)) },
            )
            if (settings.themeMode == SmoothThemeMode.VIVID) {
                LyricsSettingsSlider(
                    label = stringResource(R.string.smooth_text_boost),
                    description = stringResource(R.string.smooth_text_boost_desc),
                    value = settings.textBoostPercent,
                    range = 0..200,
                    step = 5,
                    valueText = { "$it%" },
                    onValueChange = { onChange(settings.copy(textBoostPercent = it)) },
                )
            }
        }
        LyricsSettingsSlider(
            label = stringResource(R.string.smooth_saturation),
            description = stringResource(R.string.smooth_saturation_desc),
            value = settings.saturationPercent,
            range = 0..200,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { onChange(settings.copy(saturationPercent = it)) },
        )
        // Speed, colour change and frame limit only matter while the background moves.
        if (!settings.freeze) {
            LyricsSettingsSlider(
                label = stringResource(R.string.smooth_speed),
                description = stringResource(R.string.smooth_speed_desc),
                value = settings.speedTenths,
                range = 0..30,
                step = 1,
                valueText = { tenths(it) + "x" },
                onValueChange = { onChange(settings.copy(speedTenths = it)) },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.smooth_transition),
                description = stringResource(R.string.smooth_transition_desc),
                value = settings.transitionTenths,
                range = 5..80,
                step = 5,
                valueText = { tenths(it) },
                onValueChange = { onChange(settings.copy(transitionTenths = it)) },
            )
            LyricsSettingsSlider(
                label = stringResource(R.string.smooth_frame_limit),
                description = stringResource(R.string.smooth_frame_limit_desc),
                value = settings.frameLimit,
                range = 15..120,
                step = 15,
                valueText = { "$it fps" },
                onValueChange = { onChange(settings.copy(frameLimit = it)) },
            )
        }
        LyricsSettingsSlider(
            label = stringResource(R.string.smooth_bias),
            description = stringResource(R.string.smooth_bias_desc),
            value = settings.biasMilli,
            range = 2..200,
            step = 2,
            valueText = { "0." + it.toString().padStart(3, '0') },
            onValueChange = { onChange(settings.copy(biasMilli = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.smooth_power),
            description = stringResource(R.string.smooth_power_desc),
            value = settings.powerTenths,
            range = 10..80,
            step = 5,
            valueText = { tenths(it) },
            onValueChange = { onChange(settings.copy(powerTenths = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.smooth_noise),
            description = stringResource(R.string.smooth_noise_desc),
            value = settings.noise,
            range = 0..30,
            step = 1,
            valueText = { "$it" },
            onValueChange = { onChange(settings.copy(noise = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.smooth_render_scale),
            description = stringResource(R.string.smooth_render_scale_desc),
            value = settings.renderScalePercent,
            range = 25..100,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { onChange(settings.copy(renderScalePercent = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.smooth_dim),
            description = stringResource(R.string.smooth_dim_desc),
            value = settings.dimPercent,
            range = 0..70,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { onChange(settings.copy(dimPercent = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.smooth_lyrics_dim),
            description = stringResource(R.string.smooth_lyrics_dim_desc),
            value = settings.lyricsDimPercent,
            range = 0..70,
            step = 5,
            valueText = { "$it%" },
            onValueChange = { onChange(settings.copy(lyricsDimPercent = it)) },
        )
        LyricsSettingsResetButton(
            text = stringResource(R.string.smooth_reset),
            onClick = onReset,
        )
    }
}

