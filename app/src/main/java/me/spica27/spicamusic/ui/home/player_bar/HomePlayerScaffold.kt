package me.spica27.spicamusic.ui.home.player_bar

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeLayoutState
import androidx.compose.ui.layout.SubcomposeSlotReusePolicy
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazePerformanceMode
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import me.spica27.spicamusic.R
import me.spica27.spicamusic.common.entity.BottomBarLayout
import me.spica27.spicamusic.ui.theme.EaseOutEmphasized
import me.spica27.spicamusic.ui.theme.LocalReducedMotion
import kotlin.math.roundToInt

private const val MiniPlayerFadeEnd = 0.3f
private const val NavigationFadeEnd = 0.5f

private enum class PlayerLayoutSlot { MiniPlayer, Navigation, Page, Blur, Player }

/** 尺寸不纳入状态观察，拖拽仅更新布局和绘制。 */
private class PlayerLayoutMeasurements {
    var height = 0
    var miniPlayerHeight = 0
    var navigationHeight = 0
    val travel: Float get() = (height - miniPlayerHeight - navigationHeight).coerceAtLeast(0).toFloat()
}

/** 将全屏播放器裁剪为随进度扩展的卡片。 */
private class PlayerRevealShape(
    private val height: Float,
    private val radius: Float,
    private val bottomRadius: Float,
    private val leftInset: Float,
    private val rightInset: Float,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline =
        Outline.Rounded(
            RoundRect(
                left = leftInset,
                top = 0f,
                right = size.width - rightInset,
                bottom = height.coerceIn(0f, size.height),
                topLeftCornerRadius = CornerRadius(radius),
                topRightCornerRadius = CornerRadius(radius),
                bottomLeftCornerRadius = CornerRadius(bottomRadius),
                bottomRightCornerRadius = CornerRadius(bottomRadius),
            ),
        )
}

