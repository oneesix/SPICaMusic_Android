package me.spica27.spicamusic.ui.settings

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.spica27.spicamusic.common.entity.BottomBarLayout
import me.spica27.spicamusic.common.entity.DynamicCoverType
import me.spica27.spicamusic.common.entity.DynamicSpectrumBackground
import me.spica27.spicamusic.common.entity.ProgressBarStyle
import me.spica27.spicamusic.common.entity.ThemeColorStyle
import me.spica27.spicamusic.feature.settings.domain.SettingsUseCases
import me.spica27.spicamusic.ui.glass.LiquidGlassSetting
import me.spica27.spicamusic.ui.glass.LiquidGlassTuning
import me.spica27.spicamusic.ui.glass.LiquidGlassV2Setting
import me.spica27.spicamusic.ui.glass.LiquidGlassV2Tuning
import me.spica27.spicamusic.ui.home.page.AlbumStyleSetting
import me.spica27.spicamusic.ui.home.page.AlbumStyleTuning
import me.spica27.spicamusic.ui.home.page.MusicTopBarCustomization
import me.spica27.spicamusic.ui.home.page.MusicTopBarSetting
import me.spica27.spicamusic.ui.home.page.defaultValue
import me.spica27.spicamusic.ui.home.page.preferenceKey
import me.spica27.spicamusic.ui.home.player_bar.BottomBarCustomization
import me.spica27.spicamusic.ui.home.player_bar.BottomBarSetting
import me.spica27.spicamusic.ui.player.PillPlayerTuning
import me.spica27.spicamusic.ui.widget.SmoothSettings

/**
 * 设置页面 ViewModel
 */
