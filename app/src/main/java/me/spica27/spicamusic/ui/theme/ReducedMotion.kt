package me.spica27.spicamusic.ui.theme

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * 是否走降级动效。应用已移除动画，默认始终为 true，
 * 因此仍读取它的代码（底部栏、播放面板等）都会直接跳到目标状态。
 */
val LocalReducedMotion: ProvidableCompositionLocal<Boolean> = staticCompositionLocalOf { true }