/** 复用迷你播放条，随展开进度移动并渐隐。 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun HomePlayerScaffold(
    playerAvailable: Boolean,
    sheetState: PlayerSheetState,
    navigationBar: @Composable () -> Unit,
    miniPlayer: @Composable (dragModifier: Modifier) -> Unit,
    fullScreenPlayer: @Composable (progress: () -> Float, dragModifier: Modifier) -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = true,
    onMiniPlayerDragStart: () -> Unit = {},
    layout: BottomBarLayout = BottomBarLayout.Traditional,
    blurHazeState: HazeState? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    val reducedMotion = LocalReducedMotion.current
    val keyboardVisible = WindowInsets.isImeVisible
    val colors = MaterialTheme.colorScheme
    val appearance = BottomBarAppearance.forLayout(layout)
    // 底栏自定义：左右/上下位置与宽高。
    val custom = LocalBottomBarCustomization.current
    val collapsedPlayerColor = appearance.miniPlayerContainerColor(colors)
    // Bottom blur replaces the solid / fading tint behind the bar.
    val blurOn = blurHazeState != null && custom.blur == 1 && custom.blurAmount > 0
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val safeInsets = WindowInsets.safeDrawing
    val horizontalMargin = with(density) { appearance.horizontalMargin.toPx() }
    val leftInset = if (appearance.isFloating) horizontalMargin + safeInsets.getLeft(density, layoutDirection) else 0f
    val rightInset = if (appearance.isFloating) horizontalMargin + safeInsets.getRight(density, layoutDirection) else 0f
    val miniPlayerGap = with(density) { appearance.gap.toPx() }
    val navigationBottomPadding = safeInsets.getBottom(density) + with(density) { appearance.bottomMargin.toPx() }
    val measurements = remember { PlayerLayoutMeasurements() }
    val layoutState = remember { SubcomposeLayoutState(SubcomposeSlotReusePolicy(1)) }
    val progress = remember(sheetState) { { sheetState.progress } }
    val sheetVisible = sheetState.isVisible
    val dragState = rememberDraggableState { sheetState.dragBy(it, measurements.travel) }
    val distanceThreshold = with(density) { 56.dp.toPx() }
    val velocityThreshold = with(density) { 700.dp.toPx() }
    val dragModifier =
        Modifier.draggable(
            state = dragState,
            orientation = Orientation.Vertical,
            enabled = active && playerAvailable,
            onDragStarted = {
                if (!sheetState.isVisible) onMiniPlayerDragStart()
                sheetState.beginDrag()
            },
            onDragStopped = { velocity ->
                sheetState.settle(velocity, measurements.travel, distanceThreshold, velocityThreshold, reducedMotion)
            },
        )
    val enter =
        if (reducedMotion) {
            fadeIn(snap())
        } else {
            fadeIn(tween(180, easing = EaseOutEmphasized)) +
                expandVertically(tween(220, easing = EaseOutEmphasized), expandFrom = Alignment.Bottom)
        }
    val exit =
        if (reducedMotion) {
            fadeOut(snap())
        } else {
            fadeOut(tween(140, easing = EaseOutEmphasized)) +
                shrinkVertically(tween(180, easing = EaseOutEmphasized), shrinkTowards = Alignment.Bottom)
        }
    val hiddenSemantics = if (sheetVisible) Modifier.clearAndSetSemantics {} else Modifier

    LaunchedEffect(playerAvailable, reducedMotion) {
        if (!playerAvailable) sheetState.animateTo(false, reducedMotion)
    }
    // 播放器后注册，优先处理队列和歌词的返回操作。
    BackHandler(enabled = active && sheetVisible) { sheetState.animateTo(false, reducedMotion) }

    // 复用槽位内容，展开进度仅在布局和绘制阶段读取。
    val miniSlot: @Composable () -> Unit =
        remember(playerAvailable, keyboardVisible, miniPlayer, dragModifier, appearance, enter, exit, hiddenSemantics, custom) {
            {
                Box(
                    hiddenSemantics.graphicsLayer {
                        alpha = (1f - progress() / MiniPlayerFadeEnd).coerceIn(0f, 1f)
                        // 向上抬高；随播放器展开逐渐归位。
                        translationY = -custom.miniRaise.dp.toPx() * (1f - progress().coerceIn(0f, 1f))
                    },
                ) {
                    AnimatedVisibility(visible = playerAvailable && !keyboardVisible, enter = enter, exit = exit) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .then(
                                    if (appearance.isFloating) {
                                        Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                                    } else {
                                        Modifier
                                    },
                                ).padding(
                                    start = appearance.horizontalMargin,
                                    end = appearance.horizontalMargin,
                                    bottom = appearance.gap,
                                ),
                        ) {
                            Box(
                                Modifier
                                    .align(BiasAlignment(custom.miniHorizontal / 50f - 1f, 0f))
                                    .fillMaxWidth(custom.miniWidth / 100f)
                                    .then(if (custom.miniHeight >= 44) Modifier.height(custom.miniHeight.dp) else Modifier),
                            ) { miniPlayer(dragModifier) }
                        }
                    }
                }
            }
        }
    val navigationSlot: @Composable () -> Unit =
        remember(keyboardVisible, navigationBar, appearance, colors.surface, navigationBottomPadding, enter, exit, hiddenSemantics, custom, blurOn) {
            {
                Box(
                    hiddenSemantics.graphicsLayer {
                        val fraction = progress()
                        alpha = (1f - fraction / NavigationFadeEnd).coerceIn(0f, 1f)
                        translationY = size.height * fraction - custom.tabsRaise.dp.toPx() * (1f - fraction.coerceIn(0f, 1f))
                    },
                ) {
                    AnimatedVisibility(visible = !keyboardVisible, enter = enter, exit = exit) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .then(
                                    if (blurOn) {
                                        Modifier
                                    } else if (appearance.isFloating) {
                                        Modifier.drawBehind {
                                            // 悬浮栏下方渐隐至主题底色，衔接系统手势区。
                                            val fadeStart = (size.height - navigationBottomPadding - 24.dp.toPx()).coerceAtLeast(0f)
                                            val fadeEnd = size.height - navigationBottomPadding + appearance.bottomMargin.toPx()
                                            drawRect(
                                                brush =
                                                    Brush.verticalGradient(
                                                        listOf(colors.surface.copy(alpha = 0f), colors.surface),
                                                        startY = fadeStart,
                                                        endY = fadeEnd,
                                                    ),
                                                topLeft = Offset(0f, fadeStart),
                                                size = Size(size.width, size.height - fadeStart),
                                            )
                                        }
                                    } else {
                                        Modifier.background(colors.surface)
                                    },
                                ).windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom),
                                ).padding(
                                    start = appearance.horizontalMargin,
                                    end = appearance.horizontalMargin,
                                    bottom = appearance.bottomMargin,
                                ),
                        ) {
                            Box(
                                Modifier
                                    .align(BiasAlignment(custom.tabsHorizontal / 50f - 1f, 0f))
                                    .fillMaxWidth(custom.tabsWidth / 100f)
                                    .then(if (custom.tabsHeight >= 44) Modifier.height(custom.tabsHeight.dp) else Modifier),
                            ) { navigationBar() }
                        }
                    }
                }
            }
        }
    val playerSlot: @Composable () -> Unit =
        remember(
            sheetVisible,
            fullScreenPlayer,
            dragModifier,
            colors,
            appearance,
            collapsedPlayerColor,
            leftInset,
            rightInset,
            miniPlayerGap,
        ) {
            {
                Box(
                    if (sheetVisible) {
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val fraction = progress()
                                val miniHeight = (measurements.miniPlayerHeight - miniPlayerGap).coerceAtLeast(0f)
                                val cardHeight = miniHeight + (size.height - miniHeight) * fraction
                                shape =
                                    PlayerRevealShape(
                                        cardHeight,
                                        appearance.revealTopRadius.toPx() * (1f - fraction),
                                        appearance.revealBottomRadius.toPx() * (1f - fraction),
                                        leftInset * (1f - fraction),
                                        rightInset * (1f - fraction),
                                    )
                                clip = true
                            }.drawBehind { drawRect(lerp(collapsedPlayerColor, colors.surface, progress())) }
                    } else {
                        Modifier
                    },
                ) {
                    if (sheetVisible) fullScreenPlayer(progress, dragModifier)
                }
            }
        }

    SubcomposeLayout(state = layoutState, modifier = modifier.fillMaxSize().imePadding().clipToBounds()) { constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val chromeConstraints = constraints.copy(minWidth = width, minHeight = 0)
        val mini = subcompose(PlayerLayoutSlot.MiniPlayer, miniSlot).single().measure(chromeConstraints)
        val navigation = subcompose(PlayerLayoutSlot.Navigation, navigationSlot).single().measure(chromeConstraints)
        measurements.height = height
        measurements.miniPlayerHeight = mini.height
        measurements.navigationHeight = navigation.height
        val padding = PaddingValues(bottom = (mini.height + navigation.height).toDp())
        // 按底栏实测高度设置页面留白。
        val page =
            subcompose(PlayerLayoutSlot.Page) {
                Box(Modifier.fillMaxSize().consumeWindowInsets(padding)) {
                    Box(hiddenSemantics.fillMaxSize()) { content(padding) }
                    if (sheetVisible) {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .graphicsLayer { alpha = progress() * 0.18f }
                                .background(colors.scrim)
                                .clickable(interactionSource = null, indication = null) { sheetState.animateTo(false, reducedMotion) }
                                .clearAndSetSemantics {},
                        )
                    }
                }
            }.single().measure(Constraints.fixed(width, height))
        // Blur band behind the bar: solid part up to the bar top + position, then an optional soft fade above it.
        val barHeight = mini.height + navigation.height
        val blurSolid = if (blurOn) (barHeight + custom.blurPosition.dp.roundToPx().coerceAtLeast(-barHeight)).coerceAtLeast(0) else 0
        val blurFade = if (blurOn && custom.softEdge == 1) custom.edgeFade.dp.roundToPx().coerceAtLeast(0) else 0
        val blur =
            if (blurHazeState != null && blurSolid + blurFade > 0) {
                subcompose(PlayerLayoutSlot.Blur) {
                    BottomBlurBand(
                        hazeState = blurHazeState,
                        amount = custom.blurAmount,
                        solidHeight = blurSolid.toDp(),
                        fadeHeight = blurFade.toDp(),
                        alpha = { (1f - progress() / NavigationFadeEnd).coerceIn(0f, 1f) },
                    )
                }.single().measure(Constraints.fixed(width, blurSolid + blurFade))
            } else {
                null
            }
        val player = subcompose(PlayerLayoutSlot.Player, playerSlot).single().measure(Constraints.fixed(width, height))
        layout(width, height) {
            val fraction = progress()
            val top = (measurements.travel * (1f - fraction)).roundToInt()
            page.placeRelative(0, 0)
            blur?.placeRelative(0, height - blur.height)
            navigation.placeRelative(0, height - navigation.height)
            // 透明后置于播放器下层，避免拦截触摸。
            if (fraction < MiniPlayerFadeEnd) {
                player.placeRelative(0, top)
                mini.placeRelative(0, top)
            } else {
                mini.placeRelative(0, top)
                player.placeRelative(0, top)
            }
        }
    }
}

/** 底栏后面的渐进模糊：与音乐页顶栏同一思路，但向上渐隐；只采样下面的列表，不带色调。 */
@OptIn(ExperimentalHazeApi::class)
@Composable
private fun BottomBlurBand(
    hazeState: HazeState,
    amount: Int,
    solidHeight: Dp,
    fadeHeight: Dp,
    alpha: () -> Float,
) {
    val background = MaterialTheme.colorScheme.background
    val hardStyle =
        remember(background, amount) {
            HazeBlurStyle {
                blurRadius(amount.dp)
                noiseFactor(0f)
                fallbackColorEffect(HazeColorEffect.tint(background.copy(alpha = 0.85f)))
            }
        }
    val fadeStyle =
        remember(background, amount) {
            HazeBlurStyle {
                blurRadius(amount.dp)
                noiseFactor(0f)
                fallbackColorEffect(HazeColorEffect.tint(background.copy(alpha = 0.85f)))
                progressive(HazeProgressive.verticalGradient(startIntensity = 0f, endIntensity = 1f))
            }
        }
    Column(Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha() }) {
        if (fadeHeight > 0.dp) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(fadeHeight)
                    .hazeBlur(
                        input = HazeInput.Sources(hazeState),
                        style = fadeStyle,
                        performanceMode = HazePerformanceMode.Balanced,
                    ),
            )
        }
        if (solidHeight > 0.dp) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(solidHeight)
                    .hazeBlur(
                        input = HazeInput.Sources(hazeState),
                        style = hardStyle,
                        performanceMode = HazePerformanceMode.Balanced,
                    ),
            )
        }
    }
}

@Composable
internal fun PlayerSheetHandle(
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.collapse)
    Box(
        modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(role = Role.Button, onClick = onCollapse)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(36.dp, 4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
    }
}
