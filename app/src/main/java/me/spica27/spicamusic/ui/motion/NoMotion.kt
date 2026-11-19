package me.spica27.spicamusic.ui.motion

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * 全应用无动画层：与 Compose 动画 API 同名同参，但所有状态变化都是即时的。
 * 需要恢复动画时，把各文件里的 import 改回 androidx 的同名 API 即可。
 */

/** 无进场、无退场的转场。 */
val NoTransition: ContentTransform = EnterTransition.None togetherWith ExitTransition.None

@Composable
fun <S> AnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER")
    transitionSpec: AnimatedContentTransitionScope<S>.() -> ContentTransform = { NoTransition },
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "AnimatedContent",
    contentKey: (targetState: S) -> Any? = { it },
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit,
) {
    androidx.compose.animation.AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = { NoTransition },
        contentAlignment = contentAlignment,
        label = label,
        contentKey = contentKey,
        content = content,
    )
}

@Composable
fun AnimatedVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") enter: EnterTransition = EnterTransition.None,
    @Suppress("UNUSED_PARAMETER") exit: ExitTransition = ExitTransition.None,
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    me.spica27.spicamusic.ui.motion.AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
        label = label,
        content = content,
    )
}

@Composable
fun AnimatedVisibility(
    visibleState: MutableTransitionState<Boolean>,
    modifier: Modifier = Modifier,
    @Suppress("UNUSED_PARAMETER") enter: EnterTransition = EnterTransition.None,
    @Suppress("UNUSED_PARAMETER") exit: ExitTransition = ExitTransition.None,
    label: String = "AnimatedVisibility",
    content: @Composable AnimatedVisibilityScope.() -> Unit,
) {
    me.spica27.spicamusic.ui.motion.AnimatedVisibility(
        visibleState = visibleState,
        modifier = modifier,
        enter = EnterTransition.None,
        exit = ExitTransition.None,
        label = label,
        content = content,
    )
}

@Composable
private fun <T> rememberInstant(
    targetValue: T,
    finishedListener: ((T) -> Unit)?,
): State<T> {
    val listener = rememberUpdatedState(finishedListener)
    val first = remember { booleanArrayOf(true) }
    LaunchedEffect(targetValue) {
        if (first[0]) first[0] = false else listener.value?.invoke(targetValue)
    }
    return rememberUpdatedState(targetValue)
}

@Composable
fun animateFloatAsState(
    targetValue: Float,
    @Suppress("UNUSED_PARAMETER") animationSpec: AnimationSpec<Float>? = null,
    @Suppress("UNUSED_PARAMETER") visibilityThreshold: Float = 0.01f,
    @Suppress("UNUSED_PARAMETER") label: String = "FloatAnimation",
    finishedListener: ((Float) -> Unit)? = null,
): State<Float> = rememberInstant(targetValue, finishedListener)

@Composable
fun animateColorAsState(
    targetValue: Color,
    @Suppress("UNUSED_PARAMETER") animationSpec: AnimationSpec<Color>? = null,
    @Suppress("UNUSED_PARAMETER") label: String = "ColorAnimation",
    finishedListener: ((Color) -> Unit)? = null,
): State<Color> = rememberInstant(targetValue, finishedListener)

@Composable
fun animateDpAsState(
    targetValue: Dp,
    @Suppress("UNUSED_PARAMETER") animationSpec: AnimationSpec<Dp>? = null,
    @Suppress("UNUSED_PARAMETER") label: String = "DpAnimation",
    finishedListener: ((Dp) -> Unit)? = null,
): State<Dp> = rememberInstant(targetValue, finishedListener)

/** 不再循环的“无限动画”：保持在初始值。 */
class StaticTransition

@Composable
fun rememberInfiniteTransition(
    @Suppress("UNUSED_PARAMETER") label: String = "InfiniteTransition",
): StaticTransition = remember { StaticTransition() }

@Composable
fun StaticTransition.animateFloat(
    initialValue: Float,
    @Suppress("UNUSED_PARAMETER") targetValue: Float,
    @Suppress("UNUSED_PARAMETER") animationSpec: InfiniteRepeatableSpec<Float>,
    @Suppress("UNUSED_PARAMETER") label: String = "FloatAnimation",
): State<Float> = remember(initialValue) { mutableStateOf(initialValue) }

/** 代替 Animatable.animateTo：直接跳到目标值。 */
suspend fun <T, V : AnimationVector> Animatable<T, V>.animateToNow(
    targetValue: T,
    @Suppress("UNUSED_PARAMETER") animationSpec: AnimationSpec<T>? = null,
    @Suppress("UNUSED_PARAMETER") initialVelocity: T? = null,
    @Suppress("UNUSED_PARAMETER") block: (Animatable<T, V>.() -> Unit)? = null,
) {
    snapTo(targetValue)
}

/** 代替 Animatable.animateDecay（仅用于回弹归零）：直接归零。 */
suspend fun Animatable<Float, AnimationVector1D>.animateDecayNow(
    @Suppress("UNUSED_PARAMETER") initialVelocity: Float,
    @Suppress("UNUSED_PARAMETER") animationSpec: DecayAnimationSpec<Float>,
    @Suppress("UNUSED_PARAMETER") block: (Animatable<Float, AnimationVector1D>.() -> Unit)? = null,
) {
    snapTo(0f)
}
