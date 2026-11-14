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
        val LIQUID_GLASS_BLUR = intPreferencesKey("liquid_glass_blur")
        val LIQUID_GLASS_QUALITY = intPreferencesKey("liquid_glass_quality")
        val LIQUID_GLASS_TINT = intPreferencesKey("liquid_glass_tint")
        val LIQUID_GLASS_CORNER = intPreferencesKey("liquid_glass_corner")
        val LIQUID_GLASS_REFRACTION = intPreferencesKey("liquid_glass_refraction")
        val LIQUID_GLASS_REFRACTION_HEIGHT = intPreferencesKey("liquid_glass_refraction_height")
        val LIQUID_GLASS_FALLOFF = intPreferencesKey("liquid_glass_falloff")
        val LIQUID_GLASS_RIM = intPreferencesKey("liquid_glass_rim")
        val LIQUID_GLASS_RIM_WIDTH = intPreferencesKey("liquid_glass_rim_width")
        val LIQUID_GLASS_SHADOW = intPreferencesKey("liquid_glass_shadow")
        val LIQUID_GLASS_MODE = intPreferencesKey("liquid_glass_mode")
        val LIQUID_GLASS_SATURATION = intPreferencesKey("liquid_glass_saturation")
        val LIQUID_GLASS_V2_ENABLED = booleanPreferencesKey("liquid_glass_v2_enabled")
        val BOTTOM_BAR_LAYOUT = stringPreferencesKey("bottom_bar_layout")
        val MUSIC_TOP_BAR_H = intPreferencesKey("music_top_bar_h")
        val MUSIC_TOP_BAR_TOP = intPreferencesKey("music_top_bar_top")
        val MUSIC_TOP_BAR_W = intPreferencesKey("music_top_bar_w")
        val MUSIC_TOP_BAR_HEIGHT = intPreferencesKey("music_top_bar_height")
        val MUSIC_TOP_BAR_LABEL_MODE = intPreferencesKey("music_top_bar_label_mode")
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
        val LYRIC_LINE_EFFECT = stringPreferencesKey("lyric_line_effect")
        val LYRIC_STRETCH_STRENGTH = intPreferencesKey("lyric_stretch_strength")
        val LYRIC_STRETCH_DELAY_MS = intPreferencesKey("lyric_stretch_delay_ms")
        val LYRIC_STRETCH_DURATION_MS = intPreferencesKey("lyric_stretch_duration_ms")
        val LYRIC_LIGHT_SWEEP = booleanPreferencesKey("lyric_light_sweep")
        val LYRIC_LIGHT_SWEEP_CONTINUOUS = booleanPreferencesKey("lyric_light_sweep_continuous")
        val LYRIC_LIGHT_SWEEP_OKLCH = booleanPreferencesKey("lyric_light_sweep_oklch")
        val LYRIC_LIGHT_SWEEP_EASE = booleanPreferencesKey("lyric_light_sweep_ease")
        val LYRIC_LIGHT_SWEEP_FLASH = booleanPreferencesKey("lyric_light_sweep_flash")
        val LYRIC_LIGHT_SWEEP_SOFTNESS = intPreferencesKey("lyric_light_sweep_softness")
        val LYRIC_GRADIENT_ENABLED = booleanPreferencesKey("lyric_gradient_enabled")
        val LYRIC_LIGHT_ENABLED = booleanPreferencesKey("lyric_light_enabled")
        val LYRIC_LIGHT_INTENSITY = intPreferencesKey("lyric_light_intensity")
        val LYRIC_DYNAMIC_ENABLED = booleanPreferencesKey("lyric_dynamic_enabled")
        val LYRIC_DYNAMIC_BACKGROUND = booleanPreferencesKey("lyric_dynamic_background")
        val LYRIC_DYNAMIC_MODE = stringPreferencesKey("lyric_dynamic_mode")
        val LYRIC_DYNAMIC_OPACITY = intPreferencesKey("lyric_dynamic_opacity")
        val LYRIC_DYNAMIC_SATURATION = intPreferencesKey("lyric_dynamic_saturation")
        val LYRIC_DYNAMIC_CONTRAST = intPreferencesKey("lyric_dynamic_contrast")
        val LYRIC_LINE_MOVE = booleanPreferencesKey("lyric_line_move")
        val LYRIC_LINE_MOVE_MS = intPreferencesKey("lyric_line_move_ms")
        val LYRIC_BG_REVEAL = booleanPreferencesKey("lyric_bg_reveal")
        val LYRIC_BG_REVEAL_BEFORE_MS = intPreferencesKey("lyric_bg_reveal_start_offset_ms")
        val LYRIC_BG_REVEAL_AFTER_MS = intPreferencesKey("lyric_bg_reveal_after_ms")
        val LYRIC_BG_REVEAL_DURATION_MS = intPreferencesKey("lyric_bg_reveal_duration_ms")
        val LYRIC_BG_REVEAL_BLUR = booleanPreferencesKey("lyric_bg_reveal_blur")
        val LYRIC_BG_REVEAL_BLUR_AMOUNT = intPreferencesKey("lyric_bg_reveal_blur_amount")
        val LYRIC_FIX_ANCHOR = booleanPreferencesKey("lyric_fix_anchor")
        val LYRIC_BG_REVEAL_STYLE = intPreferencesKey("lyric_bg_reveal_style")
        val LYRIC_BG_REVEAL_FLIP_ANGLE = intPreferencesKey("lyric_bg_reveal_flip_angle")
        val LYRIC_BG_REVEAL_FLIP_PIVOT = intPreferencesKey("lyric_bg_reveal_flip_pivot")
        val LYRIC_BG_REVEAL_FADE = booleanPreferencesKey("lyric_bg_reveal_fade")
        val LYRIC_BG_REVEAL_FADE_MS = intPreferencesKey("lyric_bg_reveal_fade_ms")
        val LYRIC_WORD_LIFT_ENABLED = booleanPreferencesKey("lyric_word_lift_enabled")
        val LYRIC_WORD_LIFT = intPreferencesKey("lyric_word_lift")
        val LYRIC_MERGE_WORDS = booleanPreferencesKey("lyric_merge_words")
        val LYRIC_HELD_MANUAL = booleanPreferencesKey("lyric_held_manual")
        val LYRIC_HELD_MIN_MS = intPreferencesKey("lyric_held_min_ms")
        val LYRIC_HELD_OPTIMIZE = booleanPreferencesKey("lyric_held_optimize")
        val LYRIC_HELD_DEFAULT_GLOW = booleanPreferencesKey("lyric_held_default_glow")
        val LYRIC_HOLD_SCROLL = booleanPreferencesKey("lyric_hold_scroll")
        val LYRIC_HOLD_SCROLL_LINES = intPreferencesKey("lyric_hold_scroll_lines")
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
        val LYRIC_HEADER_BLUR_OPTIMIZE = booleanPreferencesKey("lyric_header_blur_optimize")
        val LYRIC_HEADER_BLUR_QUALITY = intPreferencesKey("lyric_header_blur_quality")
        val LYRIC_DOCK_BLUR_OPTIMIZE = booleanPreferencesKey("lyric_dock_blur_optimize")
        val LYRIC_DOCK_BLUR_QUALITY = intPreferencesKey("lyric_dock_blur_quality")
        val LYRIC_DOCK_SOFT_EDGE = booleanPreferencesKey("lyric_dock_soft_edge")
        val LYRIC_DOCK_EDGE_FADE = intPreferencesKey("lyric_dock_edge_fade")
        val LYRIC_KINETIC_ENABLED = booleanPreferencesKey("lyric_kinetic_enabled")
        val LYRIC_KINETIC_STYLE = stringPreferencesKey("lyric_kinetic_style")
        val LYRIC_KINETIC_SIZE = intPreferencesKey("lyric_kinetic_size")
        val LYRIC_KINETIC_STYLE_SIZES = stringPreferencesKey("lyric_kinetic_style_sizes")
        val LYRIC_KINETIC_POSITION = intPreferencesKey("lyric_kinetic_position")
        val LYRIC_KINETIC_TL_SIZE = intPreferencesKey("lyric_kinetic_tl_size")
        val LYRIC_KINETIC_TL_POSITION = intPreferencesKey("lyric_kinetic_tl_position")
        val LYRIC_KINETIC_BGV_ENABLED = booleanPreferencesKey("lyric_kinetic_bgv_enabled")
        val LYRIC_KINETIC_BGV_STYLES = stringPreferencesKey("lyric_kinetic_bgv_styles")
        val LYRIC_KINETIC_BGV_SIZE = intPreferencesKey("lyric_kinetic_bgv_size")
        val LYRIC_KINETIC_BGV_POSITION = intPreferencesKey("lyric_kinetic_bgv_position")
        val LYRIC_KINETIC_RIPPLES = booleanPreferencesKey("lyric_kinetic_ripples")
        val LYRIC_KINETIC_SHADOW = booleanPreferencesKey("lyric_kinetic_shadow")
        val LYRIC_KINETIC_GLOW = booleanPreferencesKey("lyric_kinetic_glow")
        val LYRIC_KINETIC_BLUR = booleanPreferencesKey("lyric_kinetic_blur")
        val LYRIC_KINETIC_FLIP3D = booleanPreferencesKey("lyric_kinetic_flip3d")
        val LYRIC_KINETIC_SPEED = intPreferencesKey("lyric_kinetic_speed")
        val LYRIC_KINETIC_LETTER_DELAY = intPreferencesKey("lyric_kinetic_letter_delay")
        val LYRIC_KINETIC_TEXT_COLORS = intPreferencesKey("lyric_kinetic_text_colors")
        val LYRIC_KINETIC_TEXT_STRENGTH = intPreferencesKey("lyric_kinetic_text_strength")
        val LYRIC_KINETIC_BG_STRENGTH = intPreferencesKey("lyric_kinetic_bg_strength")
        val LYRIC_KINETIC_BG_QUALITY = intPreferencesKey("lyric_kinetic_bg_quality")
        val LYRIC_KINETIC_BG_FRAME = intPreferencesKey("lyric_kinetic_bg_frame")
        val LYRIC_KINETIC_TEXT_PALETTE = intPreferencesKey("lyric_kinetic_text_palette")
        val LYRIC_KINETIC_GLOW_WORDS = intPreferencesKey("lyric_kinetic_glow_words")
        val LYRIC_KINETIC_GLOW_INTENSITY = intPreferencesKey("lyric_kinetic_glow_intensity")
        val LYRIC_KINETIC_BG_DEFAULT = booleanPreferencesKey("lyric_kinetic_bg_default")
        val LYRIC_KINETIC_FONT_MIX = booleanPreferencesKey("lyric_kinetic_font_mix")
        val LYRIC_KINETIC_FONT_COUNT = intPreferencesKey("lyric_kinetic_font_count")
        val LYRIC_KINETIC_FONTS = stringPreferencesKey("lyric_kinetic_fonts")
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
                    lineEffect = p[Keys.LYRIC_LINE_EFFECT]?.let(LineEffect::fromString) ?: d.lineEffect,
                    stretchStrength = p[Keys.LYRIC_STRETCH_STRENGTH] ?: d.stretchStrength,
                    stretchDelayMs = p[Keys.LYRIC_STRETCH_DELAY_MS] ?: d.stretchDelayMs,
                    stretchDurationMs = p[Keys.LYRIC_STRETCH_DURATION_MS] ?: d.stretchDurationMs,
                    lightSweepEnabled = p[Keys.LYRIC_LIGHT_SWEEP] ?: d.lightSweepEnabled,
                    lightSweepContinuous = p[Keys.LYRIC_LIGHT_SWEEP_CONTINUOUS] ?: d.lightSweepContinuous,
                    lightSweepOklch = p[Keys.LYRIC_LIGHT_SWEEP_OKLCH] ?: d.lightSweepOklch,
                    lightSweepEase = p[Keys.LYRIC_LIGHT_SWEEP_EASE] ?: d.lightSweepEase,
                    lightSweepFlash = p[Keys.LYRIC_LIGHT_SWEEP_FLASH] ?: d.lightSweepFlash,
                    lightSweepSoftness = p[Keys.LYRIC_LIGHT_SWEEP_SOFTNESS] ?: d.lightSweepSoftness,
                    gradientLyricsEnabled = p[Keys.LYRIC_GRADIENT_ENABLED] ?: d.gradientLyricsEnabled,
                    lyricsLightEnabled = p[Keys.LYRIC_LIGHT_ENABLED] ?: d.lyricsLightEnabled,
                    lyricsLightIntensity = p[Keys.LYRIC_LIGHT_INTENSITY] ?: d.lyricsLightIntensity,
                    dynamicLyricsEnabled = p[Keys.LYRIC_DYNAMIC_ENABLED] ?: d.dynamicLyricsEnabled,
                    dynamicBackgroundEnabled = p[Keys.LYRIC_DYNAMIC_BACKGROUND] ?: d.dynamicBackgroundEnabled,
                    dynamicLyricsMode = p[Keys.LYRIC_DYNAMIC_MODE] ?: d.dynamicLyricsMode,
                    dynamicLyricsOpacity = p[Keys.LYRIC_DYNAMIC_OPACITY] ?: d.dynamicLyricsOpacity,
                    dynamicLyricsSaturation = p[Keys.LYRIC_DYNAMIC_SATURATION] ?: d.dynamicLyricsSaturation,
                    dynamicLyricsContrast = p[Keys.LYRIC_DYNAMIC_CONTRAST] ?: d.dynamicLyricsContrast,
                    lineMoveEnabled = p[Keys.LYRIC_LINE_MOVE] ?: d.lineMoveEnabled,
                    lineMoveMs = p[Keys.LYRIC_LINE_MOVE_MS] ?: d.lineMoveMs,
                    bgRevealEnabled = p[Keys.LYRIC_BG_REVEAL] ?: d.bgRevealEnabled,
                    bgRevealStartOffsetMs = p[Keys.LYRIC_BG_REVEAL_BEFORE_MS] ?: d.bgRevealStartOffsetMs,
                    bgRevealAfterMs = p[Keys.LYRIC_BG_REVEAL_AFTER_MS] ?: d.bgRevealAfterMs,
                    bgRevealDurationMs = p[Keys.LYRIC_BG_REVEAL_DURATION_MS] ?: d.bgRevealDurationMs,
                    bgRevealBlurEnabled = p[Keys.LYRIC_BG_REVEAL_BLUR] ?: d.bgRevealBlurEnabled,
                    bgRevealBlurAmount = p[Keys.LYRIC_BG_REVEAL_BLUR_AMOUNT] ?: d.bgRevealBlurAmount,
                    fixAnchorEnabled = p[Keys.LYRIC_FIX_ANCHOR] ?: d.fixAnchorEnabled,
                    bgRevealStyle = p[Keys.LYRIC_BG_REVEAL_STYLE] ?: d.bgRevealStyle,
                    bgRevealFlipAngle = p[Keys.LYRIC_BG_REVEAL_FLIP_ANGLE] ?: d.bgRevealFlipAngle,
                    bgRevealFlipPivot = p[Keys.LYRIC_BG_REVEAL_FLIP_PIVOT] ?: d.bgRevealFlipPivot,
                    bgRevealFadeEnabled = p[Keys.LYRIC_BG_REVEAL_FADE] ?: d.bgRevealFadeEnabled,
                    bgRevealFadeMs = p[Keys.LYRIC_BG_REVEAL_FADE_MS] ?: d.bgRevealFadeMs,
                    wordLiftEnabled = p[Keys.LYRIC_WORD_LIFT_ENABLED] ?: d.wordLiftEnabled,
                    wordLift = p[Keys.LYRIC_WORD_LIFT] ?: d.wordLift,
                    mergeWords = p[Keys.LYRIC_MERGE_WORDS] ?: d.mergeWords,
                    manualHeldWords = p[Keys.LYRIC_HELD_MANUAL] ?: d.manualHeldWords,
                    heldWordMinMs = p[Keys.LYRIC_HELD_MIN_MS] ?: d.heldWordMinMs,
                    optimizeHeldWords = p[Keys.LYRIC_HELD_OPTIMIZE] ?: d.optimizeHeldWords,
                    heldDefaultGlow = p[Keys.LYRIC_HELD_DEFAULT_GLOW] ?: d.heldDefaultGlow,
                    holdScrollEnabled = p[Keys.LYRIC_HOLD_SCROLL] ?: d.holdScrollEnabled,
                    holdScrollLines = p[Keys.LYRIC_HOLD_SCROLL_LINES] ?: d.holdScrollLines,
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
                    headerBlurOptimize = p[Keys.LYRIC_HEADER_BLUR_OPTIMIZE] ?: d.headerBlurOptimize,
                    headerBlurQuality = p[Keys.LYRIC_HEADER_BLUR_QUALITY] ?: d.headerBlurQuality,
                    dockBlurOptimize = p[Keys.LYRIC_DOCK_BLUR_OPTIMIZE] ?: d.dockBlurOptimize,
                    dockBlurQuality = p[Keys.LYRIC_DOCK_BLUR_QUALITY] ?: d.dockBlurQuality,
                    dockSoftEdge = p[Keys.LYRIC_DOCK_SOFT_EDGE] ?: d.dockSoftEdge,
                    dockEdgeFade = p[Keys.LYRIC_DOCK_EDGE_FADE] ?: d.dockEdgeFade,
                    kineticEnabled = p[Keys.LYRIC_KINETIC_ENABLED] ?: d.kineticEnabled,
                    kineticStyles = p[Keys.LYRIC_KINETIC_STYLE]?.let(::parseKineticStyles) ?: d.kineticStyles,
                    kineticSize = p[Keys.LYRIC_KINETIC_SIZE] ?: d.kineticSize,
                    kineticStyleSizes = p[Keys.LYRIC_KINETIC_STYLE_SIZES]?.let(::parseKineticStyleSizes) ?: d.kineticStyleSizes,
                    kineticPosition = p[Keys.LYRIC_KINETIC_POSITION] ?: d.kineticPosition,
                    kineticTranslationSize = p[Keys.LYRIC_KINETIC_TL_SIZE] ?: d.kineticTranslationSize,
                    kineticTranslationPosition = p[Keys.LYRIC_KINETIC_TL_POSITION] ?: d.kineticTranslationPosition,
                    kineticBgVocalEnabled = p[Keys.LYRIC_KINETIC_BGV_ENABLED] ?: d.kineticBgVocalEnabled,
                    kineticBgVocalStyles = p[Keys.LYRIC_KINETIC_BGV_STYLES]?.let(::parseKineticStyles) ?: d.kineticBgVocalStyles,
                    kineticBgVocalSize = p[Keys.LYRIC_KINETIC_BGV_SIZE] ?: d.kineticBgVocalSize,
                    kineticBgVocalPosition = p[Keys.LYRIC_KINETIC_BGV_POSITION] ?: d.kineticBgVocalPosition,
                    kineticRipples = p[Keys.LYRIC_KINETIC_RIPPLES] ?: d.kineticRipples,
                    kineticShadow = p[Keys.LYRIC_KINETIC_SHADOW] ?: d.kineticShadow,
                    kineticGlow = p[Keys.LYRIC_KINETIC_GLOW] ?: d.kineticGlow,
                    kineticBlur = p[Keys.LYRIC_KINETIC_BLUR] ?: d.kineticBlur,
                    kineticFlip3d = p[Keys.LYRIC_KINETIC_FLIP3D] ?: d.kineticFlip3d,
                    kineticSpeed = p[Keys.LYRIC_KINETIC_SPEED] ?: d.kineticSpeed,
                    kineticLetterDelay = p[Keys.LYRIC_KINETIC_LETTER_DELAY] ?: d.kineticLetterDelay,
                    kineticTextColors = p[Keys.LYRIC_KINETIC_TEXT_COLORS] ?: d.kineticTextColors,
                    kineticTextStrength = p[Keys.LYRIC_KINETIC_TEXT_STRENGTH] ?: d.kineticTextStrength,
                    kineticBgStrength = p[Keys.LYRIC_KINETIC_BG_STRENGTH] ?: d.kineticBgStrength,
                    kineticBgQuality = p[Keys.LYRIC_KINETIC_BG_QUALITY] ?: d.kineticBgQuality,
                    kineticBgFrame = p[Keys.LYRIC_KINETIC_BG_FRAME] ?: d.kineticBgFrame,
                    kineticTextPalette = p[Keys.LYRIC_KINETIC_TEXT_PALETTE] ?: d.kineticTextPalette,
                    kineticGlowWords = p[Keys.LYRIC_KINETIC_GLOW_WORDS] ?: d.kineticGlowWords,
                    kineticGlowIntensity = p[Keys.LYRIC_KINETIC_GLOW_INTENSITY] ?: d.kineticGlowIntensity,
                    kineticBgDefault = p[Keys.LYRIC_KINETIC_BG_DEFAULT] ?: d.kineticBgDefault,
                    kineticFontMix = p[Keys.LYRIC_KINETIC_FONT_MIX] ?: d.kineticFontMix,
                    kineticFontCount = p[Keys.LYRIC_KINETIC_FONT_COUNT] ?: d.kineticFontCount,
                    kineticFonts = p[Keys.LYRIC_KINETIC_FONTS] ?: d.kineticFonts,
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
                    lineEffect = p[Keys.LYRIC_LINE_EFFECT]?.let(LineEffect::fromString) ?: d.lineEffect,
                    stretchStrength = p[Keys.LYRIC_STRETCH_STRENGTH] ?: d.stretchStrength,
                    stretchDelayMs = p[Keys.LYRIC_STRETCH_DELAY_MS] ?: d.stretchDelayMs,
                    stretchDurationMs = p[Keys.LYRIC_STRETCH_DURATION_MS] ?: d.stretchDurationMs,
                    lightSweepEnabled = p[Keys.LYRIC_LIGHT_SWEEP] ?: d.lightSweepEnabled,
                    lightSweepContinuous = p[Keys.LYRIC_LIGHT_SWEEP_CONTINUOUS] ?: d.lightSweepContinuous,
                    lightSweepOklch = p[Keys.LYRIC_LIGHT_SWEEP_OKLCH] ?: d.lightSweepOklch,
                    lightSweepEase = p[Keys.LYRIC_LIGHT_SWEEP_EASE] ?: d.lightSweepEase,
                    lightSweepFlash = p[Keys.LYRIC_LIGHT_SWEEP_FLASH] ?: d.lightSweepFlash,
                    lightSweepSoftness = p[Keys.LYRIC_LIGHT_SWEEP_SOFTNESS] ?: d.lightSweepSoftness,
                    gradientLyricsEnabled = p[Keys.LYRIC_GRADIENT_ENABLED] ?: d.gradientLyricsEnabled,
                    lyricsLightEnabled = p[Keys.LYRIC_LIGHT_ENABLED] ?: d.lyricsLightEnabled,
                    lyricsLightIntensity = p[Keys.LYRIC_LIGHT_INTENSITY] ?: d.lyricsLightIntensity,
                    dynamicLyricsEnabled = p[Keys.LYRIC_DYNAMIC_ENABLED] ?: d.dynamicLyricsEnabled,
                    dynamicBackgroundEnabled = p[Keys.LYRIC_DYNAMIC_BACKGROUND] ?: d.dynamicBackgroundEnabled,
                    dynamicLyricsMode = p[Keys.LYRIC_DYNAMIC_MODE] ?: d.dynamicLyricsMode,
                    dynamicLyricsOpacity = p[Keys.LYRIC_DYNAMIC_OPACITY] ?: d.dynamicLyricsOpacity,
                    dynamicLyricsSaturation = p[Keys.LYRIC_DYNAMIC_SATURATION] ?: d.dynamicLyricsSaturation,
                    dynamicLyricsContrast = p[Keys.LYRIC_DYNAMIC_CONTRAST] ?: d.dynamicLyricsContrast,
                    lineMoveEnabled = p[Keys.LYRIC_LINE_MOVE] ?: d.lineMoveEnabled,
                    lineMoveMs = p[Keys.LYRIC_LINE_MOVE_MS] ?: d.lineMoveMs,
                    bgRevealEnabled = p[Keys.LYRIC_BG_REVEAL] ?: d.bgRevealEnabled,
                    bgRevealStartOffsetMs = p[Keys.LYRIC_BG_REVEAL_BEFORE_MS] ?: d.bgRevealStartOffsetMs,
                    bgRevealAfterMs = p[Keys.LYRIC_BG_REVEAL_AFTER_MS] ?: d.bgRevealAfterMs,
                    bgRevealDurationMs = p[Keys.LYRIC_BG_REVEAL_DURATION_MS] ?: d.bgRevealDurationMs,
                    bgRevealBlurEnabled = p[Keys.LYRIC_BG_REVEAL_BLUR] ?: d.bgRevealBlurEnabled,
                    bgRevealBlurAmount = p[Keys.LYRIC_BG_REVEAL_BLUR_AMOUNT] ?: d.bgRevealBlurAmount,
                    fixAnchorEnabled = p[Keys.LYRIC_FIX_ANCHOR] ?: d.fixAnchorEnabled,
                    bgRevealStyle = p[Keys.LYRIC_BG_REVEAL_STYLE] ?: d.bgRevealStyle,
                    bgRevealFlipAngle = p[Keys.LYRIC_BG_REVEAL_FLIP_ANGLE] ?: d.bgRevealFlipAngle,
                    bgRevealFlipPivot = p[Keys.LYRIC_BG_REVEAL_FLIP_PIVOT] ?: d.bgRevealFlipPivot,
                    bgRevealFadeEnabled = p[Keys.LYRIC_BG_REVEAL_FADE] ?: d.bgRevealFadeEnabled,
                    bgRevealFadeMs = p[Keys.LYRIC_BG_REVEAL_FADE_MS] ?: d.bgRevealFadeMs,
                    wordLiftEnabled = p[Keys.LYRIC_WORD_LIFT_ENABLED] ?: d.wordLiftEnabled,
                    wordLift = p[Keys.LYRIC_WORD_LIFT] ?: d.wordLift,
                    mergeWords = p[Keys.LYRIC_MERGE_WORDS] ?: d.mergeWords,
                    manualHeldWords = p[Keys.LYRIC_HELD_MANUAL] ?: d.manualHeldWords,
                    heldWordMinMs = p[Keys.LYRIC_HELD_MIN_MS] ?: d.heldWordMinMs,
                    optimizeHeldWords = p[Keys.LYRIC_HELD_OPTIMIZE] ?: d.optimizeHeldWords,
                    heldDefaultGlow = p[Keys.LYRIC_HELD_DEFAULT_GLOW] ?: d.heldDefaultGlow,
                    holdScrollEnabled = p[Keys.LYRIC_HOLD_SCROLL] ?: d.holdScrollEnabled,
                    holdScrollLines = p[Keys.LYRIC_HOLD_SCROLL_LINES] ?: d.holdScrollLines,
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
                    headerBlurOptimize = p[Keys.LYRIC_HEADER_BLUR_OPTIMIZE] ?: d.headerBlurOptimize,
                    headerBlurQuality = p[Keys.LYRIC_HEADER_BLUR_QUALITY] ?: d.headerBlurQuality,
                    dockBlurOptimize = p[Keys.LYRIC_DOCK_BLUR_OPTIMIZE] ?: d.dockBlurOptimize,
                    dockBlurQuality = p[Keys.LYRIC_DOCK_BLUR_QUALITY] ?: d.dockBlurQuality,
                    dockSoftEdge = p[Keys.LYRIC_DOCK_SOFT_EDGE] ?: d.dockSoftEdge,
                    dockEdgeFade = p[Keys.LYRIC_DOCK_EDGE_FADE] ?: d.dockEdgeFade,
                    kineticEnabled = p[Keys.LYRIC_KINETIC_ENABLED] ?: d.kineticEnabled,
                    kineticStyles = p[Keys.LYRIC_KINETIC_STYLE]?.let(::parseKineticStyles) ?: d.kineticStyles,
                    kineticSize = p[Keys.LYRIC_KINETIC_SIZE] ?: d.kineticSize,
                    kineticStyleSizes = p[Keys.LYRIC_KINETIC_STYLE_SIZES]?.let(::parseKineticStyleSizes) ?: d.kineticStyleSizes,
                    kineticPosition = p[Keys.LYRIC_KINETIC_POSITION] ?: d.kineticPosition,
                    kineticTranslationSize = p[Keys.LYRIC_KINETIC_TL_SIZE] ?: d.kineticTranslationSize,
                    kineticTranslationPosition = p[Keys.LYRIC_KINETIC_TL_POSITION] ?: d.kineticTranslationPosition,
                    kineticBgVocalEnabled = p[Keys.LYRIC_KINETIC_BGV_ENABLED] ?: d.kineticBgVocalEnabled,
                    kineticBgVocalStyles = p[Keys.LYRIC_KINETIC_BGV_STYLES]?.let(::parseKineticStyles) ?: d.kineticBgVocalStyles,
                    kineticBgVocalSize = p[Keys.LYRIC_KINETIC_BGV_SIZE] ?: d.kineticBgVocalSize,
                    kineticBgVocalPosition = p[Keys.LYRIC_KINETIC_BGV_POSITION] ?: d.kineticBgVocalPosition,
                    kineticRipples = p[Keys.LYRIC_KINETIC_RIPPLES] ?: d.kineticRipples,
                    kineticShadow = p[Keys.LYRIC_KINETIC_SHADOW] ?: d.kineticShadow,
                    kineticGlow = p[Keys.LYRIC_KINETIC_GLOW] ?: d.kineticGlow,
                    kineticBlur = p[Keys.LYRIC_KINETIC_BLUR] ?: d.kineticBlur,
                    kineticFlip3d = p[Keys.LYRIC_KINETIC_FLIP3D] ?: d.kineticFlip3d,
                    kineticSpeed = p[Keys.LYRIC_KINETIC_SPEED] ?: d.kineticSpeed,
                    kineticLetterDelay = p[Keys.LYRIC_KINETIC_LETTER_DELAY] ?: d.kineticLetterDelay,
                    kineticTextColors = p[Keys.LYRIC_KINETIC_TEXT_COLORS] ?: d.kineticTextColors,
                    kineticTextStrength = p[Keys.LYRIC_KINETIC_TEXT_STRENGTH] ?: d.kineticTextStrength,
                    kineticBgStrength = p[Keys.LYRIC_KINETIC_BG_STRENGTH] ?: d.kineticBgStrength,
                    kineticBgQuality = p[Keys.LYRIC_KINETIC_BG_QUALITY] ?: d.kineticBgQuality,
                    kineticBgFrame = p[Keys.LYRIC_KINETIC_BG_FRAME] ?: d.kineticBgFrame,
                    kineticTextPalette = p[Keys.LYRIC_KINETIC_TEXT_PALETTE] ?: d.kineticTextPalette,
                    kineticGlowWords = p[Keys.LYRIC_KINETIC_GLOW_WORDS] ?: d.kineticGlowWords,
                    kineticGlowIntensity = p[Keys.LYRIC_KINETIC_GLOW_INTENSITY] ?: d.kineticGlowIntensity,
                    kineticBgDefault = p[Keys.LYRIC_KINETIC_BG_DEFAULT] ?: d.kineticBgDefault,
                    kineticFontMix = p[Keys.LYRIC_KINETIC_FONT_MIX] ?: d.kineticFontMix,
                    kineticFontCount = p[Keys.LYRIC_KINETIC_FONT_COUNT] ?: d.kineticFontCount,
                    kineticFonts = p[Keys.LYRIC_KINETIC_FONTS] ?: d.kineticFonts,
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
            p[Keys.LYRIC_LINE_EFFECT] = n.lineEffect.value
            p[Keys.LYRIC_STRETCH_STRENGTH] = n.stretchStrength
            p[Keys.LYRIC_STRETCH_DELAY_MS] = n.stretchDelayMs
            p[Keys.LYRIC_STRETCH_DURATION_MS] = n.stretchDurationMs
            p[Keys.LYRIC_LIGHT_SWEEP] = n.lightSweepEnabled
            p[Keys.LYRIC_LIGHT_SWEEP_CONTINUOUS] = n.lightSweepContinuous
            p[Keys.LYRIC_LIGHT_SWEEP_OKLCH] = n.lightSweepOklch
            p[Keys.LYRIC_LIGHT_SWEEP_EASE] = n.lightSweepEase
            p[Keys.LYRIC_LIGHT_SWEEP_FLASH] = n.lightSweepFlash
            p[Keys.LYRIC_LIGHT_SWEEP_SOFTNESS] = n.lightSweepSoftness
            p[Keys.LYRIC_GRADIENT_ENABLED] = n.gradientLyricsEnabled
            p[Keys.LYRIC_LIGHT_ENABLED] = n.lyricsLightEnabled
            p[Keys.LYRIC_LIGHT_INTENSITY] = n.lyricsLightIntensity
            p[Keys.LYRIC_DYNAMIC_ENABLED] = n.dynamicLyricsEnabled
            p[Keys.LYRIC_DYNAMIC_BACKGROUND] = n.dynamicBackgroundEnabled
            p[Keys.LYRIC_DYNAMIC_MODE] = n.dynamicLyricsMode
            p[Keys.LYRIC_DYNAMIC_OPACITY] = n.dynamicLyricsOpacity
            p[Keys.LYRIC_DYNAMIC_SATURATION] = n.dynamicLyricsSaturation
            p[Keys.LYRIC_DYNAMIC_CONTRAST] = n.dynamicLyricsContrast
            p[Keys.LYRIC_LINE_MOVE] = n.lineMoveEnabled
            p[Keys.LYRIC_LINE_MOVE_MS] = n.lineMoveMs
            p[Keys.LYRIC_BG_REVEAL] = n.bgRevealEnabled
            p[Keys.LYRIC_BG_REVEAL_BEFORE_MS] = n.bgRevealStartOffsetMs
            p[Keys.LYRIC_BG_REVEAL_AFTER_MS] = n.bgRevealAfterMs
            p[Keys.LYRIC_BG_REVEAL_DURATION_MS] = n.bgRevealDurationMs
            p[Keys.LYRIC_BG_REVEAL_BLUR] = n.bgRevealBlurEnabled
            p[Keys.LYRIC_BG_REVEAL_BLUR_AMOUNT] = n.bgRevealBlurAmount
            p[Keys.LYRIC_FIX_ANCHOR] = n.fixAnchorEnabled
            p[Keys.LYRIC_BG_REVEAL_STYLE] = n.bgRevealStyle
            p[Keys.LYRIC_BG_REVEAL_FLIP_ANGLE] = n.bgRevealFlipAngle
            p[Keys.LYRIC_BG_REVEAL_FLIP_PIVOT] = n.bgRevealFlipPivot
            p[Keys.LYRIC_BG_REVEAL_FADE] = n.bgRevealFadeEnabled
            p[Keys.LYRIC_BG_REVEAL_FADE_MS] = n.bgRevealFadeMs
            p[Keys.LYRIC_WORD_LIFT_ENABLED] = n.wordLiftEnabled
            p[Keys.LYRIC_WORD_LIFT] = n.wordLift
            p[Keys.LYRIC_MERGE_WORDS] = n.mergeWords
            p[Keys.LYRIC_HELD_MANUAL] = n.manualHeldWords
            p[Keys.LYRIC_HELD_MIN_MS] = n.heldWordMinMs
            p[Keys.LYRIC_HELD_OPTIMIZE] = n.optimizeHeldWords
            p[Keys.LYRIC_HELD_DEFAULT_GLOW] = n.heldDefaultGlow
            p[Keys.LYRIC_HOLD_SCROLL] = n.holdScrollEnabled
            p[Keys.LYRIC_HOLD_SCROLL_LINES] = n.holdScrollLines
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
            p[Keys.LYRIC_HEADER_BLUR_OPTIMIZE] = n.headerBlurOptimize
            p[Keys.LYRIC_HEADER_BLUR_QUALITY] = n.headerBlurQuality
            p[Keys.LYRIC_DOCK_BLUR_OPTIMIZE] = n.dockBlurOptimize
            p[Keys.LYRIC_DOCK_BLUR_QUALITY] = n.dockBlurQuality
            p[Keys.LYRIC_DOCK_SOFT_EDGE] = n.dockSoftEdge
            p[Keys.LYRIC_DOCK_EDGE_FADE] = n.dockEdgeFade
            p[Keys.LYRIC_KINETIC_ENABLED] = n.kineticEnabled
            p[Keys.LYRIC_KINETIC_STYLE] = n.kineticStyles.joinToString(",") { it.value }
            p[Keys.LYRIC_KINETIC_SIZE] = n.kineticSize
            p[Keys.LYRIC_KINETIC_STYLE_SIZES] = n.kineticStyleSizes.entries.joinToString(",") { "${it.key.value}=${it.value}" }
            p[Keys.LYRIC_KINETIC_POSITION] = n.kineticPosition
            p[Keys.LYRIC_KINETIC_TL_SIZE] = n.kineticTranslationSize
            p[Keys.LYRIC_KINETIC_TL_POSITION] = n.kineticTranslationPosition
            p[Keys.LYRIC_KINETIC_BGV_ENABLED] = n.kineticBgVocalEnabled
            p[Keys.LYRIC_KINETIC_BGV_STYLES] = n.kineticBgVocalStyles.joinToString(",") { it.value }
            p[Keys.LYRIC_KINETIC_BGV_SIZE] = n.kineticBgVocalSize
            p[Keys.LYRIC_KINETIC_BGV_POSITION] = n.kineticBgVocalPosition
            p[Keys.LYRIC_KINETIC_RIPPLES] = n.kineticRipples
            p[Keys.LYRIC_KINETIC_SHADOW] = n.kineticShadow
            p[Keys.LYRIC_KINETIC_GLOW] = n.kineticGlow
            p[Keys.LYRIC_KINETIC_BLUR] = n.kineticBlur
            p[Keys.LYRIC_KINETIC_FLIP3D] = n.kineticFlip3d
            p[Keys.LYRIC_KINETIC_SPEED] = n.kineticSpeed
            p[Keys.LYRIC_KINETIC_LETTER_DELAY] = n.kineticLetterDelay
            p[Keys.LYRIC_KINETIC_TEXT_COLORS] = n.kineticTextColors
            p[Keys.LYRIC_KINETIC_TEXT_STRENGTH] = n.kineticTextStrength
            p[Keys.LYRIC_KINETIC_BG_STRENGTH] = n.kineticBgStrength
            p[Keys.LYRIC_KINETIC_BG_QUALITY] = n.kineticBgQuality
            p[Keys.LYRIC_KINETIC_BG_FRAME] = n.kineticBgFrame
            p[Keys.LYRIC_KINETIC_TEXT_PALETTE] = n.kineticTextPalette
            p[Keys.LYRIC_KINETIC_GLOW_WORDS] = n.kineticGlowWords
            p[Keys.LYRIC_KINETIC_GLOW_INTENSITY] = n.kineticGlowIntensity
            p[Keys.LYRIC_KINETIC_BG_DEFAULT] = n.kineticBgDefault
            p[Keys.LYRIC_KINETIC_FONT_MIX] = n.kineticFontMix
            p[Keys.LYRIC_KINETIC_FONT_COUNT] = n.kineticFontCount
            p[Keys.LYRIC_KINETIC_FONTS] = n.kineticFonts
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

    /** 液态玻璃调节项，顺序固定，与 app 侧 LiquidGlassSetting 的 ordinal 一一对应。 */
    private val liquidGlassTuningKeys =
        listOf(
            Keys.LIQUID_GLASS_BLUR,
            Keys.LIQUID_GLASS_QUALITY,
            Keys.LIQUID_GLASS_TINT,
            Keys.LIQUID_GLASS_CORNER,
            Keys.LIQUID_GLASS_REFRACTION,
            Keys.LIQUID_GLASS_REFRACTION_HEIGHT,
            Keys.LIQUID_GLASS_FALLOFF,
            Keys.LIQUID_GLASS_RIM,
            Keys.LIQUID_GLASS_RIM_WIDTH,
            Keys.LIQUID_GLASS_SHADOW,
            Keys.LIQUID_GLASS_MODE,
            Keys.LIQUID_GLASS_SATURATION,
        )

    fun getLiquidGlassTuning(defaults: List<Int>): Flow<List<Int>> =
        context.dataStore.data.map { preferences ->
            liquidGlassTuningKeys.mapIndexed { index, key -> preferences[key] ?: defaults[index] }
        }.distinctUntilChanged()

    suspend fun setLiquidGlassTuning(
        index: Int,
        value: Int,
    ) {
        context.dataStore.edit { preferences ->
            preferences[liquidGlassTuningKeys[index]] = value
        }
    }

    suspend fun resetLiquidGlassTuning() {
        context.dataStore.edit { preferences ->
            liquidGlassTuningKeys.forEach { preferences.remove(it) }
        }
    }

    /** 液态玻璃 V2 调节项，顺序固定，与 app 侧 LiquidGlassV2Setting 的 ordinal 一一对应。 */
    private val liquidGlassV2TuningKeys =
        listOf(
            "corner",
            "blur",
            "refraction_height",
            "refraction_amount",
            "depth",
            "chromatic",
            "vibrancy",
            "tint_opacity",
            "tint_color",
            "highlight_style",
            "highlight_width",
            "highlight_alpha",
            "highlight_angle",
            "highlight_falloff",
            "shadow_radius",
            "shadow_alpha",
            "inner_radius",
            "inner_alpha",
            "press",
        ).map { intPreferencesKey("liquid_glass_v2_$it") }

    fun getLiquidGlassV2Tuning(defaults: List<Int>): Flow<List<Int>> =
        context.dataStore.data.map { preferences ->
            liquidGlassV2TuningKeys.mapIndexed { index, key -> preferences[key] ?: defaults[index] }
        }.distinctUntilChanged()

    suspend fun setLiquidGlassV2Tuning(
        index: Int,
        value: Int,
    ) {
        context.dataStore.edit { preferences ->
            preferences[liquidGlassV2TuningKeys[index]] = value
        }
    }

    suspend fun resetLiquidGlassV2Tuning() {
        context.dataStore.edit { preferences ->
            liquidGlassV2TuningKeys.forEach { preferences.remove(it) }
        }
    }

    /** 底栏自定义（位置、尺寸、标签内容）：数值项顺序与 app 侧 BottomBarSetting 的 ordinal 对应。 */
    private val bottomBarIntKeys =
        listOf(
            "mini_h",
            "mini_raise",
            "mini_w",
            "mini_height",
            "tabs_h",
            "tabs_raise",
            "tabs_w",
            "tabs_height",
            "label_mode",
            "search_tab",
            "bubble",
            "bubble_size",
        ).map { intPreferencesKey("bottom_bar_$it") }

    /** 文本项：每个标签依次存放 图标名、文字，共 4 个标签（发现、音乐、曲库、搜索）。 */
    private val bottomBarTextKeys =
        (0 until 4).flatMap {
            listOf(stringPreferencesKey("bottom_bar_icon_$it"), stringPreferencesKey("bottom_bar_label_$it"))
        }

    fun getBottomBarCustomization(
        defaultInts: List<Int>,
        defaultTexts: List<String>,
    ): Flow<Pair<List<Int>, List<String>>> =
        context.dataStore.data.map { preferences ->
            bottomBarIntKeys.mapIndexed { index, key -> preferences[key] ?: defaultInts[index] } to
                bottomBarTextKeys.mapIndexed { index, key -> preferences[key] ?: defaultTexts[index] }
        }.distinctUntilChanged()

    suspend fun setBottomBarInt(
        index: Int,
        value: Int,
    ) {
        context.dataStore.edit { preferences -> preferences[bottomBarIntKeys[index]] = value }
    }

    suspend fun setBottomBarText(
        index: Int,
        value: String,
    ) {
        context.dataStore.edit { preferences -> preferences[bottomBarTextKeys[index]] = value }
    }

    suspend fun resetBottomBarCustomization() {
        context.dataStore.edit { preferences ->
            bottomBarIntKeys.forEach { preferences.remove(it) }
            bottomBarTextKeys.forEach { preferences.remove(it) }
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

/** "auto" or a comma separated list such as "stack,zoom" (the old single value still works). */
private fun parseKineticStyles(raw: String): Set<KineticStyle> =
    raw
        .split(',')
        .mapNotNull { v -> KineticStyle.entries.firstOrNull { it.value == v.trim() } }
        .toSet()
        .ifEmpty { setOf(KineticStyle.Auto) }

/** Such as "stack=120,zoom=90". Unknown styles and bad numbers are skipped. */
private fun parseKineticStyleSizes(raw: String): Map<KineticStyle, Int> =
    raw
        .split(',')
        .mapNotNull { part ->
            val kv = part.split('=')
            val style = KineticStyle.entries.firstOrNull { it.value == kv.getOrNull(0)?.trim() }
            val size = kv.getOrNull(1)?.trim()?.toIntOrNull()
            if (style == null || size == null) null else style to size
        }.toMap()
