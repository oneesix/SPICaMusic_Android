package me.spica27.spicamusic.ui.widget

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/** 粒子消散效果已移除：删除时立即完成。 */
object ParticleDissolveDefaults {
    const val DURATION_MILLIS = 0
    const val WAVE_DELAY_MILLIS = 0L
}

/** 不再播放消散动画；[isDissolving] 变为 true 时直接回调 [onComplete]。 */
fun Modifier.particleDissolve(
    isDissolving: Boolean,
    onComplete: () -> Unit = {},
): Modifier =
    composed {
        val currentOnComplete by rememberUpdatedState(onComplete)
        LaunchedEffect(isDissolving) {
            if (isDissolving) currentOnComplete()
        }
        Modifier
    }
