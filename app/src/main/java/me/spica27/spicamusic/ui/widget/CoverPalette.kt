package me.spica27.spicamusic.ui.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

// ---------------------------------------------------------------------------------------------
// Cover palette: k-means colours of the album art, used by the Smooth background and player text.
// ---------------------------------------------------------------------------------------------

internal class Cluster(
    val rgb: IntArray,
    val n: Int,
    val h: Float,
    val s: Float,
    val l: Float,
    val w: Float,
) {
    val score: Float = sqrt(w) * (.22f + s) * (1f - .6f * abs(l - .4f))
}

/** Opaque handle to the palette of one cover, computed once per cover. */
class CoverPalette internal constructor(
    internal val clusters: List<Cluster>,
)

private fun jsRound(v: Float): Int = kotlin.math.floor(v + .5f).toInt()

private fun rgb2hsl(r0: Int, g0: Int, b0: Int): FloatArray {
    val r = r0 / 255f
    val g = g0 / 255f
    val b = b0 / 255f
    val mx = max(r, max(g, b))
    val mn = min(r, min(g, b))
    var h = 0f
    var s = 0f
    val l = (mx + mn) / 2f
    val d = mx - mn
    if (d != 0f) {
        s = d / (1f - abs(2f * l - 1f))
        h =
            when (mx) {
                r -> ((g - b) / d) % 6f
                g -> (b - r) / d + 2f
                else -> (r - g) / d + 4f
            }
        h *= 60f
        if (h < 0f) h += 360f
    }
    return floatArrayOf(h, s, l)
}

/** k-means palette of the 44x44 cover pixels, sorted by "how good a background/accent this is". */
internal fun analyzePalette(px: List<IntArray>): CoverPalette? {
    if (px.size < 10) return null
    val k = 6

    fun dist(a: IntArray, b: IntArray): Float {
        val x = (a[0] - b[0]).toFloat()
        val y = (a[1] - b[1]).toFloat()
        val z = (a[2] - b[2]).toFloat()
        return x * x + y * y + z * z
    }

    val mf = FloatArray(3) { i -> px.sumOf { it[i].toDouble() }.toFloat() / px.size }
    fun dm(p: IntArray): Float {
        val x = p[0] - mf[0]
        val y = p[1] - mf[1]
        val z = p[2] - mf[2]
        return x * x + y * y + z * z
    }
    val cs = ArrayList<FloatArray>()
    val first = px.reduce { b, p -> if (dm(p) < dm(b)) p else b }
    cs.add(FloatArray(3) { first[it].toFloat() })
    fun distF(p: IntArray, c: FloatArray): Float {
        val x = p[0] - c[0]
        val y = p[1] - c[1]
        val z = p[2] - c[2]
        return x * x + y * y + z * z
    }
    while (cs.size < k) {
        var best: IntArray? = null
        var bd = -1f
        for (p in px) {
            var m = Float.MAX_VALUE
            for (q in cs) m = min(m, distF(p, q))
            if (m > bd) {
                bd = m
                best = p
            }
        }
        cs.add(FloatArray(3) { best!![it].toFloat() })
    }
    var cnt = IntArray(k)
    repeat(8) {
        val sum = Array(k) { FloatArray(3) }
        cnt = IntArray(k)
        for (p in px) {
            var bi = 0
            var bd = Float.MAX_VALUE
            cs.forEachIndexed { i, c ->
                val d = distF(p, c)
                if (d < bd) {
                    bd = d
                    bi = i
                }
            }
            cnt[bi]++
            sum[bi][0] += p[0]
            sum[bi][1] += p[1]
            sum[bi][2] += p[2]
        }
        cs.forEachIndexed { i, c ->
            if (cnt[i] > 0) {
                c[0] = sum[i][0] / cnt[i]
                c[1] = sum[i][1] / cnt[i]
                c[2] = sum[i][2] / cnt[i]
            }
        }
    }
    val total = px.size.toFloat()
    val cl =
        cs
            .mapIndexed { i, c -> IntArray(3) { jsRound(c[it]) } to cnt[i] }
            .filter { it.second > 0 }
            .map { (rgb, n) ->
                val hsl = rgb2hsl(rgb[0], rgb[1], rgb[2])
                Cluster(rgb, n, hsl[0], hsl[1], hsl[2], n / total)
            }.sortedByDescending { it.score }
    return CoverPalette(cl)
}

internal suspend fun extractCoverPalette(
    context: android.content.Context,
    uri: Uri?,
): CoverPalette? =
    withContext(Dispatchers.IO) {
        if (uri == null) return@withContext null
        try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (bounds.outWidth / (sample * 2) >= 88 && bounds.outHeight / (sample * 2) >= 88) sample *= 2
            val opts = BitmapFactory.Options().apply { inSampleSize = sample }
            val src =
                context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
                    ?: return@withContext null
            // 44x44 sampling (alpha > 200 only)
            val n = 44
            val scaled = Bitmap.createScaledBitmap(src, n, n, true)
            val pixels = IntArray(n * n)
            scaled.getPixels(pixels, 0, n, 0, 0, n, n)
            if (scaled !== src) scaled.recycle()
            src.recycle()
            val px = ArrayList<IntArray>(pixels.size)
            for (p in pixels) {
                if (((p ushr 24) and 0xFF) > 200) px.add(intArrayOf((p shr 16) and 0xFF, (p shr 8) and 0xFF, p and 0xFF))
            }
            analyzePalette(px)
        } catch (e: Exception) {
            null
        }
    }
