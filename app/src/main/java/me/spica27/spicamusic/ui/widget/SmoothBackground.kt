package me.spica27.spicamusic.ui.widget

import android.graphics.RuntimeShader
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Constraints
import org.intellij.lang.annotations.Language
import kotlin.math.abs
import kotlin.math.cbrt
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * "Smooth" player background (a port of the ColorfulX gradient): up to 8 colours taken from the
 * album art are blended by distance in LAB colour space; the colour points drift around.
 * All values are stored as Ints (see the *Tenths / *Milli fields) so they fit the settings store.
 */
data class SmoothSettings(
    /** Player text / icons / sliders take their colours from the album art. */
    val colorUi: Boolean = true,
    /** true = the background stops moving (no per-frame work). */
    val freeze: Boolean = false,
    /** 0..30 -> 0.0..3.0 (ColorfulX speed, 0 = still). */
    val speedTenths: Int = 10,
    /** 2..200 -> 0.002..0.200 (ColorfulX bias: higher = softer). */
    val biasMilli: Int = 10,
    /** 0..30 pixels of grain (ColorfulX noise). */
    val noise: Int = 8,
    /** 5..80 -> 0.5..8.0 (how fast the colours change after a song change). */
    val transitionTenths: Int = 35,
    /** 10..80 -> 1.0..8.0 (ColorfulX blend power, 4 = default). */
    val powerTenths: Int = 40,
    /** 15..120 frames per second. */
    val frameLimit: Int = 60,
    /** 25..100 % of the screen resolution the gradient is drawn at. */
    val renderScalePercent: Int = 50,
    /** 0..70 % black on top, keeps text readable. */
    val dimPercent: Int = 20,
    /** Auto / light / dark look: pastel background + dark text, or deep background + light text (when [colorUi] is on). */
    val themeMode: String = SmoothThemeMode.AUTO,
    /** 0..200 % colour saturation of the album colours. */
    val saturationPercent: Int = 100,
    /** 0..100 % how strongly the text takes the album colour (0 = plain white / dark text). */
    val textTintPercent: Int = 100,
    /** 0..70 % extra black over the background on the lyrics screen, keeps lyrics readable. */
    val lyricsDimPercent: Int = 15,
)

private const val SLOTS = 8

/** Colours of one cover: LAB values (flattened, SLOTS * 3), the number of colours used, plus sRGB for the fallback. */
internal class SmoothPalette(
    val lab: FloatArray,
    val count: Int,
    val rgb: List<Color>,
)

private class SmoothState {
    val pos = FloatArray(SLOTS * 2) { Random.nextFloat() }
    val tgt = FloatArray(SLOTS * 2) { Random.nextFloat() }
    val cur = FloatArray(SLOTS * 3)
    var snapped = false
}

private fun srgbToLinear(v: Float): Float = if (v > 0.04045f) ((v + 0.055f) / 1.055f).pow(2.4f) else v / 12.92f

internal fun rgbToLab(r255: Float, g255: Float, b255: Float): FloatArray {
    val r = srgbToLinear(r255 / 255f)
    val g = srgbToLinear(g255 / 255f)
    val b = srgbToLinear(b255 / 255f)
    val x = (r * 0.4124f + g * 0.3576f + b * 0.1805f) / 0.95047f
    val y = r * 0.2126f + g * 0.7152f + b * 0.0722f
    val z = (r * 0.0193f + g * 0.1192f + b * 0.9505f) / 1.08883f
    fun f(t: Float) = if (t > 0.008856f) cbrt(t) else 7.787f * t + 16f / 116f
    return floatArrayOf(116f * f(y) - 16f, 500f * (f(x) - f(y)), 200f * (f(y) - f(z)))
}

private val DefaultSmoothColors =
    listOf(
        intArrayOf(125, 20, 8),
        intArrayOf(143, 26, 9),
        intArrayOf(255, 170, 60),
        intArrayOf(74, 11, 4),
    )

