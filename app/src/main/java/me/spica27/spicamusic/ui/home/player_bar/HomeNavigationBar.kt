package me.spica27.spicamusic.ui.home.player_bar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import android.os.Build
import me.spica27.spicamusic.R
import me.spica27.spicamusic.common.entity.BottomBarLayout
import me.spica27.spicamusic.ui.glass.LocalGlassV2Backdrop
import me.spica27.spicamusic.ui.glass.liquidGlassBubble
import me.spica27.spicamusic.ui.glass.LocalLiquidGlassConfig
import me.spica27.spicamusic.ui.home.HomePage
import me.spica27.spicamusic.ui.theme.LocalReducedMotion
import me.spica27.spicamusic.ui.widget.LucideIcons
import kotlin.math.abs
import kotlin.math.roundToInt

private val IndicatorInset = 8.dp

/** 两种布局共用导航交互，悬浮布局与迷你播放器共享页面玻璃背景。 */
@Composable
internal fun HomeNavigationBar(
    selectedPage: HomePage,
    onPageSelected: (HomePage) -> Unit,
    modifier: Modifier = Modifier,
    glassEnabled: Boolean = LocalLiquidGlassConfig.current.enabled,
    hazeState: HazeState? = null,
    layout: BottomBarLayout = BottomBarLayout.Traditional,
    onSearchClick: () -> Unit = {},
) {
    val appearance = BottomBarAppearance.forLayout(layout)
    val pages = HomePage.entries
    val custom = LocalBottomBarCustomization.current
    // 搜索标签固定在最右侧，只打开搜索页，不会成为选中页。
    val searchIndex = if (custom.searchTab == 1) pages.size else -1
    val tabCount = pages.size + if (searchIndex >= 0) 1 else 0
    val scope = rememberCoroutineScope()
    val indicator = remember(scope, tabCount) { GlassNavigationIndicatorState(selectedPage.ordinal, tabCount, scope) }
    val onSearch by rememberUpdatedState(onSearchClick)
    val glassBackdrop = LocalGlassV2Backdrop.current
    val currentSelection by rememberUpdatedState(selectedPage)
    val onSelection by rememberUpdatedState(onPageSelected)
    val reducedMotion = LocalReducedMotion.current
    val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme
    var bounds by remember { mutableStateOf(IntSize.Zero) }
    val tabWidth = bounds.width.toFloat() / tabCount
    val inset = with(density) { IndicatorInset.toPx() }
    val interacting = indicator.isInteracting && !reducedMotion
    val press =
        animateFloatAsState(
            targetValue = if (interacting) 1f else 0f,
            animationSpec =
                when {
                    reducedMotion -> snap()
                    interacting -> spring(dampingRatio = 0.85f, stiffness = 900f)
                    else -> tween(140)
                },
            label = "navigationPress",
        )

    // 长按气泡需要 Android 13+ 的折射着色器和页面背景层。
    val bubbleActive =
        custom.bubble == 1 && glassBackdrop != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !reducedMotion

    LaunchedEffect(selectedPage, reducedMotion, tabCount) { indicator.synchronize(selectedPage.ordinal, reducedMotion) }

    Box(
        modifier
            .fillMaxWidth()
            .playerBarSurface(
                hazeState,
                appearance.navigationContainerColor(colors),
                glassEnabled,
                appearance.navigationShape,
                appearance.isFloating,
            ).selectableGroup()
            .onSizeChanged { bounds = it }
            .pointerInput(indicator, tabWidth, isLtr, reducedMotion) {
                if (tabWidth <= 0f) return@pointerInput
                val direction = if (isLtr) 1f else -1f
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val tracker = VelocityTracker()
                    tracker.addPosition(down.uptimeMillis, down.position)
                    indicator.press()
                    try {
                        val dragStart =
                            awaitHorizontalTouchSlopOrCancellation(down.id) { change, overSlop ->
                                val start = if (isLtr) down.position.x else size.width - down.position.x
                                indicator.beginDrag((start / tabWidth - 0.5f).coerceIn(0f, (tabCount - 1).toFloat()))
                                tracker.addPosition(change.uptimeMillis, change.position)
                                indicator.dragBy(direction * overSlop / tabWidth, 0f)
                                change.consume()
                            }
                        if (dragStart != null) {
                            val completed =
                                horizontalDrag(dragStart.id) { change ->
                                    tracker.addPosition(change.uptimeMillis, change.position)
                                    indicator.dragBy(
                                        direction * change.positionChange().x / tabWidth,
                                        direction * tracker.calculateVelocity().x / tabWidth,
                                    )
                                    change.consume()
                                }
                            val nearest = indicator.nearestIndex()
                            if (completed && nearest == searchIndex) {
                                indicator.animateTo(currentSelection.ordinal, reducedMotion)
                                onSearch()
                            } else {
                                val target = if (completed) pages[nearest] else currentSelection
                                indicator.animateTo(target.ordinal, reducedMotion)
                                if (completed && target != currentSelection) onSelection(target)
                            }
                        }
                    } finally {
                        indicator.release()
                        if (indicator.isDragging) indicator.animateTo(currentSelection.ordinal, reducedMotion)
                    }
                }
            },
    ) {
        // 长按气泡在图标下方，折射页面内容。
        if (bubbleActive && glassBackdrop != null && bounds.width > 0 && bounds.height > 0 && press.value > 0.01f) {
            val bubbleWidth = (tabWidth - 2f * inset).coerceAtLeast(1f)
            val bubbleHeight = (bounds.height - 2f * inset).coerceAtLeast(1f)
            val maxScaleY = (bounds.height / bubbleHeight).coerceAtLeast(1f)
            Box(
                Modifier
                    .offset {
                        val x =
                            if (isLtr) {
                                indicator.position * tabWidth + inset
                            } else {
                                bounds.width - (indicator.position + 1f) * tabWidth + inset
                            }
                        IntOffset(
                            x.coerceIn(inset / 2f, (bounds.width - bubbleWidth - inset / 2f).coerceAtLeast(inset / 2f)).roundToInt(),
                            inset.roundToInt(),
                        )
                    }.size(with(density) { bubbleWidth.toDp() }, with(density) { bubbleHeight.toDp() })
                    .liquidGlassBubble(
                        backdrop = glassBackdrop,
                        shape = appearance.indicatorShape,
                        scaleXProvider = { 1f + (custom.bubbleSize / 100f - 1f) * press.value },
                        scaleYProvider = { (1f + (custom.bubbleSize / 100f - 1f) * 1.1f * press.value).coerceAtMost(maxScaleY) },
                        alphaProvider = { press.value },
                    ).clearAndSetSemantics {},
            )
        }
        // 前景导航项与指示器分层，玻璃背景只采样页面。
        Row(
            Modifier.fillMaxWidth(),
        ) {
            pages.forEach { page ->
                val selected = page == selectedPage
                NavigationTabItem(
                    modifier = Modifier.weight(1f),
                    selected = selected,
                    onClick = {
                        indicator.animateTo(page.ordinal, reducedMotion)
                        if (page != currentSelection) onSelection(page)
                    },
                    iconName = custom.icons.getOrElse(page.ordinal) { "" },
                    fallbackIcon = if (selected) page.selectedIcon else page.icon,
                    label = custom.labels.getOrElse(page.ordinal) { "" }.ifBlank { stringResource(page.titleRes) },
                    labelMode = custom.labelMode,
                    color = if (selected) colors.primary else colors.onSurfaceVariant,
                    minHeight = appearance.navigationMinHeight,
                    verticalPadding = appearance.navigationVerticalPadding,
                )
            }
            if (searchIndex >= 0) {
                NavigationTabItem(
                    modifier = Modifier.weight(1f),
                    selected = false,
                    onClick = onSearch,
                    iconName = custom.icons.getOrElse(searchIndex) { "" },
                    fallbackIcon = Icons.Default.Search,
                    label = custom.labels.getOrElse(searchIndex) { "" }.ifBlank { stringResource(R.string.nav_tab_search) },
                    labelMode = custom.labelMode,
                    color = colors.onSurfaceVariant,
                    minHeight = appearance.navigationMinHeight,
                    verticalPadding = appearance.navigationVerticalPadding,
                )
            }
        }
        if (bounds.width > 0 && bounds.height > 0) {
            val lensWidth = (tabWidth - 2f * inset).coerceAtLeast(1f)
            val lensHeight = (bounds.height - 2f * inset).coerceAtLeast(1f)
            Box(
                Modifier
                    .size(with(density) { lensWidth.toDp() }, with(density) { lensHeight.toDp() })
                    .graphicsLayer {
                        val stretch = (abs(indicator.velocity) / 6f).coerceIn(0f, 1f) * press.value
                        scaleX = 1f + 0.035f * press.value + 0.025f * stretch
                        scaleY = 1f + 0.045f * press.value - 0.02f * stretch
                        alpha = if (bubbleActive) 1f - press.value else 1f
                        val x =
                            if (isLtr) {
                                indicator.position * tabWidth + inset
                            } else {
                                bounds.width - (indicator.position + 1f) * tabWidth +
                                    inset
                            }
                        translationX = x.coerceIn(inset / 2f, (bounds.width - lensWidth - inset / 2f).coerceAtLeast(inset / 2f))
                        translationY = inset
                    }.clip(appearance.indicatorShape)
                    .then(
                        Modifier.background(colors.primary.copy(alpha = 0.09f)),
                    ).clearAndSetSemantics {},
            )
        }
    }
}

