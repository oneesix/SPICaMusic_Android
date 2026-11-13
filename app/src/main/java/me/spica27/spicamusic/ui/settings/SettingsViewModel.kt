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
import me.spica27.spicamusic.ui.widget.VerseMeshSettings
import me.spica27.spicamusic.ui.widget.VerseThemeMode

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

    // ---- Verse Flow 外观 ----
    val verseThemeMode =
        settingsUseCases
            .getString(SettingsUseCases.Keys.VERSE_THEME_MODE, VerseThemeMode.AUTO)
            .stateIn(viewModelScope, SharingStarted.Eagerly, VerseThemeMode.AUTO)

    fun setVerseThemeMode(value: String) {
        viewModelScope.launch { settingsUseCases.setString(SettingsUseCases.Keys.VERSE_THEME_MODE, value) }
    }

    /** 饱和度 0..200（%），默认 100 */
    val verseSaturation =
        settingsUseCases
            .getInt(SettingsUseCases.Keys.VERSE_THEME_SATURATION, 100)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 100)

    fun setVerseSaturation(value: Int) {
        viewModelScope.launch { settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_THEME_SATURATION, value.coerceIn(0, 200)) }
    }

    /** 鲜艳度 0..100（%），默认 50 */
    val verseVibrancy =
        settingsUseCases
            .getInt(SettingsUseCases.Keys.VERSE_THEME_VIBRANCY, 50)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 50)

    fun setVerseVibrancy(value: Int) {
        viewModelScope.launch { settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_THEME_VIBRANCY, value.coerceIn(0, 100)) }
    }

    /** 高亮色相偏移 -180..180°，0 = Auto */
    val verseHue =
        settingsUseCases
            .getInt(SettingsUseCases.Keys.VERSE_THEME_HUE, 0)
            .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun setVerseHue(value: Int) {
        viewModelScope.launch { settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_THEME_HUE, value.coerceIn(-180, 180)) }
    }

    // ---- Verse Flow 歌词页色块 ----
    private val meshCount = settingsUseCases.getInt(SettingsUseCases.Keys.VERSE_MESH_COUNT, VerseMeshSettings().blobCount)
    private val meshSize = settingsUseCases.getInt(SettingsUseCases.Keys.VERSE_MESH_SIZE, VerseMeshSettings().sizePercent)
    private val meshOpacity = settingsUseCases.getInt(SettingsUseCases.Keys.VERSE_MESH_OPACITY, VerseMeshSettings().opacityPercent)
    private val meshMotion = settingsUseCases.getInt(SettingsUseCases.Keys.VERSE_MESH_MOTION, VerseMeshSettings().motionSeconds)
    private val meshColor = settingsUseCases.getInt(SettingsUseCases.Keys.VERSE_MESH_COLOR, VerseMeshSettings().colorPercent)
    private val meshAnimated = settingsUseCases.getBoolean(SettingsUseCases.Keys.VERSE_MESH_ANIMATED, VerseMeshSettings().animated)

    val verseMesh =
        combine(
            combine(meshCount, meshSize, meshOpacity) { c, s, o -> Triple(c, s, o) },
            combine(meshMotion, meshColor, meshAnimated) { m, col, a -> Triple(m, col, a) },
        ) { (c, s, o), (m, col, a) ->
            VerseMeshSettings(
                blobCount = c.coerceIn(1, 4),
                sizePercent = s.coerceIn(0, 100),
                opacityPercent = o.coerceIn(0, 100),
                motionSeconds = m.coerceIn(5, 60),
                colorPercent = col.coerceIn(0, 100),
                animated = a,
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, VerseMeshSettings())

    /** Only the fields that actually changed are written. */
    fun updateVerseMesh(new: VerseMeshSettings) {
        val old = verseMesh.value
        viewModelScope.launch {
            if (new.blobCount != old.blobCount) settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_MESH_COUNT, new.blobCount.coerceIn(1, 4))
            if (new.sizePercent != old.sizePercent) settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_MESH_SIZE, new.sizePercent.coerceIn(0, 100))
            if (new.opacityPercent != old.opacityPercent) settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_MESH_OPACITY, new.opacityPercent.coerceIn(0, 100))
            if (new.motionSeconds != old.motionSeconds) settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_MESH_MOTION, new.motionSeconds.coerceIn(5, 60))
            if (new.colorPercent != old.colorPercent) settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_MESH_COLOR, new.colorPercent.coerceIn(0, 100))
            if (new.animated != old.animated) settingsUseCases.setBoolean(SettingsUseCases.Keys.VERSE_MESH_ANIMATED, new.animated)
        }
    }

    fun resetVerseMesh() = updateVerseMesh(VerseMeshSettings())

    fun resetVerseAppearance() {
        viewModelScope.launch {
            settingsUseCases.setString(SettingsUseCases.Keys.VERSE_THEME_MODE, VerseThemeMode.AUTO)
            settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_THEME_SATURATION, 100)
            settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_THEME_VIBRANCY, 50)
            settingsUseCases.setInt(SettingsUseCases.Keys.VERSE_THEME_HUE, 0)
        }
    }

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
}
