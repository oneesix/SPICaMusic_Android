@file:Suppress("ktlint:standard:package-name")

package me.spica27.spicamusic.ui.widget.audio_seekbar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.max

private const val RING_SAMPLES = 240

internal class PillRingHit(
    val progress: Float,
    val distance: Float,
)

/**
 * Open ring that follows the bottom of the album: starts on the left side, goes round the
 * bottom corners and ends on the right side. All values are pixels; the album's top-left
 * corner is at ([originX], 0). [pad] may be negative (ring inside the album).
 */
internal class PillRingGeometry(
    originX: Float,
    albumWidth: Float,
    albumHeight: Float,
    radius: Float,
    pad: Float,
) {
    val path = Path()
    private val measure = PathMeasure()
    val length: Float
    private val xs = FloatArray(RING_SAMPLES + 1)
    private val ys = FloatArray(RING_SAMPLES + 1)

    init {
        val r = max(1f, radius + pad)
        val left = originX - pad
        val right = originX + albumWidth + pad
        val cornerY = albumHeight - radius
        val bottom = albumHeight + pad
        path.moveTo(left, cornerY)
        path.arcTo(Rect(left, bottom - 2 * r, left + 2 * r, bottom), 180f, -90f, false)
        path.lineTo(originX + albumWidth - radius, bottom)
        path.arcTo(Rect(right - 2 * r, bottom - 2 * r, right, bottom), 90f, -90f, false)
        measure.setPath(path, false)
        length = measure.length
        for (i in 0..RING_SAMPLES) {
            val o = measure.getPosition(length * i / RING_SAMPLES)
            xs[i] = o.x
            ys[i] = o.y
        }
    }

    fun position(distance: Float): Offset = measure.getPosition(distance.coerceIn(0f, length))

    fun tangent(distance: Float): Offset = measure.getTangent(distance.coerceIn(0f, length))

    fun segment(
        start: Float,
        stop: Float,
        destination: Path,
    ) {
        measure.getSegment(start, stop, destination, true)
    }

    fun nearest(point: Offset): PillRingHit {
        var best = 0
        var bestDistance = Float.MAX_VALUE
        for (i in 0..RING_SAMPLES) {
            val dx = xs[i] - point.x
            val dy = ys[i] - point.y
            val d = dx * dx + dy * dy
            if (d < bestDistance) {
                bestDistance = d
                best = i
            }
        }
        return PillRingHit(best.toFloat() / RING_SAMPLES, kotlin.math.sqrt(bestDistance))
    }
}

/**
 * Seeks by touching or dragging near the ring. Touches farther than [slopPx] from the ring are
 * ignored, so the album tap and swipe keep working.
 */
@Composable
internal fun Modifier.pillRingSeek(
    geometry: PillRingGeometry,
    slopPx: Float,
    onChange: (Float) -> Unit,
    onFinished: () -> Unit,
): Modifier {
    val change by rememberUpdatedState(onChange)
    val finished by rememberUpdatedState(onFinished)
    return pointerInput(geometry, slopPx) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val hit = geometry.nearest(down.position)
            if (hit.distance > slopPx) return@awaitEachGesture
            down.consume()
            change(hit.progress)
            drag(down.id) { pointer ->
                change(geometry.nearest(pointer.position).progress)
                pointer.consume()
            }
            finished()
        }
    }
}

/** Ring progress bar with the same glow and particle trail as the glow-particles seek bar. */
@Composable
internal fun PillRingSeekBar(
    geometry: PillRingGeometry,
    progress: Float,
    animationsEnabled: Boolean,
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
    progressColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    val time = remember { mutableLongStateOf(0L) }
    LaunchedEffect(animationsEnabled) {
        if (!animationsEnabled) return@LaunchedEffect
        var previous = 0L
        while (isActive) {
            withFrameNanos { frame ->
                if (previous != 0L) time.longValue += frame - previous
                previous = frame
            }
        }
    }
    val particles = remember { GlowParticleSystem() }
    val value = if (progress.isFinite()) progress.coerceIn(0f, 1f) else 0f

    Canvas(modifier) {
        val length = geometry.length
        if (length <= 0f) return@Canvas
        val stroke = 5.dp.toPx()
        val played = length * value
        drawPath(geometry.path, trackColor, style = Stroke(width = stroke, cap = StrokeCap.Round))
        if (played > 0f) {
            val segment = Path()
            geometry.segment(0f, played, segment)
            drawPath(segment, progressColor, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }

        val head = geometry.position(played)
        particles.update(
            timeNanos = time.longValue,
            emitterX = played / density,
            trackWidth = length / density,
            emissionDirection = 1f,
            emitting = animationsEnabled,
        )
        val haloRadius = 26.dp.toPx()
        drawCircle(
            brush =
                Brush.radialGradient(
                    0f to lerp(glowColor, Color.White, 0.35f).copy(alpha = 0.55f),
                    0.4f to glowColor.copy(alpha = 0.25f),
                    1f to glowColor.copy(alpha = 0f),
                    center = head,
                    radius = haloRadius,
                ),
            radius = haloRadius,
            center = head,
        )
        particles.particles.forEach { particle ->
            if (!particle.alive) return@forEach
            val distance = (particle.x * density).coerceIn(0f, length)
            val base = geometry.position(distance)
            val tangent = geometry.tangent(distance)
            val offset = particle.y * density
            val point = Offset(base.x - tangent.y * offset, base.y + tangent.x * offset)
            val radius = particle.radius * density
            drawCircle(glowColor, radius * 2.2f, point, alpha = particle.opacity * 0.14f)
            drawCircle(lerp(glowColor, Color.White, 0.75f), radius, point, alpha = particle.opacity * 0.7f)
        }
        drawCircle(Color.White, radius = stroke * 0.7f, center = head)
    }
}