/** 单个导航标签：支持 Lucide 图标、自定义文字，以及“图标+文字 / 仅图标 / 仅文字”。 */
@Composable
private fun NavigationTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconName: String,
    fallbackIcon: ImageVector,
    label: String,
    labelMode: Int,
    color: androidx.compose.ui.graphics.Color,
    minHeight: androidx.compose.ui.unit.Dp,
    verticalPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val iconRes = remember(iconName) { LucideIcons.resolve(context, iconName) }
    Column(
        modifier
            .heightIn(min = minHeight)
            .selectable(
                selected = selected,
                role = Role.Tab,
                interactionSource = null,
                indication = null,
                onClick = onClick,
            ).semantics(mergeDescendants = true) { contentDescription = label }
            .padding(horizontal = 8.dp, vertical = verticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
    ) {
        if (labelMode != BottomBarLabelMode.TextOnly) {
            if (iconRes != 0) {
                Icon(painterResource(iconRes), null, Modifier.size(24.dp), tint = color)
            } else {
                Icon(fallbackIcon, null, Modifier.size(24.dp), tint = color)
            }
        }
        if (labelMode != BottomBarLabelMode.IconOnly) {
            Text(
                label,
                color = color,
                style = if (labelMode == BottomBarLabelMode.TextOnly) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(widthDp = 360)
@Composable
private fun HomeNavigationBarPreview() {
    var page by remember { mutableStateOf(HomePage.Music) }
    MaterialTheme { HomeNavigationBar(page, { page = it }) }
}
