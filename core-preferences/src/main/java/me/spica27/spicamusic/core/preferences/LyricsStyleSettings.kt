package me.spica27.spicamusic.core.preferences

/** Where the transliteration (romanized text) sits relative to the lyric words. */
/** What the lines below the current one do after a line change. */
enum class LineEffect(
    val value: String,
) {
    Stretch("stretch"),
    Jiggle("jiggle"),
    Off("off"),
    ;

    companion object {
        fun fromString(value: String?): LineEffect = entries.firstOrNull { it.value == value } ?: Stretch
    }
}

enum class TransliterationPosition(
    val value: String,
) {
    Top("top"),
    Bottom("bottom"),
    ;

    companion object {
        fun fromString(value: String?): TransliterationPosition = entries.firstOrNull { it.value == value } ?: Bottom
    }
}

/**
 * Every user-adjustable lyrics option, with the defaults and ranges of the reference lyrics window.
 * All percentages are plain Ints (100 = 100%).
 */
data class LyricsStyleSettings(
    // ---- Font ----
    /** Font for the whole player: "default" or "system". */
    val fontStyle: String = "default",
    /** Font for the lyrics only: "same" follows [fontStyle]. */
    val lyricsFont: String = "same",
    /** 400 Regular .. 800 Extra bold. */
    val lyricsWeight: Int = DEFAULT_WEIGHT,
    // ---- Text size (percent) ----
    val lyricsSize: Int = 100,
    val transliterationSize: Int = 100,
    val translationSize: Int = 100,
    val backgroundTranslationSize: Int = 100,
    // ---- Transliteration ----
    val attachToWords: Boolean = true,
    val transliterationPosition: TransliterationPosition = TransliterationPosition.Bottom,
    // ---- Blur and opacity (percent) ----
    val unsungOpacity: Int = 50,
    val blur: Int = 100,
    // ---- Motion ----
    /** Time the lyrics take to scroll to the current line, in ms. */
    val moveDurationMs: Int = 600,
    /** Where the playing line sits on screen, percent of the height (5..70). */
    val currentLinePosition: Int = 40,
    // ---- Jiggle ----
    val jiggleEnabled: Boolean = true,
    val jiggleStrength: Int = 50,
    val jiggleDurationMs: Int = 1200,
    val jiggleDelayMs: Int = 45,
    // ---- Line effect (Stretch is the Apple Music style: no bounce, the gaps between lines stretch) ----
    val lineEffect: LineEffect = LineEffect.Stretch,
    /** How much farther lines lag the further down they are (percent). */
    val stretchStrength: Int = 60,
    /** Wait of each line after the one above it, in ms. */
    val stretchDelayMs: Int = 45,
    /** Time each line below the current one takes to settle, in ms (0 = they jump). */
    val stretchDurationMs: Int = 600,
    // ---- Light sweep (off = the original word-by-word sweep) ----
    val lightSweepEnabled: Boolean = false,
    val lightSweepContinuous: Boolean = true,
    val lightSweepOklch: Boolean = true,
    val lightSweepEase: Boolean = true,
    val lightSweepFlash: Boolean = true,
    /** Width of the soft edge of the light, in percent (100 = normal). */
    val lightSweepSoftness: Int = 100,
    // ---- Lyrics gradient and light effects (off = the original look) ----
    /** Sung lyrics use the same colour gradient as the top progress bar. */
    val gradientLyricsEnabled: Boolean = false,
    /** Glow around the sung word and a bright spark at the front, like the progress bar head. */
    val lyricsLightEnabled: Boolean = false,
    /** Strength of the light effects in percent. */
    val lyricsLightIntensity: Int = 70,
    // ---- Color player UI (player colours from the album art; off = the theme colours) ----
    /** Colour the player UI (text, icons, buttons, lyrics) from the album art. */
    val dynamicLyricsEnabled: Boolean = false,
    /** Also tint the player background with the album colour (needs [dynamicLyricsEnabled]). */
    val dynamicBackgroundEnabled: Boolean = false,
    /** "auto" follows the app theme, "dark" or "light" forces the tone. */
    val dynamicLyricsMode: String = "auto",
    /** Opacity of the lyrics colour in percent. */
    val dynamicLyricsOpacity: Int = 100,
    /** Saturation of the album colour in percent (100 = as in the cover). */
    val dynamicLyricsSaturation: Int = 100,
    /** Distance from the background in percent: lighter on dark, darker on light. */
    val dynamicLyricsContrast: Int = 50,
    // ---- Lyrics progress (all off = the original behaviour) ----
    val wordLiftEnabled: Boolean = false,
    /** Custom word lift in tenths of dp (15 = 1.5 dp), used when [wordLiftEnabled]. */
    val wordLift: Int = 15,
    /** Join the pieces of one word that the lyrics file splits (sym + phony = symphony). */
    val mergeWords: Boolean = false,
    val manualHeldWords: Boolean = false,
    /** A word counts as held from this length in ms, used when [manualHeldWords]. */
    val heldWordMinMs: Int = 1000,
    /** Smooth the lyrics clock so held-word animations do not step. */
    val optimizeHeldWords: Boolean = false,
    /** With [optimizeHeldWords]: keep the original blurred glow instead of the smooth one. */
    val heldDefaultGlow: Boolean = false,
    /** Keep the lyrics on a line that is still being sung while the next line has already started. */
    val holdScrollEnabled: Boolean = false,
    /** How many lines the lyrics may hold back (1..3), used when [holdScrollEnabled]. */
    val holdScrollLines: Int = 2,
    // ---- Enhanced header (lyrics window) ----
    /** false = the original header (no line, no blur). */
    val enhanceHeader: Boolean = false,
    /** Cover size in dp. */
    val headerAlbumSize: Int = 56,
    /** Title text size in sp. */
    val headerTitleSize: Int = 22,
    /** Artist text size in sp. */
    val headerArtistSize: Int = 14,
    /** Like and "more" button size in dp. */
    val headerButtonSize: Int = 44,
    /** Separator line weight in steps of 0.5 dp (2 = 1 dp). */
    val headerLineWeight: Int = 2,
    /** Separator line opacity in percent. */
    val headerLineOpacity: Int = 18,
    /** Space between the header content and the line, in dp. */
    val headerBottomPadding: Int = 12,
    /** true: lyrics scroll behind a blurred header. false: the line separates header and lyrics. */
    val headerBlurEnabled: Boolean = true,
    /** Blur radius in dp. */
    val headerBlurAmount: Int = 16,
    /** true: the blur fades out below the header instead of stopping at a hard edge. */
    val headerSoftEdge: Boolean = true,
    /** Height of the fade band below the header, in dp. */
    val headerEdgeFade: Int = 32,
    /** Thin outline around the album cover. */
    val headerOutlineEnabled: Boolean = true,
    /** Outline weight in steps of 0.5 dp (2 = 1 dp). */
    val headerOutlineWeight: Int = 2,
    /** Outline opacity in percent. */
    val headerOutlineOpacity: Int = 15,
    // ---- Lyrics page bottom bar (translation / transliteration buttons + player pill) ----
    /** Show the translation lines (the right button toggles this). */
    val showTranslation: Boolean = true,
    /** Show the transliteration lines (the left button toggles this). */
    val showTransliteration: Boolean = true,
    /** true: the buttons and the pill blur the lyrics behind them. false: solid fill colour. */
    val dockBlurEnabled: Boolean = true,
    /** Blur radius in dp. */
    val dockBlurAmount: Int = 14,
    /** Height of the pill and diameter of the side buttons, in dp. */
    val dockSize: Int = 52,
    /** Space under the bottom bar, in dp. */
    val dockBottomPadding: Int = 18,
    /** Space inside the pill around the cover and the play button, in dp. */
    val dockPillPadding: Int = 6,
    /** Space between the transliteration button and the pill, in dp. */
    val dockGapLeft: Int = 10,
    /** Space between the translation button and the pill, in dp. */
    val dockGapRight: Int = 10,
    /** false: every button and the pill blur on their own (normal). true: one blur behind the whole bar. */
    val dockSpreadBlur: Boolean = false,
    /** Spread only: the blur fades out above the bar instead of ending at a hard edge. */
    val dockSoftEdge: Boolean = true,
    /** Spread only: height of the fade band above the bar, in dp. */
    val dockEdgeFade: Int = 32,
    // ---- Line and background vocal animation ----
    /** A line moves a little (up and smaller) after it has been sung. */
    val lineMoveEnabled: Boolean = false,
    /** Duration of that move in ms. */
    val lineMoveMs: Int = 600,
    /** Background vocals expand out from behind the main lyric before they are sung and collapse after. */
    val bgRevealEnabled: Boolean = false,
    /** When the expand starts, relative to the background vocal start, in ms. Negative = before it starts. */
    val bgRevealStartOffsetMs: Int = -1000,
    /** Collapse starts this long after the background vocal is sung, in ms. */
    val bgRevealAfterMs: Int = 1000,
    /** Duration of the expand and of the collapse, in ms. */
    val bgRevealDurationMs: Int = 800,
    /** Blur the background vocal while it expands or collapses. */
    val bgRevealBlurEnabled: Boolean = true,
    /** Blur radius in dp. */
    val bgRevealBlurAmount: Int = 8,
    /** Keeps lines and the current line steady on their anchor while a background vocal expands or collapses. */
    val fixAnchorEnabled: Boolean = true,
) {
    /** Returns a copy with every value forced into its allowed range. */
    fun sanitized(): LyricsStyleSettings =
        copy(
            lyricsWeight = (lyricsWeight / 100 * 100).coerceIn(WEIGHT_RANGE),
            lyricsSize = lyricsSize.coerceIn(LYRICS_SIZE_RANGE),
            transliterationSize = transliterationSize.coerceIn(SECONDARY_SIZE_RANGE),
            translationSize = translationSize.coerceIn(SECONDARY_SIZE_RANGE),
            backgroundTranslationSize = backgroundTranslationSize.coerceIn(SECONDARY_SIZE_RANGE),
            unsungOpacity = unsungOpacity.coerceIn(UNSUNG_RANGE),
            blur = blur.coerceIn(BLUR_RANGE),
            moveDurationMs = moveDurationMs.coerceIn(MOVE_RANGE),
            currentLinePosition = currentLinePosition.coerceIn(POSITION_RANGE),
            jiggleStrength = jiggleStrength.coerceIn(JIGGLE_STRENGTH_RANGE),
            jiggleDurationMs = jiggleDurationMs.coerceIn(JIGGLE_DURATION_RANGE),
            jiggleDelayMs = jiggleDelayMs.coerceIn(JIGGLE_DELAY_RANGE),
            stretchStrength = stretchStrength.coerceIn(STRETCH_STRENGTH_RANGE),
            stretchDelayMs = stretchDelayMs.coerceIn(STRETCH_DELAY_RANGE),
            stretchDurationMs = stretchDurationMs.coerceIn(STRETCH_DURATION_RANGE),
            lightSweepSoftness = lightSweepSoftness.coerceIn(LIGHT_SWEEP_SOFTNESS_RANGE),
            lyricsLightIntensity = lyricsLightIntensity.coerceIn(LYRICS_LIGHT_RANGE),
            dynamicLyricsOpacity = dynamicLyricsOpacity.coerceIn(DYNAMIC_OPACITY_RANGE),
            dynamicLyricsSaturation = dynamicLyricsSaturation.coerceIn(DYNAMIC_SATURATION_RANGE),
            dynamicLyricsContrast = dynamicLyricsContrast.coerceIn(DYNAMIC_CONTRAST_RANGE),
            wordLift = wordLift.coerceIn(WORD_LIFT_RANGE),
            heldWordMinMs = heldWordMinMs.coerceIn(HELD_WORD_MIN_RANGE),
            holdScrollLines = holdScrollLines.coerceIn(HOLD_SCROLL_RANGE),
            headerAlbumSize = headerAlbumSize.coerceIn(HEADER_ALBUM_RANGE),
            headerTitleSize = headerTitleSize.coerceIn(HEADER_TITLE_RANGE),
            headerArtistSize = headerArtistSize.coerceIn(HEADER_ARTIST_RANGE),
            headerButtonSize = headerButtonSize.coerceIn(HEADER_BUTTON_RANGE),
            headerLineWeight = headerLineWeight.coerceIn(HEADER_LINE_WEIGHT_RANGE),
            headerLineOpacity = headerLineOpacity.coerceIn(HEADER_LINE_OPACITY_RANGE),
            headerBottomPadding = headerBottomPadding.coerceIn(HEADER_BOTTOM_PADDING_RANGE),
            headerBlurAmount = headerBlurAmount.coerceIn(HEADER_BLUR_RANGE),
            headerEdgeFade = headerEdgeFade.coerceIn(HEADER_EDGE_FADE_RANGE),
            headerOutlineWeight = headerOutlineWeight.coerceIn(HEADER_OUTLINE_WEIGHT_RANGE),
            headerOutlineOpacity = headerOutlineOpacity.coerceIn(HEADER_OUTLINE_OPACITY_RANGE),
            dockBlurAmount = dockBlurAmount.coerceIn(DOCK_BLUR_RANGE),
            dockSize = dockSize.coerceIn(DOCK_SIZE_RANGE),
            dockBottomPadding = dockBottomPadding.coerceIn(DOCK_BOTTOM_PADDING_RANGE),
            dockPillPadding = dockPillPadding.coerceIn(DOCK_PILL_PADDING_RANGE),
            dockGapLeft = dockGapLeft.coerceIn(DOCK_GAP_RANGE),
            dockGapRight = dockGapRight.coerceIn(DOCK_GAP_RANGE),
            dockEdgeFade = dockEdgeFade.coerceIn(DOCK_EDGE_FADE_RANGE),
            lineMoveMs = lineMoveMs.coerceIn(LINE_MOVE_AFTER_RANGE),
            bgRevealStartOffsetMs = bgRevealStartOffsetMs.coerceIn(BG_REVEAL_START_OFFSET_RANGE),
            bgRevealAfterMs = bgRevealAfterMs.coerceIn(BG_REVEAL_OFFSET_RANGE),
            bgRevealDurationMs = bgRevealDurationMs.coerceIn(BG_REVEAL_DURATION_RANGE),
            bgRevealBlurAmount = bgRevealBlurAmount.coerceIn(BG_REVEAL_BLUR_RANGE),
        )

    companion object {
        const val DEFAULT_WEIGHT = 700
        val WEIGHT_RANGE = 400..800
        val LYRICS_SIZE_RANGE = 70..140
        val SECONDARY_SIZE_RANGE = 60..200
        val UNSUNG_RANGE = 10..100
        val BLUR_RANGE = 0..200
        val MOVE_RANGE = 200..1500
        val POSITION_RANGE = 5..70
        val JIGGLE_STRENGTH_RANGE = 0..100
        val JIGGLE_DURATION_RANGE = 0..2500
        val JIGGLE_DELAY_RANGE = 0..150
        val STRETCH_STRENGTH_RANGE = 0..100
        val STRETCH_DELAY_RANGE = 0..150
        val STRETCH_DURATION_RANGE = 0..2500
        val LIGHT_SWEEP_SOFTNESS_RANGE = 50..200
        val LYRICS_LIGHT_RANGE = 0..100
        val DYNAMIC_OPACITY_RANGE = 20..100
        val DYNAMIC_SATURATION_RANGE = 0..200
        val DYNAMIC_CONTRAST_RANGE = 0..100
        val WORD_LIFT_RANGE = 0..80
        val HELD_WORD_MIN_RANGE = 0..3000
        val HOLD_SCROLL_RANGE = 1..3
        val HEADER_ALBUM_RANGE = 40..96
        val HEADER_TITLE_RANGE = 14..32
        val HEADER_ARTIST_RANGE = 10..22
        val HEADER_BUTTON_RANGE = 32..64
        val HEADER_LINE_WEIGHT_RANGE = 1..8
        val HEADER_LINE_OPACITY_RANGE = 0..100
        val HEADER_BOTTOM_PADDING_RANGE = 0..40
        val HEADER_BLUR_RANGE = 0..40
        val HEADER_EDGE_FADE_RANGE = 0..80
        val HEADER_OUTLINE_WEIGHT_RANGE = 1..8
        val HEADER_OUTLINE_OPACITY_RANGE = 0..100
        val DOCK_BLUR_RANGE = 0..40
        val DOCK_SIZE_RANGE = 40..72
        val DOCK_BOTTOM_PADDING_RANGE = 0..48
        val DOCK_PILL_PADDING_RANGE = 0..16
        val DOCK_GAP_RANGE = 0..32
        val DOCK_EDGE_FADE_RANGE = 0..80
        val LINE_MOVE_AFTER_RANGE = 200..2000
        val BG_REVEAL_OFFSET_RANGE = 0..1500

        /** Negative = the expand starts before the background vocal starts, positive = after it has started. */
        val BG_REVEAL_START_OFFSET_RANGE = -1000..1500
        val BG_REVEAL_DURATION_RANGE = 500..2000
        val BG_REVEAL_BLUR_RANGE = 1..20
    }
}
