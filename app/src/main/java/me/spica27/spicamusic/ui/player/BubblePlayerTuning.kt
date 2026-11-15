package me.spica27.spicamusic.ui.player

import androidx.compose.runtime.Immutable

/** Index of every Bubble setting inside [BubblePlayerTuning.toList]. New settings are only appended. */
internal object BubbleSetting {
    const val AlbumWidth = 0
    const val AlbumHeight = 1
    const val Corner = 2
    const val AlbumBackground = 3
    const val YearInfo = 4
    const val PlaySize = 5
    const val SkipSize = 6
    const val ButtonGap = 7
    const val ButtonBottom = 8
    const val ButtonShift = 9
    const val TopBarHeight = 10
    const val ProgressHeight = 11
    const val TitleBarHeight = 12
    const val LyricsBarHeight = 13
    const val TimeText = 14
    const val NextText = 15
    const val LyricsText = 16
    const val TitleText = 17
    const val ArtistText = 18
    const val BlurEnabled = 19
    const val BlurAmount = 20
    const val BlurOptimize = 21
    const val BlurQuality = 22
    const val BarOrder = 23
    const val HlStyle = 24
    const val HlWidth = 25
    const val HlAlpha = 26
    const val HlAngle = 27
    const val HlFalloff = 28
    const val SeekHeight = 29
    const val SeekThickness = 30
    const val SeekWidth = 31
    const val InfoBarPos = 32
    const val InfoBarHeight = 33
    const val InfoText = 34
    const val TimeLabel = 35
    const val NextLabel = 36
    const val InfoBarStyle = 37
    const val ParticleTrail = 38
}

/**
 * Layout values of the Bubble player style. Sizes are dp, text sizes are sp x 10, switches are 0/1.
 * [barOrderCode] holds the order of the three bottom bars as three digits (0 progress, 1 title, 2 lyrics).
 */
@Immutable
data class BubblePlayerTuning(
    val albumWidthDp: Int = 260,
    val albumHeightDp: Int = 260,
    val cornerDp: Int = 28,
    val albumBackground: Int = 0,
    val yearInfo: Int = 0,
    val playSizeDp: Int = 56,
    val skipSizeDp: Int = 40,
    val buttonGapDp: Int = 12,
    val buttonBottomDp: Int = 16,
    val buttonShiftDp: Int = 0,
    val topBarHeightDp: Int = 58,
    val progressHeightDp: Int = 70,
    val titleBarHeightDp: Int = 56,
    val lyricsBarHeightDp: Int = 44,
    val timeTextTenths: Int = 220,
    val nextTextTenths: Int = 125,
    val lyricsTextTenths: Int = 140,
    val titleTextTenths: Int = 150,
    val artistTextTenths: Int = 125,
    val blurEnabled: Int = 1,
    val blurAmountDp: Int = 14,
    val blurOptimize: Int = 0,
    val blurQuality: Int = 45,
    val barOrderCode: Int = DEFAULT_ORDER_CODE,
    /** Album highlight: 0 off, 1 directional, 2 ambient, 3 solid (same as the new album style). */
    val hlStyle: Int = 0,
    val hlWidthTenths: Int = 12,
    val hlAlphaPercent: Int = 50,
    val hlAngle: Int = 45,
    val hlFalloffTenths: Int = 20,
    /** Progress bar (glow particles) size: area height dp, line thickness dp, width in percent of the bar. */
    val seekHeightDp: Int = 40,
    val seekThicknessDp: Int = 3,
    val seekWidthPercent: Int = 100,
    /** Year / album / disc bar: 0 above the top bar, 1 below the top bar, 2 hidden. */
    val infoBarPos: Int = 0,
    val infoBarHeightDp: Int = 38,
    val infoTextTenths: Int = 125,
    /** Size of the small "Total listening:" and "Upcoming song:" labels in the top bar. */
    val timeLabelTenths: Int = 105,
    val nextLabelTenths: Int = 105,
    /** Year / album / disc bar style: 0 three columns, 1 one line. */
    val infoBarStyle: Int = 0,
    /** Particle trail behind the progress bar head: 0 off, 1 on. */
    val particleTrail: Int = 0,
) {
    fun toList(): List<Int> =
        listOf(
            albumWidthDp,
            albumHeightDp,
            cornerDp,
            albumBackground,
            yearInfo,
            playSizeDp,
            skipSizeDp,
            buttonGapDp,
            buttonBottomDp,
            buttonShiftDp,
            topBarHeightDp,
            progressHeightDp,
            titleBarHeightDp,
            lyricsBarHeightDp,
            timeTextTenths,
            nextTextTenths,
            lyricsTextTenths,
            titleTextTenths,
            artistTextTenths,
            blurEnabled,
            blurAmountDp,
            blurOptimize,
            blurQuality,
            barOrderCode,
            hlStyle,
            hlWidthTenths,
            hlAlphaPercent,
            hlAngle,
            hlFalloffTenths,
            seekHeightDp,
            seekThicknessDp,
            seekWidthPercent,
            infoBarPos,
            infoBarHeightDp,
            infoTextTenths,
            timeLabelTenths,
            nextLabelTenths,
            infoBarStyle,
            particleTrail,
        )

    fun encode(): String = toList().joinToString(",")

    /** Returns a copy with one setting changed (see [BubbleSetting]). */
    fun withValue(
        index: Int,
        value: Int,
    ): BubblePlayerTuning = fromList(toList().toMutableList().also { it[index] = value })

    /** The three bottom bars from top to bottom: 0 progress, 1 title, 2 lyrics. */
    val barOrder: List<Int>
        get() = decodeOrder(barOrderCode)

    /** Moves one bottom bar up (delta -1) or down (delta 1). */
    fun withMovedBar(
        bar: Int,
        delta: Int,
    ): BubblePlayerTuning {
        val order = barOrder.toMutableList()
        val from = order.indexOf(bar)
        val to = from + delta
        if (from < 0 || to !in order.indices) return this
        val moved = order.removeAt(from)
        order.add(to, moved)
        return copy(barOrderCode = encodeOrder(order))
    }

    companion object {
        const val BAR_PROGRESS = 0
        const val BAR_TITLE = 1
        const val BAR_LYRICS = 2
        const val DEFAULT_ORDER_CODE = 12 // digits 0, 1, 2

        val Default = BubblePlayerTuning()

        private fun encodeOrder(order: List<Int>): Int = order[0] * 100 + order[1] * 10 + order[2]

        private fun decodeOrder(code: Int): List<Int> {
            val order = listOf(code / 100 % 10, code / 10 % 10, code % 10)
            return if (order.toSet() == setOf(0, 1, 2)) order else listOf(0, 1, 2)
        }

        /** Missing entries (older saved text) fall back to the defaults, so adding settings never resets old ones. */
        fun fromList(v: List<Int>): BubblePlayerTuning {
            val d = Default.toList()

            fun g(i: Int) = v.getOrNull(i) ?: d[i]
            return BubblePlayerTuning(
                g(0), g(1), g(2), g(3), g(4), g(5), g(6), g(7), g(8), g(9), g(10), g(11),
                g(12), g(13), g(14), g(15), g(16), g(17), g(18), g(19), g(20), g(21), g(22), g(23),
                g(24), g(25), g(26), g(27), g(28),
                g(29), g(30), g(31),
                g(32), g(33), g(34), g(35), g(36), g(37), g(38),
            )
        }

        fun decode(text: String): BubblePlayerTuning = fromList(text.split(',').mapNotNull { it.trim().toIntOrNull() })
    }
}
