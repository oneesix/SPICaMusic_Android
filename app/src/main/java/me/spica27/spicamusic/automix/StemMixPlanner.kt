package me.spica27.spicamusic.automix

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/** The mix point chosen in one song. */
class CuePick(
    /** The cue candidate (seconds), or the fallback target when no cue fit. */
    val candidateSec: Double,
    /** Index in the beat grid of the beat the cue snaps to. */
    val beatIndex: Int,
    val fromCue: Boolean,
)

/**
 * A planned blend of song A into song B.
 *
 * Song A plays from [startA] to [cueA] (the blend, [blendSec] long). Song B is time-stretched by [speedB]
 * (seconds of B per second of output) so its beats line up with A, and its cue [cueB] lands exactly on [cueA].
 * After the blend B keeps playing while its speed eases back to 1 over [releaseSec], and normal playback
 * resumes at [resumeBSec].
 */
class MixPlan(
    val fromMediaId: String,
    val toMediaId: String,
    val bpmA: Double,
    val bpmB: Double,
    val blendBeats: Int,
    val startA: Double,
    val cueA: Double,
    val startB: Double,
    val cueB: Double,
    val speedB: Double,
    val releaseSec: Double,
    /** Output time (seconds from the clip start) where A's vocals have faded out. */
    val vocalCutSec: Double,
    /** Output time where B's vocals come in. */
    val vocalInSec: Double,
    /** B's loudness relative to A in dB (applied during the blend, eased to 0 during the release). */
    val levelBDb: Double,
) {
    val blendSec: Double get() = cueA - startA
    val clipSec: Double get() = blendSec + releaseSec
    val resumeBSec: Double get() = cueB + releaseSec * (speedB + 1.0) / 2.0

    /** Seconds of B (counted from [startB]) that have been played [t] seconds into the clip. */
    fun sourceOffset(t: Double): Double {
        if (t <= 0.0) return speedB * t
        val d = blendSec
        if (t <= d) return speedB * t
        val r = releaseSec
        val u = min(t - d, r)
        // The speed goes linearly from speedB to 1 over r seconds.
        var offset = speedB * d + speedB * u + (1.0 - speedB) * u * u / (2.0 * r)
        if (t - d > r) offset += (t - d - r)
        return offset
    }

    private fun smooth(
        x: Double,
        a: Double,
        b: Double,
    ): Double {
        if (x <= a) return 0.0
        if (x >= b) return 1.0
        return 0.5 - 0.5 * cos(PI * (x - a) / (b - a))
    }

    /** Gain of one stem of song A, [t] seconds into the clip. */
    fun gainA(
        stem: String,
        t: Double,
    ): Float {
        val d = blendSec
        if (t >= d) return 0f
        val u = t / d
        val h = 0.5 * 60.0 / bpmA / d // half a beat, as a share of the blend
        val g =
            when (stem) {
                "drums" -> 1.0 - smooth(u, 0.50, 0.85)
                "bass" -> cos(PI / 2.0 * smooth(u, 0.5 - h, 0.5 + h))
                "other" -> cos(PI / 2.0 * smooth(u, 0.15, 0.90))
                else -> 1.0 - smooth(t, vocalCutSec - 0.25, vocalCutSec)
            }
        return g.toFloat()
    }

    /** Gain of one stem of song B, [t] seconds into the clip (includes the level match). */
    fun gainB(
        stem: String,
        t: Double,
    ): Float {
        val d = blendSec
        val u = t / d
        val h = 0.5 * 60.0 / bpmA / d
        val g =
            if (t >= d) {
                1.0
            } else {
                when (stem) {
                    "drums" -> smooth(u, 0.35, 0.65)
                    "bass" -> sin(PI / 2.0 * smooth(u, 0.5 - h, 0.5 + h))
                    "other" -> sin(PI / 2.0 * smooth(u, 0.15, 0.90))
                    else -> smooth(t, vocalInSec - 0.15, vocalInSec)
                }
            }
        val db =
            when {
                t <= d -> levelBDb
                t >= d + releaseSec -> 0.0
                else -> levelBDb * (1.0 - (t - d) / releaseSec)
            }
        return (g * 10.0.pow(db / 20.0)).toFloat()
    }
}

/**
 * Chooses the mix points, the tempo match and the stem curves for two songs.
 * The cue and tempo rules are the ones of the Python engine (cue_analysis.py); the manual mode only
 * changes the part of each song where cues are accepted.
 */
object StemMixPlanner {
    private const val RELEASE_SEC = 6.0
    private const val END_MARGIN_SEC = 1.0
    private const val MAX_LEVEL_DB = 6.0

