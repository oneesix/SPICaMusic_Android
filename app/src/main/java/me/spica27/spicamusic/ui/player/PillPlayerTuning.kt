package me.spica27.spicamusic.ui.player

import androidx.compose.runtime.Immutable

/** Layout values (dp) of the pill player style. Order matches [toList] and the settings sliders. */
@Immutable
data class PillPlayerTuning(
    val albumWidthDp: Int = 216,
    val albumHeightDp: Int = 300,
    val bottomRadiusDp: Int = 95,
    val ringPaddingDp: Int = 14,
    val actionsHeightDp: Int = 12,
    val playSizeDp: Int = 56,
    val playHeightDp: Int = 16,
    val skipSizeDp: Int = 30,
    val skipSideDp: Int = 20,
    val skipHeightDp: Int = 0,
) {
    fun toList(): List<Int> =
        listOf(
            albumWidthDp,
            albumHeightDp,
            bottomRadiusDp,
            ringPaddingDp,
            actionsHeightDp,
            playSizeDp,
            playHeightDp,
            skipSizeDp,
            skipSideDp,
            skipHeightDp,
        )

    fun encode(): String = toList().joinToString(",")

    companion object {
        const val STYLE_CLASSIC = "classic"
        const val STYLE_PILL = "pill"

        val Default = PillPlayerTuning()

        fun fromList(v: List<Int>): PillPlayerTuning =
            if (v.size == 10) {
                PillPlayerTuning(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], v[8], v[9])
            } else {
                Default
            }

        fun decode(text: String): PillPlayerTuning =
            fromList(text.split(',').mapNotNull { it.trim().toIntOrNull() })
    }
}