@Stable
class SettingsViewModel(
    private val settingsUseCases: SettingsUseCases,
) : ViewModel() {
    // 暗色模式
    val darkMode =
        settingsUseCases
            .getBoolean(SettingsUseCases.Keys.DARK_MODE, false)
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.setBoolean(SettingsUseCases.Keys.DARK_MODE, enabled)
        }
    }

    // 液态玻璃：默认关闭
    val liquidGlassEnabled =
        settingsUseCases
            .getBoolean(SettingsUseCases.Keys.LIQUID_GLASS_ENABLED, false)
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setLiquidGlassEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.setBoolean(SettingsUseCases.Keys.LIQUID_GLASS_ENABLED, enabled)
        }
    }

    // 液态玻璃调节项：仅在液态玻璃开启时显示在设置页。
    val liquidGlassTuning =
        settingsUseCases
            .getLiquidGlassTuning(LiquidGlassTuning.DefaultValues)
            .map(LiquidGlassTuning::fromList)
            .stateIn(viewModelScope, SharingStarted.Eagerly, LiquidGlassTuning())

    fun setLiquidGlassTuning(
        setting: LiquidGlassSetting,
        value: Int,
    ) {
        viewModelScope.launch {
            settingsUseCases.setLiquidGlassTuning(setting.ordinal, value)
        }
    }

    fun resetLiquidGlassTuning() {
        viewModelScope.launch {
            settingsUseCases.resetLiquidGlassTuning()
        }
    }

    // 液态玻璃 V2（Kyant Backdrop）：独立开关与调节项，不影响上面的旧版设置。
    val liquidGlassV2Enabled =
        settingsUseCases
            .getBoolean(SettingsUseCases.Keys.LIQUID_GLASS_V2_ENABLED, false)
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setLiquidGlassV2Enabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.setBoolean(SettingsUseCases.Keys.LIQUID_GLASS_V2_ENABLED, enabled)
        }
    }

    val liquidGlassV2Tuning =
        settingsUseCases
            .getLiquidGlassV2Tuning(LiquidGlassV2Tuning.DefaultValues)
            .map(LiquidGlassV2Tuning::fromList)
            .stateIn(viewModelScope, SharingStarted.Eagerly, LiquidGlassV2Tuning())

    fun setLiquidGlassV2Tuning(
        setting: LiquidGlassV2Setting,
        value: Int,
    ) {
        viewModelScope.launch {
            settingsUseCases.setLiquidGlassV2Tuning(setting.ordinal, value)
        }
    }

    fun resetLiquidGlassV2Tuning() {
        viewModelScope.launch {
            settingsUseCases.resetLiquidGlassV2Tuning()
        }
    }

    // 底栏自定义：迷你播放条与导航栏的位置/尺寸、标签图标与文字、搜索标签、长按气泡。
    val bottomBarCustomization =
        settingsUseCases
            .getBottomBarCustomization(BottomBarCustomization.DefaultInts, BottomBarCustomization.DefaultTexts)
            .map { (ints, texts) -> BottomBarCustomization.from(ints, texts) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, BottomBarCustomization())

    fun setBottomBarInt(
        setting: BottomBarSetting,
        value: Int,
    ) {
        viewModelScope.launch {
            settingsUseCases.setBottomBarInt(setting.ordinal, value)
        }
    }

    /** slot：0 发现，1 音乐，2 曲库，3 搜索；isIcon 为 true 时写入图标名，否则写入标签文字。 */
    fun setBottomBarText(
        slot: Int,
        isIcon: Boolean,
        value: String,
    ) {
        viewModelScope.launch {
            settingsUseCases.setBottomBarText(slot * 2 + if (isIcon) 0 else 1, value)
        }
    }

    fun resetBottomBarCustomization() {
        viewModelScope.launch {
            settingsUseCases.resetBottomBarCustomization()
        }
    }

    // 音乐页悬浮顶栏自定义：位置、尺寸、标签内容。
    val musicTopBarCustomization =
        combine(
            MusicTopBarSetting.entries.map { setting ->
                settingsUseCases.getInt(setting.preferenceKey(), setting.defaultValue())
            },
        ) { values -> MusicTopBarCustomization.from(values.toList()) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, MusicTopBarCustomization())

    fun setMusicTopBarInt(
        setting: MusicTopBarSetting,
        value: Int,
    ) {
        viewModelScope.launch {
            settingsUseCases.setInt(setting.preferenceKey(), value)
        }
    }

    fun resetMusicTopBar() {
        viewModelScope.launch {
            MusicTopBarSetting.entries.forEach { settingsUseCases.setInt(it.preferenceKey(), it.defaultValue()) }
        }
    }

    // 底栏布局独立于玻璃材质，默认传统。
    val bottomBarLayout =
        settingsUseCases
            .getString(SettingsUseCases.Keys.BOTTOM_BAR_LAYOUT, BottomBarLayout.Traditional.value)
            .map(BottomBarLayout::fromString)
            .stateIn(viewModelScope, SharingStarted.Eagerly, BottomBarLayout.Traditional)

    fun setBottomBarLayout(layout: BottomBarLayout) {
        viewModelScope.launch {
            settingsUseCases.setString(SettingsUseCases.Keys.BOTTOM_BAR_LAYOUT, layout.value)
        }
    }

    // 主题色风格
    val themeColorStyle =
        settingsUseCases
            .getString(
                SettingsUseCases.Keys.THEME_COLOR_STYLE,
                ThemeColorStyle.Textured.value,
            ).stateIn(viewModelScope, SharingStarted.Eagerly, ThemeColorStyle.Textured.value)

    fun setThemeColorStyle(value: String) {
        viewModelScope.launch {
            settingsUseCases.setString(SettingsUseCases.Keys.THEME_COLOR_STYLE, value)
        }
    }

    // 屏幕常亮
    val keepScreenOn =
        settingsUseCases
            .getBoolean(SettingsUseCases.Keys.KEEP_SCREEN_ON, false)
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.setBoolean(SettingsUseCases.Keys.KEEP_SCREEN_ON, enabled)
        }
    }

    // New album style
    val albumNewStyle =
        settingsUseCases
            .getBoolean(SettingsUseCases.Keys.ALBUM_NEW_STYLE, false)
            .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setAlbumNewStyle(enabled: Boolean) {
        viewModelScope.launch {
            settingsUseCases.setBoolean(SettingsUseCases.Keys.ALBUM_NEW_STYLE, enabled)
        }
    }

    val albumStyleTuning =
        settingsUseCases
            .getAlbumStyleTuning(AlbumStyleTuning.DefaultValues)
            .map(AlbumStyleTuning::fromList)
            .stateIn(viewModelScope, SharingStarted.Eagerly, AlbumStyleTuning())

    fun setAlbumStyleTuning(
        setting: AlbumStyleSetting,
        value: Int,
    ) {
        viewModelScope.launch {
            settingsUseCases.setAlbumStyleTuning(setting.ordinal, value)
        }
    }

    fun resetAlbumStyleTuning() {
        viewModelScope.launch {
            settingsUseCases.resetAlbumStyleTuning()
        }
    }

    // 动态频谱
    val dynamicSpectrumBackground =
        settingsUseCases
            .getString(
                SettingsUseCases.Keys.DYNAMIC_SPECTRUM_BACKGROUND,
                DynamicSpectrumBackground.FluidWarp.value,
            ).stateIn(viewModelScope, SharingStarted.Eagerly, DynamicSpectrumBackground.FluidWarp.value)

    fun setDynamicSpectrumBackground(value: String) {
        viewModelScope.launch {
            settingsUseCases.setString(SettingsUseCases.Keys.DYNAMIC_SPECTRUM_BACKGROUND, value)
        }
    }

    // ---- Smooth background (ColorfulX style) ----
    private val smoothDefaults = SmoothSettings()
    private val smoothColorUi = settingsUseCases.getBoolean(SettingsUseCases.Keys.SMOOTH_COLOR_UI, smoothDefaults.colorUi)
    private val smoothFreeze = settingsUseCases.getBoolean(SettingsUseCases.Keys.SMOOTH_FREEZE, smoothDefaults.freeze)
    private val smoothSpeed = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_SPEED, smoothDefaults.speedTenths)
    private val smoothBias = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_BIAS, smoothDefaults.biasMilli)
    private val smoothNoise = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_NOISE, smoothDefaults.noise)
    private val smoothTransition = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_TRANSITION, smoothDefaults.transitionTenths)
    private val smoothPower = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_POWER, smoothDefaults.powerTenths)
    private val smoothFrameLimit = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_FRAME_LIMIT, smoothDefaults.frameLimit)
    private val smoothRenderScale = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_RENDER_SCALE, smoothDefaults.renderScalePercent)
    private val smoothDim = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_DIM, smoothDefaults.dimPercent)
    private val smoothThemeMode = settingsUseCases.getString(SettingsUseCases.Keys.SMOOTH_THEME_MODE, smoothDefaults.themeMode)
    private val smoothSaturation = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_SATURATION, smoothDefaults.saturationPercent)
    private val smoothTextTint = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_TEXT_TINT, smoothDefaults.textTintPercent)
    private val smoothLyricsDim = settingsUseCases.getInt(SettingsUseCases.Keys.SMOOTH_LYRICS_DIM, smoothDefaults.lyricsDimPercent)

    val smooth =
        combine(
            combine(smoothColorUi, smoothFreeze, smoothSpeed, smoothBias, smoothNoise) { c, f, s, b, n ->
                SmoothSettings(colorUi = c, freeze = f, speedTenths = s, biasMilli = b, noise = n)
            },
            combine(smoothTransition, smoothPower, smoothFrameLimit, smoothRenderScale, smoothDim) { t, p, fl, rs, d ->
                intArrayOf(t, p, fl, rs, d)
            },
            combine(smoothThemeMode, smoothSaturation, smoothTextTint, smoothLyricsDim) { m, s, t, l ->
                m to intArrayOf(s, t, l)
            },
        ) { a, b, c ->
            a.copy(
                speedTenths = a.speedTenths.coerceIn(0, 30),
                biasMilli = a.biasMilli.coerceIn(2, 200),
                noise = a.noise.coerceIn(0, 30),
                transitionTenths = b[0].coerceIn(5, 80),
                powerTenths = b[1].coerceIn(10, 80),
                frameLimit = b[2].coerceIn(15, 120),
                renderScalePercent = b[3].coerceIn(25, 100),
                dimPercent = b[4].coerceIn(0, 70),
                themeMode = c.first,
                saturationPercent = c.second[0].coerceIn(0, 200),
                textTintPercent = c.second[1].coerceIn(0, 100),
                lyricsDimPercent = c.second[2].coerceIn(0, 70),
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, SmoothSettings())

    /** Only the fields that actually changed are written. */
    fun updateSmooth(new: SmoothSettings) {
        val old = smooth.value
        viewModelScope.launch {
            if (new.colorUi != old.colorUi) settingsUseCases.setBoolean(SettingsUseCases.Keys.SMOOTH_COLOR_UI, new.colorUi)
            if (new.freeze != old.freeze) settingsUseCases.setBoolean(SettingsUseCases.Keys.SMOOTH_FREEZE, new.freeze)
            if (new.speedTenths != old.speedTenths) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_SPEED, new.speedTenths.coerceIn(0, 30))
            if (new.biasMilli != old.biasMilli) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_BIAS, new.biasMilli.coerceIn(2, 200))
            if (new.noise != old.noise) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_NOISE, new.noise.coerceIn(0, 30))
            if (new.transitionTenths != old.transitionTenths) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_TRANSITION, new.transitionTenths.coerceIn(5, 80))
            if (new.powerTenths != old.powerTenths) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_POWER, new.powerTenths.coerceIn(10, 80))
            if (new.frameLimit != old.frameLimit) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_FRAME_LIMIT, new.frameLimit.coerceIn(15, 120))
            if (new.renderScalePercent != old.renderScalePercent) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_RENDER_SCALE, new.renderScalePercent.coerceIn(25, 100))
            if (new.dimPercent != old.dimPercent) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_DIM, new.dimPercent.coerceIn(0, 70))
            if (new.themeMode != old.themeMode) settingsUseCases.setString(SettingsUseCases.Keys.SMOOTH_THEME_MODE, new.themeMode)
            if (new.saturationPercent != old.saturationPercent) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_SATURATION, new.saturationPercent.coerceIn(0, 200))
            if (new.textTintPercent != old.textTintPercent) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_TEXT_TINT, new.textTintPercent.coerceIn(0, 100))
            if (new.lyricsDimPercent != old.lyricsDimPercent) settingsUseCases.setInt(SettingsUseCases.Keys.SMOOTH_LYRICS_DIM, new.lyricsDimPercent.coerceIn(0, 70))
        }
    }

    fun resetSmooth() = updateSmooth(SmoothSettings())

    // 动态封面
    val dynamicCoverType =
        settingsUseCases
            .getString(
                SettingsUseCases.Keys.DYNAMIC_COVER_TYPE,
                DynamicCoverType.ShiningStars.value,
            ).stateIn(viewModelScope, SharingStarted.Eagerly, DynamicCoverType.ShiningStars.value)

    fun setDynamicCoverType(value: String) {
        viewModelScope.launch {
            settingsUseCases.setString(SettingsUseCases.Keys.DYNAMIC_COVER_TYPE, value)
        }
    }

    // 进度条样式
    val progressBarStyle =
        settingsUseCases
            .getString(
                SettingsUseCases.Keys.PROGRESS_BAR_STYLE,
                ProgressBarStyle.DynamicWaveform.value,
            ).stateIn(viewModelScope, SharingStarted.Eagerly, ProgressBarStyle.DynamicWaveform.value)

    fun setProgressBarStyle(value: String) {
        viewModelScope.launch {
            settingsUseCases.setString(SettingsUseCases.Keys.PROGRESS_BAR_STYLE, value)
        }
    }

    // 播放器样式（classic / pill）
    val playerStyle =
        settingsUseCases
            .getString(
                SettingsUseCases.Keys.PLAYER_STYLE,
                PillPlayerTuning.STYLE_CLASSIC,
            ).stateIn(viewModelScope, SharingStarted.Eagerly, PillPlayerTuning.STYLE_CLASSIC)

    fun setPlayerStyle(value: String) {
        viewModelScope.launch {
            settingsUseCases.setString(SettingsUseCases.Keys.PLAYER_STYLE, value)
        }
    }

    // Pill 播放器的布局调节
    val pillPlayerTuning =
        settingsUseCases
            .getString(
                SettingsUseCases.Keys.PILL_PLAYER_TUNING,
                PillPlayerTuning.Default.encode(),
            ).map { PillPlayerTuning.decode(it) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, PillPlayerTuning.Default)

    fun setPillPlayerTuning(
        index: Int,
        value: Int,
    ) {
        val values = pillPlayerTuning.value.toList().toMutableList()
        if (index !in values.indices) return
        values[index] = value
        viewModelScope.launch {
            settingsUseCases.setString(
                SettingsUseCases.Keys.PILL_PLAYER_TUNING,
                PillPlayerTuning.fromList(values).encode(),
            )
        }
    }

    fun resetPillPlayerTuning() {
        viewModelScope.launch {
            settingsUseCases.setString(
                SettingsUseCases.Keys.PILL_PLAYER_TUNING,
                PillPlayerTuning.Default.encode(),
            )
        }
    }
}
