package me.spica27.spicamusic.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PreferencesManager(
    private val context: Context,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val LIQUID_GLASS_ENABLED = booleanPreferencesKey("liquid_glass_enabled")
        val BOTTOM_BAR_LAYOUT = stringPreferencesKey("bottom_bar_layout")
        val THEME_COLOR_STYLE = stringPreferencesKey("theme_color_style")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val DYNAMIC_SPECTRUM_BACKGROUND = stringPreferencesKey("dynamic_spectrum_background")
        // ---- Verse Flow 外观（Auto / Light / Dark、饱和度、鲜艳度、高亮色相）----
        val VERSE_THEME_MODE = stringPreferencesKey("verse_theme_mode")
        val VERSE_THEME_SATURATION = intPreferencesKey("verse_theme_saturation")
        val VERSE_THEME_VIBRANCY = intPreferencesKey("verse_theme_vibrancy")
        val VERSE_THEME_HUE = intPreferencesKey("verse_theme_hue")

        // ---- Verse Flow 歌词页色块（blobs）----
        val VERSE_MESH_COUNT = intPreferencesKey("verse_mesh_count")
        val VERSE_MESH_SIZE = intPreferencesKey("verse_mesh_size")
        val VERSE_MESH_OPACITY = intPreferencesKey("verse_mesh_opacity")
        val VERSE_MESH_MOTION = intPreferencesKey("verse_mesh_motion")
        val VERSE_MESH_COLOR = intPreferencesKey("verse_mesh_color")
        val VERSE_MESH_ANIMATED = booleanPreferencesKey("verse_mesh_animated")
        val DYNAMIC_COVER_TYPE = stringPreferencesKey("dynamic_cover_type")
        val PROGRESS_BAR_STYLE = stringPreferencesKey("progress_bar_style")
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val EQ_BANDS = stringPreferencesKey("eq_bands")
        val LOUDNESS_NORMALIZATION_ENABLED = booleanPreferencesKey("loudness_normalization_enabled")

        // 目标响度（LUFS）。本项目 float 一律以字符串存储，见 getFloat/setFloat
        val LOUDNESS_TARGET_LUFS = stringPreferencesKey("loudness_target_lufs")
        val SCAN_MIN_DURATION_SEC = stringPreferencesKey("scan_min_duration_sec")
        val SCAN_MIN_FILE_SIZE_KB = stringPreferencesKey("scan_min_file_size_kb")
        val SCAN_ENABLED_FORMATS = stringPreferencesKey("scan_enabled_formats")
        val SCAN_LAST_COMPLETED_AT = stringPreferencesKey("scan_last_completed_at")

        // 是否把 "A/B"、"A、B"、"A & B" 等单个艺人字符串拆成多位艺人（默认关闭）
        val ARTIST_SPLIT_ENABLED = booleanPreferencesKey("artist_split_enabled")

        // ---- 歌词样式（Lyrics style）----
        val LYRIC_FONT_STYLE = stringPreferencesKey("lyric_font_style")
        val LYRIC_FONT = stringPreferencesKey("lyric_font")
        val LYRIC_WEIGHT = intPreferencesKey("lyric_weight")
        val LYRIC_SIZE = intPreferencesKey("lyric_size")
        val LYRIC_TRANSLITERATION_SIZE = intPreferencesKey("lyric_transliteration_size")
        val LYRIC_TRANSLATION_SIZE = intPreferencesKey("lyric_translation_size")
        val LYRIC_BG_TRANSLATION_SIZE = intPreferencesKey("lyric_bg_translation_size")
        val LYRIC_ATTACH_TO_WORDS = booleanPreferencesKey("lyric_attach_to_words")
        val LYRIC_TRANSLITERATION_POSITION = stringPreferencesKey("lyric_transliteration_position")
        val LYRIC_UNSUNG_OPACITY = intPreferencesKey("lyric_unsung_opacity")
        val LYRIC_BLUR = intPreferencesKey("lyric_blur")
        val LYRIC_MOVE_DURATION_MS = intPreferencesKey("lyric_move_duration_ms")
        val LYRIC_CURRENT_POSITION = intPreferencesKey("lyric_current_position")
        val LYRIC_JIGGLE_ENABLED = booleanPreferencesKey("lyric_jiggle_enabled")
        val LYRIC_JIGGLE_STRENGTH = intPreferencesKey("lyric_jiggle_strength")
        val LYRIC_JIGGLE_DURATION_MS = intPreferencesKey("lyric_jiggle_duration_ms")
        val LYRIC_JIGGLE_DELAY_MS = intPreferencesKey("lyric_jiggle_delay_ms")
        val LYRIC_HEADER_ENHANCED = booleanPreferencesKey("lyric_header_enhanced")
        val LYRIC_HEADER_ALBUM_SIZE = intPreferencesKey("lyric_header_album_size")
        val LYRIC_HEADER_TITLE_SIZE = intPreferencesKey("lyric_header_title_size")
        val LYRIC_HEADER_ARTIST_SIZE = intPreferencesKey("lyric_header_artist_size")
        val LYRIC_HEADER_BUTTON_SIZE = intPreferencesKey("lyric_header_button_size")
        val LYRIC_HEADER_LINE_WEIGHT = intPreferencesKey("lyric_header_line_weight")
        val LYRIC_HEADER_LINE_OPACITY = intPreferencesKey("lyric_header_line_opacity")
        val LYRIC_HEADER_BOTTOM_PADDING = intPreferencesKey("lyric_header_bottom_padding")
        val LYRIC_HEADER_BLUR_ENABLED = booleanPreferencesKey("lyric_header_blur_enabled")
        val LYRIC_HEADER_BLUR_AMOUNT = intPreferencesKey("lyric_header_blur_amount")
        val LYRIC_HEADER_SOFT_EDGE = booleanPreferencesKey("lyric_header_soft_edge")
        val LYRIC_HEADER_EDGE_FADE = intPreferencesKey("lyric_header_edge_fade")
        val LYRIC_HEADER_OUTLINE_ENABLED = booleanPreferencesKey("lyric_header_outline_enabled")
        val LYRIC_HEADER_OUTLINE_WEIGHT = intPreferencesKey("lyric_header_outline_weight")
        val LYRIC_HEADER_OUTLINE_OPACITY = intPreferencesKey("lyric_header_outline_opacity")
        val LYRIC_SHOW_TRANSLATION = booleanPreferencesKey("lyric_show_translation")
        val LYRIC_SHOW_TRANSLITERATION = booleanPreferencesKey("lyric_show_transliteration")
        val LYRIC_DOCK_BLUR_ENABLED = booleanPreferencesKey("lyric_dock_blur_enabled")
        val LYRIC_DOCK_BLUR_AMOUNT = intPreferencesKey("lyric_dock_blur_amount")
        val LYRIC_DOCK_SIZE = intPreferencesKey("lyric_dock_size")
        val LYRIC_DOCK_BOTTOM_PADDING = intPreferencesKey("lyric_dock_bottom_padding")
        val LYRIC_DOCK_PILL_PADDING = intPreferencesKey("lyric_dock_pill_padding")
        val LYRIC_DOCK_GAP_LEFT = intPreferencesKey("lyric_dock_gap_left")
        val LYRIC_DOCK_GAP_RIGHT = intPreferencesKey("lyric_dock_gap_right")
        val LYRIC_DOCK_SPREAD_BLUR = booleanPreferencesKey("lyric_dock_spread_blur")
        val LYRIC_DOCK_SOFT_EDGE = booleanPreferencesKey("lyric_dock_soft_edge")
        val LYRIC_DOCK_EDGE_FADE = intPreferencesKey("lyric_dock_edge_fade")
    }


    /** 歌词样式：任何一项变化都会发出完整、已校正的设置对象。 */
    val lyricsStyle: Flow<LyricsStyleSettings> =
        context.dataStore.data
            .map { p ->
                val d = LyricsStyleSettings()
                LyricsStyleSettings(
                    fontStyle = p[Keys.LYRIC_FONT_STYLE] ?: d.fontStyle,
                    lyricsFont = p[Keys.LYRIC_FONT] ?: d.lyricsFont,
                    lyricsWeight = p[Keys.LYRIC_WEIGHT] ?: d.lyricsWeight,
                    lyricsSize = p[Keys.LYRIC_SIZE] ?: d.lyricsSize,
                    transliterationSize = p[Keys.LYRIC_TRANSLITERATION_SIZE] ?: d.transliterationSize,
                    translationSize = p[Keys.LYRIC_TRANSLATION_SIZE] ?: d.translationSize,
                    backgroundTranslationSize = p[Keys.LYRIC_BG_TRANSLATION_SIZE] ?: d.backgroundTranslationSize,
                    attachToWords = p[Keys.LYRIC_ATTACH_TO_WORDS] ?: d.attachToWords,
                    transliterationPosition =
                        p[Keys.LYRIC_TRANSLITERATION_POSITION]
                            ?.let(TransliterationPosition::fromString) ?: d.transliterationPosition,
                    unsungOpacity = p[Keys.LYRIC_UNSUNG_OPACITY] ?: d.unsungOpacity,
                    blur = p[Keys.LYRIC_BLUR] ?: d.blur,
                    moveDurationMs = p[Keys.LYRIC_MOVE_DURATION_MS] ?: d.moveDurationMs,
                    currentLinePosition = p[Keys.LYRIC_CURRENT_POSITION] ?: d.currentLinePosition,
                    jiggleEnabled = p[Keys.LYRIC_JIGGLE_ENABLED] ?: d.jiggleEnabled,
                    jiggleStrength = p[Keys.LYRIC_JIGGLE_STRENGTH] ?: d.jiggleStrength,
                    jiggleDurationMs = p[Keys.LYRIC_JIGGLE_DURATION_MS] ?: d.jiggleDurationMs,
                    jiggleDelayMs = p[Keys.LYRIC_JIGGLE_DELAY_MS] ?: d.jiggleDelayMs,
                    enhanceHeader = p[Keys.LYRIC_HEADER_ENHANCED] ?: d.enhanceHeader,
                    headerAlbumSize = p[Keys.LYRIC_HEADER_ALBUM_SIZE] ?: d.headerAlbumSize,
                    headerTitleSize = p[Keys.LYRIC_HEADER_TITLE_SIZE] ?: d.headerTitleSize,
                    headerArtistSize = p[Keys.LYRIC_HEADER_ARTIST_SIZE] ?: d.headerArtistSize,
                    headerButtonSize = p[Keys.LYRIC_HEADER_BUTTON_SIZE] ?: d.headerButtonSize,
                    headerLineWeight = p[Keys.LYRIC_HEADER_LINE_WEIGHT] ?: d.headerLineWeight,
                    headerLineOpacity = p[Keys.LYRIC_HEADER_LINE_OPACITY] ?: d.headerLineOpacity,
                    headerBottomPadding = p[Keys.LYRIC_HEADER_BOTTOM_PADDING] ?: d.headerBottomPadding,
                    headerBlurEnabled = p[Keys.LYRIC_HEADER_BLUR_ENABLED] ?: d.headerBlurEnabled,
                    headerBlurAmount = p[Keys.LYRIC_HEADER_BLUR_AMOUNT] ?: d.headerBlurAmount,
                    headerSoftEdge = p[Keys.LYRIC_HEADER_SOFT_EDGE] ?: d.headerSoftEdge,
                    headerEdgeFade = p[Keys.LYRIC_HEADER_EDGE_FADE] ?: d.headerEdgeFade,
                    headerOutlineEnabled = p[Keys.LYRIC_HEADER_OUTLINE_ENABLED] ?: d.headerOutlineEnabled,
                    headerOutlineWeight = p[Keys.LYRIC_HEADER_OUTLINE_WEIGHT] ?: d.headerOutlineWeight,
                    headerOutlineOpacity = p[Keys.LYRIC_HEADER_OUTLINE_OPACITY] ?: d.headerOutlineOpacity,
                    showTranslation = p[Keys.LYRIC_SHOW_TRANSLATION] ?: d.showTranslation,
                    showTransliteration = p[Keys.LYRIC_SHOW_TRANSLITERATION] ?: d.showTransliteration,
                    dockBlurEnabled = p[Keys.LYRIC_DOCK_BLUR_ENABLED] ?: d.dockBlurEnabled,
                    dockBlurAmount = p[Keys.LYRIC_DOCK_BLUR_AMOUNT] ?: d.dockBlurAmount,
                    dockSize = p[Keys.LYRIC_DOCK_SIZE] ?: d.dockSize,
                    dockBottomPadding = p[Keys.LYRIC_DOCK_BOTTOM_PADDING] ?: d.dockBottomPadding,
                    dockPillPadding = p[Keys.LYRIC_DOCK_PILL_PADDING] ?: d.dockPillPadding,
                    dockGapLeft = p[Keys.LYRIC_DOCK_GAP_LEFT] ?: d.dockGapLeft,
                    dockGapRight = p[Keys.LYRIC_DOCK_GAP_RIGHT] ?: d.dockGapRight,
                    dockSpreadBlur = p[Keys.LYRIC_DOCK_SPREAD_BLUR] ?: d.dockSpreadBlur,
                    dockSoftEdge = p[Keys.LYRIC_DOCK_SOFT_EDGE] ?: d.dockSoftEdge,
                    dockEdgeFade = p[Keys.LYRIC_DOCK_EDGE_FADE] ?: d.dockEdgeFade,
                ).sanitized()
            }.distinctUntilChanged()

    /** 读取当前歌词样式，套用 [transform] 后一次性写回所有键。 */
    suspend fun updateLyricsStyle(transform: (LyricsStyleSettings) -> LyricsStyleSettings) {
        context.dataStore.edit { p ->
            val d = LyricsStyleSettings()
            val current =
                LyricsStyleSettings(
                    fontStyle = p[Keys.LYRIC_FONT_STYLE] ?: d.fontStyle,
                    lyricsFont = p[Keys.LYRIC_FONT] ?: d.lyricsFont,
                    lyricsWeight = p[Keys.LYRIC_WEIGHT] ?: d.lyricsWeight,
                    lyricsSize = p[Keys.LYRIC_SIZE] ?: d.lyricsSize,
                    transliterationSize = p[Keys.LYRIC_TRANSLITERATION_SIZE] ?: d.transliterationSize,
                    translationSize = p[Keys.LYRIC_TRANSLATION_SIZE] ?: d.translationSize,
                    backgroundTranslationSize = p[Keys.LYRIC_BG_TRANSLATION_SIZE] ?: d.backgroundTranslationSize,
                    attachToWords = p[Keys.LYRIC_ATTACH_TO_WORDS] ?: d.attachToWords,
                    transliterationPosition =
                        p[Keys.LYRIC_TRANSLITERATION_POSITION]
                            ?.let(TransliterationPosition::fromString) ?: d.transliterationPosition,
                    unsungOpacity = p[Keys.LYRIC_UNSUNG_OPACITY] ?: d.unsungOpacity,
                    blur = p[Keys.LYRIC_BLUR] ?: d.blur,
                    moveDurationMs = p[Keys.LYRIC_MOVE_DURATION_MS] ?: d.moveDurationMs,
                    currentLinePosition = p[Keys.LYRIC_CURRENT_POSITION] ?: d.currentLinePosition,
                    jiggleEnabled = p[Keys.LYRIC_JIGGLE_ENABLED] ?: d.jiggleEnabled,
                    jiggleStrength = p[Keys.LYRIC_JIGGLE_STRENGTH] ?: d.jiggleStrength,
                    jiggleDurationMs = p[Keys.LYRIC_JIGGLE_DURATION_MS] ?: d.jiggleDurationMs,
                    jiggleDelayMs = p[Keys.LYRIC_JIGGLE_DELAY_MS] ?: d.jiggleDelayMs,
                    enhanceHeader = p[Keys.LYRIC_HEADER_ENHANCED] ?: d.enhanceHeader,
                    headerAlbumSize = p[Keys.LYRIC_HEADER_ALBUM_SIZE] ?: d.headerAlbumSize,
                    headerTitleSize = p[Keys.LYRIC_HEADER_TITLE_SIZE] ?: d.headerTitleSize,
                    headerArtistSize = p[Keys.LYRIC_HEADER_ARTIST_SIZE] ?: d.headerArtistSize,
                    headerButtonSize = p[Keys.LYRIC_HEADER_BUTTON_SIZE] ?: d.headerButtonSize,
                    headerLineWeight = p[Keys.LYRIC_HEADER_LINE_WEIGHT] ?: d.headerLineWeight,
                    headerLineOpacity = p[Keys.LYRIC_HEADER_LINE_OPACITY] ?: d.headerLineOpacity,
                    headerBottomPadding = p[Keys.LYRIC_HEADER_BOTTOM_PADDING] ?: d.headerBottomPadding,
                    headerBlurEnabled = p[Keys.LYRIC_HEADER_BLUR_ENABLED] ?: d.headerBlurEnabled,
                    headerBlurAmount = p[Keys.LYRIC_HEADER_BLUR_AMOUNT] ?: d.headerBlurAmount,
                    headerSoftEdge = p[Keys.LYRIC_HEADER_SOFT_EDGE] ?: d.headerSoftEdge,
                    headerEdgeFade = p[Keys.LYRIC_HEADER_EDGE_FADE] ?: d.headerEdgeFade,
                    headerOutlineEnabled = p[Keys.LYRIC_HEADER_OUTLINE_ENABLED] ?: d.headerOutlineEnabled,
                    headerOutlineWeight = p[Keys.LYRIC_HEADER_OUTLINE_WEIGHT] ?: d.headerOutlineWeight,
                    headerOutlineOpacity = p[Keys.LYRIC_HEADER_OUTLINE_OPACITY] ?: d.headerOutlineOpacity,
                    showTranslation = p[Keys.LYRIC_SHOW_TRANSLATION] ?: d.showTranslation,
                    showTransliteration = p[Keys.LYRIC_SHOW_TRANSLITERATION] ?: d.showTransliteration,
                    dockBlurEnabled = p[Keys.LYRIC_DOCK_BLUR_ENABLED] ?: d.dockBlurEnabled,
                    dockBlurAmount = p[Keys.LYRIC_DOCK_BLUR_AMOUNT] ?: d.dockBlurAmount,
                    dockSize = p[Keys.LYRIC_DOCK_SIZE] ?: d.dockSize,
                    dockBottomPadding = p[Keys.LYRIC_DOCK_BOTTOM_PADDING] ?: d.dockBottomPadding,
                    dockPillPadding = p[Keys.LYRIC_DOCK_PILL_PADDING] ?: d.dockPillPadding,
                    dockGapLeft = p[Keys.LYRIC_DOCK_GAP_LEFT] ?: d.dockGapLeft,
                    dockGapRight = p[Keys.LYRIC_DOCK_GAP_RIGHT] ?: d.dockGapRight,
                    dockSpreadBlur = p[Keys.LYRIC_DOCK_SPREAD_BLUR] ?: d.dockSpreadBlur,
                    dockSoftEdge = p[Keys.LYRIC_DOCK_SOFT_EDGE] ?: d.dockSoftEdge,
                    dockEdgeFade = p[Keys.LYRIC_DOCK_EDGE_FADE] ?: d.dockEdgeFade,
                )
            val n = transform(current).sanitized()
            p[Keys.LYRIC_FONT_STYLE] = n.fontStyle
            p[Keys.LYRIC_FONT] = n.lyricsFont
            p[Keys.LYRIC_WEIGHT] = n.lyricsWeight
            p[Keys.LYRIC_SIZE] = n.lyricsSize
            p[Keys.LYRIC_TRANSLITERATION_SIZE] = n.transliterationSize
            p[Keys.LYRIC_TRANSLATION_SIZE] = n.translationSize
            p[Keys.LYRIC_BG_TRANSLATION_SIZE] = n.backgroundTranslationSize
            p[Keys.LYRIC_ATTACH_TO_WORDS] = n.attachToWords
            p[Keys.LYRIC_TRANSLITERATION_POSITION] = n.transliterationPosition.value
            p[Keys.LYRIC_UNSUNG_OPACITY] = n.unsungOpacity
            p[Keys.LYRIC_BLUR] = n.blur
            p[Keys.LYRIC_MOVE_DURATION_MS] = n.moveDurationMs
            p[Keys.LYRIC_CURRENT_POSITION] = n.currentLinePosition
            p[Keys.LYRIC_JIGGLE_ENABLED] = n.jiggleEnabled
            p[Keys.LYRIC_JIGGLE_STRENGTH] = n.jiggleStrength
            p[Keys.LYRIC_JIGGLE_DURATION_MS] = n.jiggleDurationMs
            p[Keys.LYRIC_JIGGLE_DELAY_MS] = n.jiggleDelayMs
            p[Keys.LYRIC_HEADER_ENHANCED] = n.enhanceHeader
            p[Keys.LYRIC_HEADER_ALBUM_SIZE] = n.headerAlbumSize
            p[Keys.LYRIC_HEADER_TITLE_SIZE] = n.headerTitleSize
            p[Keys.LYRIC_HEADER_ARTIST_SIZE] = n.headerArtistSize
            p[Keys.LYRIC_HEADER_BUTTON_SIZE] = n.headerButtonSize
            p[Keys.LYRIC_HEADER_LINE_WEIGHT] = n.headerLineWeight
            p[Keys.LYRIC_HEADER_LINE_OPACITY] = n.headerLineOpacity
            p[Keys.LYRIC_HEADER_BOTTOM_PADDING] = n.headerBottomPadding
            p[Keys.LYRIC_HEADER_BLUR_ENABLED] = n.headerBlurEnabled
            p[Keys.LYRIC_HEADER_BLUR_AMOUNT] = n.headerBlurAmount
            p[Keys.LYRIC_HEADER_SOFT_EDGE] = n.headerSoftEdge
            p[Keys.LYRIC_HEADER_EDGE_FADE] = n.headerEdgeFade
            p[Keys.LYRIC_HEADER_OUTLINE_ENABLED] = n.headerOutlineEnabled
            p[Keys.LYRIC_HEADER_OUTLINE_WEIGHT] = n.headerOutlineWeight
            p[Keys.LYRIC_HEADER_OUTLINE_OPACITY] = n.headerOutlineOpacity
            p[Keys.LYRIC_SHOW_TRANSLATION] = n.showTranslation
            p[Keys.LYRIC_SHOW_TRANSLITERATION] = n.showTransliteration
            p[Keys.LYRIC_DOCK_BLUR_ENABLED] = n.dockBlurEnabled
            p[Keys.LYRIC_DOCK_BLUR_AMOUNT] = n.dockBlurAmount
            p[Keys.LYRIC_DOCK_SIZE] = n.dockSize
            p[Keys.LYRIC_DOCK_BOTTOM_PADDING] = n.dockBottomPadding
            p[Keys.LYRIC_DOCK_PILL_PADDING] = n.dockPillPadding
            p[Keys.LYRIC_DOCK_GAP_LEFT] = n.dockGapLeft
            p[Keys.LYRIC_DOCK_GAP_RIGHT] = n.dockGapRight
            p[Keys.LYRIC_DOCK_SPREAD_BLUR] = n.dockSpreadBlur
            p[Keys.LYRIC_DOCK_SOFT_EDGE] = n.dockSoftEdge
            p[Keys.LYRIC_DOCK_EDGE_FADE] = n.dockEdgeFade
        }
    }

    fun getBoolean(
        key: Preferences.Key<Boolean>,
        defaultValue: Boolean = false,
    ): Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }.distinctUntilChanged()

    suspend fun setBoolean(
        key: Preferences.Key<Boolean>,
        value: Boolean,
    ) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getInt(
        key: Preferences.Key<Int>,
        defaultValue: Int = 0,
    ): Flow<Int> =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }.distinctUntilChanged()

    suspend fun setInt(
        key: Preferences.Key<Int>,
        value: Int,
    ) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getString(
        key: Preferences.Key<String>,
        defaultValue: String = "",
    ): Flow<String> =
        context.dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }

    suspend fun setString(
        key: Preferences.Key<String>,
        value: String,
    ) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun getFloat(
        key: Preferences.Key<String>,
        defaultValue: Float = 0f,
    ): Flow<Float> =
        context.dataStore.data.map { preferences ->
            preferences[key]?.toFloatOrNull() ?: defaultValue
        }.distinctUntilChanged()

    suspend fun setFloat(
        key: Preferences.Key<String>,
        value: Float,
    ) {
        context.dataStore.edit { preferences ->
            preferences[key] = value.toString()
        }
    }

    fun getFloatList(
        key: Preferences.Key<String>,
        defaultValue: List<Float> = emptyList(),
    ): Flow<List<Float>> =
        context.dataStore.data.map { preferences ->
            val serialized = preferences[key]
            if (serialized.isNullOrEmpty()) {
                defaultValue
            } else {
                serialized.split(",").mapNotNull { it.toFloatOrNull() }.ifEmpty { defaultValue }
            }
        }

    suspend fun setFloatList(
        key: Preferences.Key<String>,
        value: List<Float>,
    ) {
        context.dataStore.edit { preferences ->
            preferences[key] = value.joinToString(",")
        }
    }
}
