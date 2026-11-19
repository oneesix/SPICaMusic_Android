package me.spica27.spicamusic.ui.theme

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier

/** 以前用于列表入场的间隔与等待时间；现在无入场动画，保留为 0 供既有的 delay 调用使用。 */
const val ENTRANCE_STAGGER_MILLIS = 0L
const val ENTRANCE_GATE_MILLIS = 0L

/** 入场动画已移除：内容直接显示。保留签名，调用处无需改动。 */
@Stable
@Suppress("UNUSED_PARAMETER")
fun Modifier.entrance(
    order: Int,
    play: Boolean = true,
    reducedMotion: Boolean? = null,
    replayKey: Int = 0,
): Modifier = this