internal fun buildSmoothPalette(
    palette: CoverPalette?,
    saturation: Float,
    dark: Boolean,
): SmoothPalette {
    val source = palette?.clusters?.take(6)?.map { it.rgb } ?: DefaultSmoothColors
    val colors = if (source.size >= 4) source else List(4) { source[it % source.size] }
    val lab = FloatArray(SLOTS * 3)
    val rgb = ArrayList<Color>()
    colors.forEachIndexed { i, c ->
        // saturation: push the colour away from (or towards) its own grey
        val gray = 0.299f * c[0] + 0.587f * c[1] + 0.114f * c[2]
        val r = (gray + (c[0] - gray) * saturation).coerceIn(0f, 255f)
        val g = (gray + (c[1] - gray) * saturation).coerceIn(0f, 255f)
        val b = (gray + (c[2] - gray) * saturation).coerceIn(0f, 255f)
        // light / dark look: same hue, lightness moved into a light or a dark band
        val l = toneLab(rgbToLab(r, g, b), dark)
        lab[i * 3] = l[0]
        lab[i * 3 + 1] = l[1]
        lab[i * 3 + 2] = l[2]
        val toned = labToRgb(l[0], l[1], l[2])
        rgb.add(Color(toned[0].roundToInt().coerceIn(0, 255), toned[1].roundToInt().coerceIn(0, 255), toned[2].roundToInt().coerceIn(0, 255)))
    }
    return SmoothPalette(lab, colors.size.coerceAtMost(SLOTS), rgb)
}

private fun snapColors(
    s: SmoothState,
    p: SmoothPalette,
) {
    for (i in 0 until SLOTS * 3) s.cur[i] = p.lab[i]
    s.snapped = true
}

private fun stepState(
    s: SmoothState,
    p: SmoothPalette,
    dt: Float,
    speed: Float,
    transition: Float,
) {
    // colours ease towards the new palette (after a song change)
    val k = 1f - exp(-dt * transition)
    for (i in 0 until SLOTS * 3) s.cur[i] += (p.lab[i] - s.cur[i]) * k
    // points drift towards random targets; a new target is picked when one is close (as in ColorfulX)
    val m = 1f - exp(-dt * speed * 0.5f)
    for (i in 0 until p.count) {
        val x = i * 2
        val y = x + 1
        s.pos[x] += (s.tgt[x] - s.pos[x]) * m
        s.pos[y] += (s.tgt[y] - s.pos[y]) * m
        if (abs(s.pos[x] - s.tgt[x]) < 0.125f || abs(s.pos[y] - s.tgt[y]) < 0.125f) {
            s.tgt[x] = Random.nextFloat()
            s.tgt[y] = Random.nextFloat()
        }
    }
}

@Language("AGSL")
private const val SMOOTH_SHADER =
    """
    uniform float2 resolution;
    uniform float bias;
    uniform float noise;
    uniform float power;
    uniform float count;
    uniform float2 pts[8];
    uniform float3 cols[8];

    float2 hash23(float3 p3) {
        p3 = fract(p3 * float3(443.897, 441.423, 0.0973));
        p3 += dot(p3, p3.yzx + 19.19);
        return fract((p3.xx + p3.yz) * p3.zy);
    }

    float labInv(float t, float f) {
        return t > 0.008856 ? t : (f - 16.0 / 116.0) / 7.787;
    }

    float gamma(float c) {
        c = clamp(c, 0.0, 1.0);
        return c <= 0.0031308 ? 12.92 * c : 1.055 * pow(c, 1.0 / 2.4) - 0.055;
    }

    float3 lab2rgb(float3 lab) {
        float y = (lab.x + 16.0) / 116.0;
        float x = lab.y / 500.0 + y;
        float z = y - lab.z / 200.0;
        float X = labInv(x * x * x, x) * 0.95047;
        float Y = labInv(y * y * y, y);
        float Z = labInv(z * z * z, z) * 1.08883;
        float r = X * 3.2406 + Y * -1.5372 + Z * -0.4986;
        float g = X * -0.9689 + Y * 1.8758 + Z * 0.0415;
        float b = X * 0.0557 + Y * -0.2040 + Z * 1.0570;
        return float3(gamma(r), gamma(g), gamma(b));
    }

    half4 main(float2 fragCoord) {
        float2 n = hash23(float3(fragCoord / float2(resolution.x, resolution.x), 0.0));
        float2 uv = (fragCoord + sin(n * 6.2831853) * noise) / resolution.x;
        float aspect = resolution.y / resolution.x;
        float total = 0.0;
        for (int i = 0; i < 8; i++) {
            if (float(i) < count) {
                float d = length(uv - pts[i] * float2(1.0, aspect));
                total += 1.0 / (bias + pow(d, power));
            }
        }
        float3 lab = float3(0.0);
        for (int j = 0; j < 8; j++) {
            if (float(j) < count) {
                float d = length(uv - pts[j] * float2(1.0, aspect));
                float c = 1.0 / (bias + pow(d, power));
                lab += cols[j] * (c / total);
            }
        }
        return half4(half3(lab2rgb(lab)), 1.0);
    }
    """

