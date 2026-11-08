package me.spica27.spicamusic.core.preferences

/** Where the transliteration (romanized text) sits relative to the lyric words. */
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
        val JIGGLE_STRENGTH_RANGE = 10..100
        val JIGGLE_DURATION_RANGE = 500..2500
        val JIGGLE_DELAY_RANGE = 0..150
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
    }
}