    fun plan(
        from: StemSongData,
        to: StemSongData,
        fromMediaId: String,
        toMediaId: String,
        settings: AutoMixSettingsState,
    ): MixPlan? {
        val n = settings.blendBeats
        val pair = selectTempoPair(from.tempo, to.tempo) ?: return null
        val tempoA = pair.first
        val tempoB = pair.second
        if (tempoA.beats.size <= n || tempoB.beats.size <= n) return null

        val pickA = chooseCue(from.cues, tempoA, from.durationSec, true, settings, n) ?: return null
        val pickB = chooseCue(to.cues, tempoB, to.durationSec, false, settings, n) ?: return null
        val startA = tempoA.beats[pickA.beatIndex - n]
        val cueA = tempoA.beats[pickA.beatIndex]
        val startB = tempoB.beats[pickB.beatIndex - n]
        val cueB = tempoB.beats[pickB.beatIndex]

        val blend = cueA - startA
        if (blend < 1.0 || cueA > from.durationSec) return null
        val speed = (cueB - startB) / blend
        if (speed < 0.7 || speed > 1.4) return null
        val resume = cueB + RELEASE_SEC * (speed + 1.0) / 2.0
        if (resume + END_MARGIN_SEC > to.durationSec) return null

        val (cutSec, inSec) = vocalTiming(from.vocalPhrases, to.vocalPhrases, startA, startB, blend, speed)
        val level =
            if (from.lufs != null && to.lufs != null) (from.lufs - to.lufs).coerceIn(-MAX_LEVEL_DB, MAX_LEVEL_DB) else 0.0

        return MixPlan(
            fromMediaId = fromMediaId,
            toMediaId = toMediaId,
            bpmA = tempoA.bpm,
            bpmB = tempoB.bpm,
            blendBeats = n,
            startA = startA,
            cueA = cueA,
            startB = startB,
            cueB = cueB,
            speedB = speed,
            releaseSec = RELEASE_SEC,
            vocalCutSec = cutSec,
            vocalInSec = inSec,
            levelBDb = level,
        )
    }

    /** The BPM pair that fits together best (same score as the engine). */
    fun selectTempoPair(
        a: List<TempoOption>,
        b: List<TempoOption>,
    ): Pair<TempoOption, TempoOption>? {
        var best: Pair<TempoOption, TempoOption>? = null
        var bestScore = Double.NEGATIVE_INFINITY
        for (x in a) {
            for (y in b) {
                val compatibility = abs(ln(x.bpm / y.bpm))
                val centrality = abs(ln(x.bpm / 100.0)) + abs(ln(y.bpm / 100.0))
                val score = x.strength + y.strength - 2.5 * compatibility - 0.12 * centrality
                if (score > bestScore) {
                    bestScore = score
                    best = x to y
                }
            }
        }
        return best
    }

    fun chooseCue(
        candidates: DoubleArray,
        tempo: TempoOption,
        duration: Double,
        outgoing: Boolean,
        settings: AutoMixSettingsState,
        blendBeats: Int,
    ): CuePick? {
        val beats = tempo.beats
        if (beats.isEmpty()) return null
        val period = 60.0 / tempo.bpm

        // The part of the song where a cue is accepted.
        val low: Double
        val high: Double
        if (outgoing) {
            low =
                if (settings.mode == AutoMixMode.Auto) 0.55 * duration else duration * (1.0 - settings.outgoingRangePercent / 100.0)
            high = duration - 4.0
        } else {
            low = 0.0
            high =
                if (settings.mode == AutoMixMode.Auto) {
                    min(0.35 * duration, 75.0)
                } else {
                    duration * settings.incomingRangePercent / 100.0
                }
        }

        var bestCandidate = 0.0
        var bestIndex = -1
        var bestRank = 0.0
        for (candidate in candidates) {
            if (candidate < 0.0 || candidate > duration) continue
            val index = nearestIndex(beats, candidate)
            val snap = abs(beats[index] - candidate)
            if (snap > 0.6 * period || index < blendBeats) continue
            if (candidate < low || candidate > high) continue
            val rank = if (outgoing) candidate - 0.25 * snap else candidate + 0.25 * snap
            val better = bestIndex < 0 || (if (outgoing) rank > bestRank else rank < bestRank)
            if (better) {
                bestCandidate = candidate
                bestIndex = index
                bestRank = rank
            }
        }
        if (bestIndex >= 0) return CuePick(bestCandidate, bestIndex, true)

        // No cue in the range: use the beat grid.
        val target =
            if (outgoing) {
                if (settings.mode == AutoMixMode.Auto) 0.9 * duration else min(duration * (1.0 - settings.outgoingRangePercent / 200.0), high)
            } else {
                if (settings.mode == AutoMixMode.Auto) {
                    if (beats.size > blendBeats) beats[blendBeats] else return null
                } else {
                    max(high / 2.0, if (beats.size > blendBeats) beats[blendBeats] else return null)
                }
            }
        val index = max(nearestIndex(beats, target), blendBeats)
        if (index >= beats.size) return null
        return CuePick(target, index, false)
    }

    /** Index of the beat closest to [t] (the beat list is sorted). */
    fun nearestIndex(
        beats: DoubleArray,
        t: Double,
    ): Int {
        var lo = 0
        var hi = beats.size - 1
        while (lo < hi) {
            val mid = (lo + hi) / 2
            if (beats[mid] < t) lo = mid + 1 else hi = mid
        }
        return if (lo > 0 && abs(beats[lo - 1] - t) <= abs(beats[lo] - t)) lo - 1 else lo
    }

    /**
     * When A's vocals should be gone and B's vocals should come in (seconds from the clip start).
     * A's singer finishes the phrase they are in (not later than 75% of the blend), then B's singer
     * starts with the next phrase of B.
     */
    private fun vocalTiming(
        phrasesA: List<DoubleArray>,
        phrasesB: List<DoubleArray>,
        startA: Double,
        startB: Double,
        blend: Double,
        speed: Double,
    ): Pair<Double, Double> {
        var cut = 0.25 * blend
        for (p in phrasesA) {
            val end = p[1] - startA
            if (end >= 0.25 * blend) {
                cut = min(end, 0.75 * blend)
                break
            }
        }
        var vocalIn = 0.95 * blend
        for (p in phrasesB) {
            val t = (p[0] - startB) / speed
            if (t >= cut + 0.15) {
                vocalIn = min(t, 0.95 * blend)
                break
            }
        }
        return cut to max(vocalIn, cut + 0.15)
    }
}
