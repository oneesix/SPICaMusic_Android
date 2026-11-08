package me.spica27.spicamusic.ui.widget

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.spica27.spicamusic.common.entity.LyricItem
import kotlin.math.ceil
import kotlin.math.max

/*
 * Break line ("interlude"): three dots and a countdown, shown for a long intro or a long instrumental gap.
 * Same rules as the reference window:
 *  - the song starts with 5 s or more before the first lyric, or
 *  - two lines are 7 s or more apart.
 * The break starts when the previous line ends and finishes 0.3 s before the next line starts.
 */

internal const val INTERLUDE_KEY_PREFIX = "~interlude~"
private const val INTRO_MIN_MS = 5000L
private const val BREAK_MIN_MS = 7000L
private const val INTERLUDE_LEAD_MS = 300L

private val InterludeEase = CubicBezierEasing(0.2f, 0.8f, 0.2f, 1f)

/** True for the break lines created by [insertInterludes]. */
internal fun LyricItem.isInterlude(): Boolean = this is LyricItem.NormalLyric && key.startsWith(INTERLUDE_KEY_PREFIX)

/** End of a break line in ms (it ends 0.3 s before the next lyric starts). */
internal fun LyricItem.interludeEndMs(): Long = (this as? LyricItem.NormalLyric)?.translation?.toLongOrNull() ?: time

private fun LyricItem.knownEndMs(): Long? =
    (this as? LyricItem.WordsLyric)?.let { words ->
        max(words.endTime, words.accompaniment.maxOfOrNull { it.endTime } ?: words.endTime)
    }

private fun interludeItem(
    startMs: Long,
    endMs: Long,
    index: Int,
): LyricItem =
    LyricItem.NormalLyric(
        content = "",
        translation = endMs.toString(),
        time = startMs,
        key = "$INTERLUDE_KEY_PREFIX$index",
    )

/** Inserts break lines into a time-sorted lyric list. Only used for time-synced lyrics. */
internal fun insertInterludes(lines: List<LyricItem>): List<LyricItem> {
    if (lines.isEmpty()) return lines
    val result = ArrayList<LyricItem>(lines.size + 4)
    val first = lines.first()
    if (first.time >= INTRO_MIN_MS) {
        result += interludeItem(0L, first.time - INTERLUDE_LEAD_MS, result.size)
    }
    lines.forEachIndexed { i, line ->
        result += line
        val next = lines.getOrNull(i + 1) ?: return@forEachIndexed
        val end = line.knownEndMs() ?: return@forEachIndexed
        if (next.time - end >= BREAK_MIN_MS) {
            result += interludeItem(end, next.time - INTERLUDE_LEAD_MS, result.size)
        }
    }
    return result
}

/**
 * Draws a break line. The dots fill one by one as the break goes by and breathe while it is current;
 * the number counts down the whole seconds until the next line, and rolls up into place on every change.
 */
@Composable
internal fun InterludeLine(
    startMs: Long,
    endMs: Long,
    currentTimeState: State<Long>,
    isActive: Boolean,
    alphaProvider: () -> Float,
    scaleProvider: () -> Float,
    blurRadius: Dp,
    horizontalPadding: Dp,
    verticalPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val color = MaterialTheme.colorScheme.onSurface
    val blurEffect =
        remember(blurRadius, density) {
            val px = with(density) { blurRadius.toPx() }
            if (px > 0f) BlurEffect(px, px, TileMode.Decal) else null
        }

    val nextStartMs = endMs + INTERLUDE_LEAD_MS
    val fullSeconds = max(1, ceil((nextStartMs - startMs) / 1000.0).toInt())
    // Whole seconds until the next line; a finished or waiting break shows its full length again.
    val count by remember(startMs, endMs, isActive) {
        derivedStateOf {
            if (isActive) {
                max(1, ceil((nextStartMs - currentTimeState.value) / 1000.0).toInt()).coerceAtMost(fullSeconds)
            } else {
                fullSeconds
            }
        }
    }

    // The dots breathe one after another while nobody sings (scale 1 -> 1.32, 2.4 s loop, 0.26 s apart).
    val breath = rememberInfiniteTransition(label = "interludeBreath")
    val scales =
        List(3) { i ->
            breath.animateFloat(
                initialValue = 1f,
                targetValue = 1.32f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse,
                        initialStartOffset = StartOffset(i * 260),
                    ),
                label = "interludeDot$i",
            )
        }

    val dot = 14.dp
    val dotGap = 9.dp
    val rollOffsetPx = with(density) { 9.dp.roundToPx() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .graphicsLayer {
                    this.alpha = alphaProvider()
                    val scale = scaleProvider()
                    scaleX = scale
                    scaleY = scale
                    transformOrigin =
                        TransformOrigin(
                            pivotFractionX = if (size.width > 0f) horizontalPadding.toPx() / size.width else 0f,
                            pivotFractionY = 0.5f,
                        )
                    renderEffect = blurEffect
                }.padding(horizontal = horizontalPadding, vertical = verticalPadding),
    ) {
        Canvas(modifier = Modifier.size(width = dot * 3 + dotGap * 2, height = dot)) {
            val diameter = dot.toPx()
            val radius = diameter / 2f
            val step = diameter + dotGap.toPx()
            // How far through the break we are (0..1); the dots light up one third at a time.
            val progress =
                if (isActive) {
                    ((currentTimeState.value - startMs).toFloat() / max(100L, endMs - startMs).toFloat())
                        .coerceIn(0f, 1f)
                } else {
                    0f
                }
            for (i in 0..2) {
                val lit = (progress * 3f - i).coerceIn(0f, 1f)
                val dotScale = if (isActive) scales[i].value else 1f
                drawCircle(
                    color = color.copy(alpha = 0.28f + 0.72f * lit),
                    radius = radius * dotScale,
                    center = Offset(radius + i * step, size.height / 2f),
                )
            }
        }
        Spacer(modifier = Modifier.width(dotGap + 7.dp))
        AnimatedContent(
            targetState = count,
            transitionSpec = {
                (
                    fadeIn(tween(600, easing = InterludeEase)) +
                        slideInVertically(tween(600, easing = InterludeEase)) { rollOffsetPx } +
                        scaleIn(tween(600, easing = InterludeEase), initialScale = 0.9f)
                ) togetherWith
                    (
                        fadeOut(tween(450, easing = InterludeEase)) +
                            slideOutVertically(tween(450, easing = InterludeEase)) { -rollOffsetPx } +
                            scaleOut(tween(450, easing = InterludeEase), targetScale = 0.9f)
                    ) using SizeTransform(clip = false)
            },
            contentAlignment = Alignment.CenterStart,
            label = "interludeCount",
            modifier = Modifier.widthIn(min = 26.dp),
        ) { seconds ->
            Text(
                text = seconds.toString(),
                color = color,
                style =
                    TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFeatureSettings = "tnum",
                    ),
            )
        }
    }
}
