package me.spica27.spicamusic.automix

import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.File

/** One tempo guess for a song: the BPM, how sure the analysis was, and the beat times for that BPM. */
class TempoOption(
    val bpm: Double,
    val strength: Double,
    val beats: DoubleArray,
)

/**
 * Everything the phone needs from a song's song.json to plan a transition:
 * tempo options with beat grids, CUE-DETR cue candidates, vocal phrases, loudness and the stem files.
 */
class StemSongData(
    val durationSec: Double,
    val lufs: Double?,
    val tempo: List<TempoOption>,
    val cues: DoubleArray,
    /** Each item is [start, end] in seconds. */
    val vocalPhrases: List<DoubleArray>,
    val stemFiles: Map<String, File>,
) {
    companion object {
        val STEM_NAMES = listOf("drums", "bass", "other", "vocals")

        fun load(song: MixingSong): StemSongData? =
            try {
                val json = JSONObject(File(song.folder, "song.json").readText(Charsets.UTF_8))
                val files = json.getJSONObject("stems").getJSONObject("files")
                val stems = HashMap<String, File>()
                for (name in STEM_NAMES) stems[name] = File(song.folder, files.getString(name))

                val tempo = ArrayList<TempoOption>()
                val candidates = json.getJSONObject("tempo").getJSONArray("candidates")
                for (i in 0 until candidates.length()) {
                    val o = candidates.getJSONObject(i)
                    tempo.add(TempoOption(o.getDouble("bpm"), o.getDouble("strength"), doubles(o.getJSONArray("beats"))))
                }

                val phrases = ArrayList<DoubleArray>()
                val phraseArray = json.optJSONObject("vocals")?.optJSONArray("phrases")
                if (phraseArray != null) {
                    for (i in 0 until phraseArray.length()) {
                        val p = phraseArray.getJSONArray(i)
                        phrases.add(doubleArrayOf(p.getDouble(0), p.getDouble(1)))
                    }
                }

                val loudness = json.optJSONObject("loudness")
                val lufs =
                    if (loudness == null || loudness.isNull("integratedLufs")) null else loudness.getDouble("integratedLufs")

                StemSongData(
                    durationSec = json.getLong("durationMs") / 1000.0,
                    lufs = lufs,
                    tempo = tempo,
                    cues = doubles(json.getJSONObject("cues").getJSONArray("candidates")),
                    vocalPhrases = phrases,
                    stemFiles = stems,
                )
            } catch (e: Exception) {
                Timber.w(e, "Could not read song.json in %s", song.folder.path)
                null
            }

        private fun doubles(array: JSONArray): DoubleArray = DoubleArray(array.length()) { array.getDouble(it) }
    }
}
