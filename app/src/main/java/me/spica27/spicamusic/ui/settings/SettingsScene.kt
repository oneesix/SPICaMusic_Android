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
import me.spica27.spicamusic.ui.navigation.AboutRoute
import me.spica27.spicamusic.ui.navigation.AudioEffectsRoute
import me.spica27.spicamusic.ui.navigation.IgnoredSongsRoute
import me.spica27.spicamusic.ui.navigation.LocalBackStack
import me.spica27.spicamusic.ui.navigation.SleepTimerRoute
import me.spica27.spicamusic.ui.player.LocalPlayerViewModel
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
import me.spica27.spicamusic.ui.widget.VerseMeshSettings
import me.spica27.spicamusic.ui.widget.VerseThemeMode
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
    val keepScreenOn by viewModel.keepScreenOn.collectAsStateWithLifecycle()
    val backgroundValue by viewModel.dynamicSpectrumBackground.collectAsStateWithLifecycle()
    val verseMode by viewModel.verseThemeMode.collectAsStateWithLifecycle()
    val verseSaturation by viewModel.verseSaturation.collectAsStateWithLifecycle()
    val verseVibrancy by viewModel.verseVibrancy.collectAsStateWithLifecycle()
    val verseHue by viewModel.verseHue.collectAsStateWithLifecycle()
    val verseMesh by viewModel.verseMesh.collectAsStateWithLifecycle()
    val coverTapValue by viewModel.dynamicCoverType.collectAsStateWithLifecycle()
    val progressWaveformValue by viewModel.progressBarStyle.collectAsStateWithLifecycle()
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

            item(key = "settings_now_playing") {
                val backgroundOptions = rememberBackgroundOptions()
                val coverTapOptions = rememberCoverTapOptions()
                val progressWaveformOptions = rememberProgressWaveformOptions()
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
                        visible = backgroundValue == DynamicSpectrumBackground.VerseFlow.value,
                        enter =
                            expandVertically(
                                animationSpec = tween(durationMillis = 220, easing = EaseOutEmphasized),
                            ) + fadeIn(tween(durationMillis = 180)),
                        exit =
                            shrinkVertically(
                                animationSpec = tween(durationMillis = 180, easing = EaseOutEmphasized),
                            ) + fadeOut(tween(durationMillis = 140)),
                    ) {
                        VerseAppearanceControls(
                            mode = verseMode,
                            saturation = verseSaturation,
                            vibrancy = verseVibrancy,
                            hue = verseHue,
                            onModeChange = viewModel::setVerseThemeMode,
                            onSaturationChange = viewModel::setVerseSaturation,
                            onVibrancyChange = viewModel::setVerseVibrancy,
                            onHueChange = viewModel::setVerseHue,
                            onReset = viewModel::resetVerseAppearance,
                        )
                        VerseBlobControls(
                            mesh = verseMesh,
                            onChange = viewModel::updateVerseMesh,
                            onReset = viewModel::resetVerseMesh,
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
    val verse = stringResource(R.string.bg_verse_flow)
    val verseDesc = stringResource(R.string.bg_verse_flow_desc)
    return remember(
        verse,
        verseDesc,
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
                    DynamicSpectrumBackground.VerseFlow.value,
                    verse,
                    verseDesc,
                    Icons.Default.Palette,
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

/** Verse Flow 外观：Auto / Light / Dark，饱和度、鲜艳度、高亮色（与 HTML 的 Appearance 一致）。 */
@Composable
private fun VerseAppearanceControls(
    mode: String,
    saturation: Int,
    vibrancy: Int,
    hue: Int,
    onModeChange: (String) -> Unit,
    onSaturationChange: (Int) -> Unit,
    onVibrancyChange: (Int) -> Unit,
    onHueChange: (Int) -> Unit,
    onReset: () -> Unit,
) {
    val auto = stringResource(R.string.verse_hue_auto)
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
            label = stringResource(R.string.verse_mode),
            description = stringResource(R.string.verse_mode_desc),
            options = listOf(VerseThemeMode.AUTO, VerseThemeMode.LIGHT, VerseThemeMode.DARK),
            selected = mode,
            optionLabel = {
                when (it) {
                    VerseThemeMode.LIGHT -> stringResource(R.string.verse_mode_light)
                    VerseThemeMode.DARK -> stringResource(R.string.verse_mode_dark)
                    else -> stringResource(R.string.verse_mode_auto)
                }
            },
            onSelect = onModeChange,
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.verse_saturation),
            description = stringResource(R.string.verse_saturation_desc),
            value = saturation,
            range = 0..200,
            step = 5,
            valueText = { "$it%" },
            onValueChange = onSaturationChange,
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.verse_vibrancy),
            description = stringResource(R.string.verse_vibrancy_desc),
            value = vibrancy,
            range = 0..100,
            step = 1,
            valueText = { "$it%" },
            onValueChange = onVibrancyChange,
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.verse_highlight),
            description = stringResource(R.string.verse_highlight_desc),
            value = hue,
            range = -180..180,
            step = 5,
            valueText = { if (it == 0) auto else (if (it > 0) "+" else "") + it + "°" },
            onValueChange = onHueChange,
        )
        LyricsSettingsResetButton(
            text = stringResource(R.string.verse_reset),
            onClick = onReset,
        )
    }
}

/** Verse Flow 歌词页色块：数量、大小、不透明度、颜色、动效速度，以及动画开关（关闭后完全静态）。 */
@Composable
private fun VerseBlobControls(
    mesh: VerseMeshSettings,
    onChange: (VerseMeshSettings) -> Unit,
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
        LyricsSettingsSection(title = stringResource(R.string.verse_blobs_section))
        LyricsSettingsToggle(
            label = stringResource(R.string.verse_blobs_anim),
            description = stringResource(R.string.verse_blobs_anim_desc),
            checked = mesh.animated,
            onCheckedChange = { onChange(mesh.copy(animated = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.verse_blobs_count),
            description = stringResource(R.string.verse_blobs_count_desc),
            value = mesh.blobCount,
            range = 1..4,
            step = 1,
            valueText = { "$it" },
            onValueChange = { onChange(mesh.copy(blobCount = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.verse_blobs_size),
            description = stringResource(R.string.verse_blobs_size_desc),
            value = mesh.sizePercent,
            range = 0..100,
            step = 1,
            valueText = { "$it%" },
            onValueChange = { onChange(mesh.copy(sizePercent = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.verse_blobs_opacity),
            description = stringResource(R.string.verse_blobs_opacity_desc),
            value = mesh.opacityPercent,
            range = 0..100,
            step = 1,
            valueText = { "$it%" },
            onValueChange = { onChange(mesh.copy(opacityPercent = it)) },
        )
        LyricsSettingsSlider(
            label = stringResource(R.string.verse_blobs_color),
            description = stringResource(R.string.verse_blobs_color_desc),
            value = mesh.colorPercent,
            range = 0..100,
            step = 1,
            valueText = { "$it%" },
            onValueChange = { onChange(mesh.copy(colorPercent = it)) },
        )
        // 动画关闭时速度没有意义，直接隐藏
        if (mesh.animated) {
            LyricsSettingsSlider(
                label = stringResource(R.string.verse_blobs_motion),
                description = stringResource(R.string.verse_blobs_motion_desc),
                value = mesh.motionSeconds,
                range = 5..60,
                step = 1,
                valueText = { "${it}s" },
                onValueChange = { onChange(mesh.copy(motionSeconds = it)) },
            )
        }
        LyricsSettingsResetButton(
            text = stringResource(R.string.verse_blobs_reset),
            onClick = onReset,
        )
    }
}
