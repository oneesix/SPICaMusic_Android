package me.spica27.spicamusic.automix

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.math.tanh

/** Builds the transition clip of a [MixPlan] from the stems of the two songs. */
object StemClipRenderer {
    private const val SR = StemAudio.SAMPLE_RATE
    private const val PAD_SEC = 0.25
    private const val TAIL_SEC = 0.5

    /**
     * Writes the clip (16-bit WAV) to [outFile] and returns the transition for the player.
     * [onStep] is called between the heavy steps so a cancelled job stops early.
     */
    fun render(
        plan: MixPlan,
        songA: StemSongData,
        songB: StemSongData,
        outFile: File,
        onStep: () -> Unit = {},
    ): AutoMixTransition {
        val outLength = ceil(plan.clipSec * SR).toInt()
        val outL = FloatArray(outLength)
        val outR = FloatArray(outLength)

        // Song A: its four stems from the blend start to the cue, with the fade curves.
        for (name in StemSongData.STEM_NAMES) {
            onStep()
            val pcm = StemAudio.decode(songA.stemFiles.getValue(name), plan.startA, plan.cueA)
            val n = min(pcm.size, outLength)
            for (i in 0 until n) {
                val g = plan.gainA(name, i / SR.toDouble())
                if (g == 0f) continue
                outL[i] += pcm.left[i] * g
                outR[i] += pcm.right[i] * g
            }
        }

        // Song B: stretched so its beats match A, then faded in with the curves.
        onStep()
        val t0 = max(0.0, plan.startB - PAD_SEC)
        val padSamples = ((plan.startB - t0) * SR).roundToInt()
        val srcEnd = plan.startB + plan.sourceOffset(plan.clipSec) + TAIL_SEC
        val stems = StemSongData.STEM_NAMES.map { StemAudio.decode(songB.stemFiles.getValue(it), t0, srcEnd) }
        val srcLength = stems.minOf { it.size }
        check(srcLength > StemAudio.FRAME_SAMPLES * 4) { "Song B is too short to blend" }
        onStep()
        val frames = StemAudio.frameCount(outLength)
        val guide = StemAudio.makeGuide(stems, srcLength)
        val starts =
            StemAudio.frameStarts(guide, srcLength, frames) { k ->
                val outTime = (k - 1) * StemAudio.HOP_SAMPLES / SR.toDouble() // frame k starts one hop before sample 0
                padSamples + (plan.sourceOffset(outTime) * SR).roundToInt()
            }
        for ((index, name) in StemSongData.STEM_NAMES.withIndex()) {
            onStep()
            val l = StemAudio.stretch(stems[index].left, starts, outLength)
            val r = StemAudio.stretch(stems[index].right, starts, outLength)
            for (i in 0 until outLength) {
                val g = plan.gainB(name, i / SR.toDouble())
                outL[i] += l[i] * g
                outR[i] += r[i] * g
            }
        }

        onStep()
        writeWav(outFile, outL, outR)
        return AutoMixTransition(
            fromMediaId = plan.fromMediaId,
            toMediaId = plan.toMediaId,
            clipPath = outFile.absolutePath,
            clipStartMs = (plan.startA * 1000).roundToLong(),
            resumeMs = (plan.resumeBSec * 1000).roundToLong(),
            clipDurationMs = (outLength * 1000.0 / SR).roundToLong(),
        )
    }

    /** Gentle limiter: nothing changes below 0.98, louder peaks are rounded off instead of clipping. */
    private fun limit(x: Float): Float {
        val a = abs(x)
        if (a <= 0.98f) return x
        val y = 0.98f + 0.02f * tanh((a - 0.98f) / 0.02f)
        return if (x < 0) -y else y
    }

    private fun writeWav(
        file: File,
        left: FloatArray,
        right: FloatArray,
    ) {
        file.parentFile?.mkdirs()
        val dataBytes = left.size * 4
        BufferedOutputStream(FileOutputStream(file), 1 shl 16).use { out ->
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
            header.put("RIFF".toByteArray()).putInt(36 + dataBytes).put("WAVE".toByteArray())
            header.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(2)
            header.putInt(SR).putInt(SR * 4).putShort(4).putShort(16)
            header.put("data".toByteArray()).putInt(dataBytes)
            out.write(header.array())
            val chunk = ByteBuffer.allocate(4096 * 4).order(ByteOrder.LITTLE_ENDIAN)
            var i = 0
            while (i < left.size) {
                chunk.clear()
                val end = min(left.size, i + 4096)
                while (i < end) {
                    chunk.putShort((limit(left[i]) * 32767f).roundToInt().coerceIn(-32768, 32767).toShort())
                    chunk.putShort((limit(right[i]) * 32767f).roundToInt().coerceIn(-32768, 32767).toShort())
                    i++
                }
                out.write(chunk.array(), 0, chunk.position())
            }
        }
    }
}