/**
 * Smooth background. [dark] picks the look: a deep gradient (dark) or a pastel gradient (light).
 * [enabled] = false (app in background / animations off) behaves like [SmoothSettings.freeze].
 * Needs Android 13+ (AGSL); older versions get a static album-colour gradient.
 */
@Composable
fun SmoothBackground(
    coverUri: Uri?,
    settings: SmoothSettings,
    enabled: Boolean,
    dark: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var coverPalette by remember(coverUri) { mutableStateOf<CoverPalette?>(null) }
    LaunchedEffect(coverUri) { coverPalette = extractCoverPalette(context, coverUri) }
    val palette =
        remember(coverPalette, settings.saturationPercent, dark) {
            buildSmoothPalette(coverPalette, settings.saturationPercent / 100f, dark)
        }
    val dim = settings.dimPercent.coerceIn(0, 70) / 100f

    Box(modifier = modifier) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            SmoothShaderLayer(
                palette = palette,
                settings = settings,
                enabled = enabled,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            val colors = palette.rgb.take(3).let { if (it.size < 2) it + it else it }
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors)))
        }
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim)))
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun SmoothShaderLayer(
    palette: SmoothPalette,
    settings: SmoothSettings,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val state = remember { SmoothState() }
    var tick by remember { mutableStateOf(0) }
    val cfg by rememberUpdatedState(settings)
    val pal by rememberUpdatedState(palette)
    val running = enabled && !settings.freeze

    // First frame, frozen, or paused: show the new colours at once (no animation).
    LaunchedEffect(palette, running) {
        if (!state.snapped || !running) {
            snapColors(state, palette)
            tick++
        }
    }

    // Animation loop; it is not running at all while frozen.
    LaunchedEffect(running, settings.frameLimit) {
        if (!running) return@LaunchedEffect
        val minDelta = 1_000_000_000L / settings.frameLimit.coerceAtLeast(1)
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last == 0L) {
                    last = now
                } else if (now - last >= minDelta) {
                    val dt = ((now - last) / 1_000_000_000f).coerceAtMost(0.1f)
                    last = now
                    stepState(state, pal, dt, cfg.speedTenths / 10f, cfg.transitionTenths / 10f)
                    tick++
                }
            }
        }
    }

    val shader = remember { RuntimeShader(SMOOTH_SHADER) }
    val brush = remember(shader) { ShaderBrush(shader) }
    val scale = (settings.renderScalePercent / 100f).coerceIn(0.2f, 1f)
    val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()

    Box(
        modifier =
            modifier
                // Draw the gradient at a lower resolution and scale it up (render scale).
                .layout { measurable, constraints ->
                    val w = max(1, (constraints.maxWidth * scale).roundToInt())
                    val h = max(1, (constraints.maxHeight * scale).roundToInt())
                    val placeable = measurable.measure(Constraints.fixed(w, h))
                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.placeWithLayer(0, 0) {
                            scaleX = constraints.maxWidth.toFloat() / w
                            scaleY = constraints.maxHeight.toFloat() / h
                            transformOrigin = TransformOrigin(0f, 0f)
                            compositingStrategy = CompositingStrategy.Offscreen
                        }
                    }
                }.drawBehind {
                    @Suppress("UNUSED_VARIABLE")
                    val redraw = tick // read so every step redraws
                    val p = pal
                    val c = cfg
                    shader.setFloatUniform("resolution", size.width, size.height)
                    shader.setFloatUniform("bias", c.biasMilli / 1000f)
                    shader.setFloatUniform("noise", c.noise * (size.width / max(1f, screenWidth)))
                    shader.setFloatUniform("power", c.powerTenths / 10f)
                    shader.setFloatUniform("count", p.count.toFloat())
                    shader.setFloatUniform("pts", state.pos.copyOf())
                    shader.setFloatUniform("cols", state.cur.copyOf())
                    drawRect(brush)
                },
    )
}
