package me.spica27.spicamusic.automix

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import java.io.File
import java.io.IOException
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Stereo audio at [StemAudio.SAMPLE_RATE]. */
class Pcm(
    val left: FloatArray,
    val right: FloatArray,
) {
    val size: Int get() = min(left.size, right.size)
}

/** Decoding, resampling and time-stretching of stem files. */
object StemAudio {
    const val SAMPLE_RATE = 44100

    private const val PRE_ROLL_SEC = 1.0
    private const val TIMEOUT_US = 10_000L
    private const val MAX_IDLE_LOOPS = 3000

    // WSOLA time stretch
    private const val FRAME = 2048
    private const val HOP = FRAME / 2
    private const val DECIM = 4
    private const val SEARCH = 512 // +/- samples around the planned position (about 11 ms)
    private const val PULL = 0.15

    // ───────────────────────── Decoding ─────────────────────────

    private class FloatBuf {
        var data = FloatArray(1 shl 16)
        var size = 0

        fun add(v: Float) {
            if (size == data.size) data = data.copyOf(size * 2)
            data[size++] = v
        }
    }

    /** Decode the part of [file] between [startSec] and [endSec] (Opus, FLAC or any format the phone can decode). */
    fun decode(
        file: File,
        startSec: Double,
        endSec: Double,
    ): Pcm {
        val extractor = MediaExtractor()
        var codec: MediaCodec? = null
        try {
            extractor.setDataSource(file.absolutePath)
            var track = -1
            var format: MediaFormat? = null
            for (i in 0 until extractor.trackCount) {
                val f = extractor.getTrackFormat(i)
                if ((f.getString(MediaFormat.KEY_MIME) ?: "").startsWith("audio/")) {
                    track = i
                    format = f
                    break
                }
            }
            if (track < 0 || format == null) throw IOException("No audio track in ${file.name}")
            extractor.selectTrack(track)
            extractor.seekTo((max(0.0, startSec - PRE_ROLL_SEC) * 1_000_000).toLong(), MediaExtractor.SEEK_TO_PREVIOUS_SYNC)

            val decoder = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            codec = decoder
            decoder.configure(format, null, null, 0)
            decoder.start()

            var sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            var channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            var floatOutput = false
            val left = FloatBuf()
            val right = FloatBuf()
            val info = MediaCodec.BufferInfo()
            var firstPtsUs = -1L
            var inputDone = false
            var outputDone = false
            var idle = 0

            while (!outputDone) {
                var progressed = false
                if (!inputDone) {
                    val inIndex = decoder.dequeueInputBuffer(TIMEOUT_US)
                    if (inIndex >= 0) {
                        val buffer = decoder.getInputBuffer(inIndex)!!
                        val size = extractor.readSampleData(buffer, 0)
                        if (size < 0) {
                            decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                            inputDone = true
                        } else {
                            decoder.queueInputBuffer(inIndex, 0, size, extractor.sampleTime, 0)
                            extractor.advance()
                        }
                        progressed = true
                    }
                }
                val outIndex = decoder.dequeueOutputBuffer(info, TIMEOUT_US)
                if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    val out = decoder.outputFormat
                    sampleRate = out.getInteger(MediaFormat.KEY_SAMPLE_RATE)
                    channels = out.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
                    floatOutput =
                        out.containsKey(MediaFormat.KEY_PCM_ENCODING) &&
                        out.getInteger(MediaFormat.KEY_PCM_ENCODING) == android.media.AudioFormat.ENCODING_PCM_FLOAT
                    progressed = true
                } else if (outIndex >= 0) {
                    if (info.size > 0) {
                        if (firstPtsUs < 0) firstPtsUs = info.presentationTimeUs
                        val buffer = decoder.getOutputBuffer(outIndex)!!
                        buffer.order(ByteOrder.nativeOrder())
                        buffer.position(info.offset)
                        buffer.limit(info.offset + info.size)
                        if (floatOutput) {
                            val fb = buffer.asFloatBuffer()
                            while (fb.remaining() >= channels) {
                                val l = fb.get()
                                val r = if (channels > 1) fb.get() else l
                                for (c in 2 until channels) fb.get()
                                left.add(l)
                                right.add(r)
                            }
                        } else {
                            val sb = buffer.asShortBuffer()
                            while (sb.remaining() >= channels) {
                                val l = sb.get() / 32768f
                                val r = if (channels > 1) sb.get() / 32768f else l
                                for (c in 2 until channels) sb.get()
                                left.add(l)
                                right.add(r)
                            }
                        }
                    }
                    decoder.releaseOutputBuffer(outIndex, false)
                    if (info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) outputDone = true
                    if (firstPtsUs >= 0 && firstPtsUs / 1_000_000.0 + left.size.toDouble() / sampleRate >= endSec + 0.1) outputDone = true
                    progressed = true
                }
                if (progressed) idle = 0 else if (++idle > MAX_IDLE_LOOPS) throw IOException("Decoder stalled on ${file.name}")
            }
            if (firstPtsUs < 0) throw IOException("No audio decoded from ${file.name}")

            // Cut exactly [startSec, endSec], using the time stamp of the first decoded sample.
            val firstSec = firstPtsUs / 1_000_000.0
            val from = ((startSec - firstSec) * sampleRate).roundToInt()
            val to = ((endSec - firstSec) * sampleRate).roundToInt()
            val l = slice(left.data, left.size, from, to)
            val r = slice(right.data, right.size, from, to)
            return Pcm(resample(l, sampleRate), resample(r, sampleRate))
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            extractor.release()
        }
    }

    /** data[from until to], with zeros where the range is outside the decoded audio. */
    private fun slice(
        data: FloatArray,
        size: Int,
        from: Int,
        to: Int,
    ): FloatArray {
        val out = FloatArray(max(0, to - from))
        for (i in out.indices) {
            val src = from + i
            if (src in 0 until size) out[i] = data[src]
        }
        return out
    }

    /** Linear resampling to [SAMPLE_RATE] (only needed when a file is not 44.1 kHz). */
    fun resample(
        x: FloatArray,
        fromRate: Int,
    ): FloatArray {
        if (fromRate == SAMPLE_RATE || x.isEmpty()) return x
        val n = (x.size.toLong() * SAMPLE_RATE / fromRate).toInt()
        val out = FloatArray(n)
        val step = fromRate.toDouble() / SAMPLE_RATE
        for (i in 0 until n) {
            val p = i * step
            val i0 = p.toInt()
            val f = (p - i0).toFloat()
            val a = x[min(i0, x.size - 1)]
            val b = x[min(i0 + 1, x.size - 1)]
            out[i] = a + (b - a) * f
        }
        return out
    }

    // ───────────────────────── Time stretch (WSOLA) ─────────────────────────

    private val window: FloatArray =
        FloatArray(FRAME) { (0.5 - 0.5 * cos(2.0 * PI * it / FRAME)).toFloat() } // periodic Hann: overlaps add up to 1

    /**
     * Where each output frame reads from the source. All stems of a song use the same positions
     * (found on their sum), so drums, bass and the rest stay together.
     *
     * @param guide the sum of the stems, mono, every [DECIM]th sample averaged
     * @param srcLength length of the stems in samples
     * @param frames number of output frames
     * @param planned planned source position (samples) for the frame that starts at output sample `k * HOP - HOP`
     */
    fun frameStarts(
        guide: FloatArray,
        srcLength: Int,
        frames: Int,
        planned: (Int) -> Int,
    ): IntArray {
        val starts = IntArray(frames)
        val fd = FRAME / DECIM
        val maxStart = srcLength - FRAME
        for (k in 0 until frames) {
            val p = planned(k).coerceIn(0, maxStart)
            if (k == 0) {
                starts[k] = p
                continue
            }
            val ref = ((starts[k - 1] + HOP) / DECIM).coerceIn(0, guide.size - fd)
            val lo = ((p - SEARCH) / DECIM).coerceIn(0, guide.size - fd)
            val hi = ((p + SEARCH) / DECIM).coerceIn(0, guide.size - fd)
            var refEnergy = 1e-9
            for (j in 0 until fd) refEnergy += guide[ref + j] * guide[ref + j]
            val refNorm = sqrt(refEnergy)
            var best = p
            var bestScore = -Double.MAX_VALUE
            for (c in lo..hi) {
                var dot = 0.0
                var energy = 1e-9
                for (j in 0 until fd) {
                    val g = guide[c + j]
                    dot += guide[ref + j] * g
                    energy += g * g
                }
                // Normalised correlation, minus a small pull towards the planned position so the
                // beats stay on the grid (without it the stretch can drift by about 12 ms).
                val score = dot / (sqrt(energy) * refNorm) - PULL * abs(c * DECIM - p) / SEARCH
                if (score > bestScore) {
                    bestScore = score
                    best = c * DECIM
                }
            }
            starts[k] = best.coerceIn(0, maxStart)
        }
        return starts
    }

    /** Number of output frames needed for [outLength] samples. */
    fun frameCount(outLength: Int): Int = (outLength + HOP - 1) / HOP + 2

    /** Mono guide for [frameStarts]: the sum of all stems, [DECIM] samples averaged into one. */
    fun makeGuide(
        stems: List<Pcm>,
        length: Int,
    ): FloatArray {
        val guide = FloatArray(length / DECIM)
        for (i in guide.indices) {
            var s = 0f
            for (stem in stems) {
                for (j in 0 until DECIM) s += stem.left[i * DECIM + j] + stem.right[i * DECIM + j]
            }
            guide[i] = s / (2 * DECIM)
        }
        return guide
    }

    /**
     * Overlap-add one stem with the given frame positions. The result is shifted so that
     * element 0 is the first output sample (the first frame starts one hop before it).
     */
    fun stretch(
        src: FloatArray,
        starts: IntArray,
        outLength: Int,
    ): FloatArray {
        val acc = FloatArray(starts.size * HOP + FRAME)
        for (k in starts.indices) {
            val q = starts[k]
            val base = k * HOP
            for (i in 0 until FRAME) acc[base + i] += window[i] * src[q + i]
        }
        return acc.copyOfRange(HOP, HOP + outLength)
    }

    /** Smallest source length (in samples) the stretch needs to read safely. */
    const val FRAME_SAMPLES = FRAME
    const val HOP_SAMPLES = HOP
}
