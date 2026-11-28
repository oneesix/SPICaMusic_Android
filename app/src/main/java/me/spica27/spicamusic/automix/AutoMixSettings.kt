package me.spica27.spicamusic.automix

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** How the mix points are chosen. */
enum class AutoMixMode(
    val key: String,
) {
    /** The best cues, picked by the engine rules. */
    Auto("auto"),

    /** The user chooses the part of each song where the app looks for a cue. */
    Manual("manual"),
    ;

    companion object {
        fun fromKey(key: String?): AutoMixMode = entries.firstOrNull { it.key == key } ?: Auto
    }
}

/** The values of the "AutoMix settings" sheet. */
data class AutoMixSettingsState(
    val mode: AutoMixMode = AutoMixMode.Auto,
    /** Manual mode: song A looks for its mix point in the last N percent of the song. */
    val outgoingRangePercent: Int = 10,
    /** Manual mode: song B looks for its mix point in the first N percent of the song. */
    val incomingRangePercent: Int = 10,
    /** How many beats the blend lasts. */
    val blendBeats: Int = 32,
    /** Use TTML lyrics for the vocal handover when a song has them. */
    val useTtmlVocalTiming: Boolean = true,
)

/**
 * Saved AutoMix settings. They live in the same preferences file as the on/off switch
 * ([AutoMixStore]), under their own keys, so nothing that is saved already changes.
 */
object AutoMixSettings {
    private const val PREFS = "automix_prefs"
    private const val KEY_MODE = "mix_mode"
    private const val KEY_RANGE_OUT = "mix_range_out_percent"
    private const val KEY_RANGE_IN = "mix_range_in_percent"
    private const val KEY_BEATS = "mix_blend_beats"
    private const val KEY_TTML = "mix_use_ttml"

    val BLEND_BEATS_OPTIONS = listOf(8, 16, 32, 64)
    val RANGE_PERCENT_RANGE = 5..50

    private val _state = MutableStateFlow(AutoMixSettingsState())
    val state: StateFlow<AutoMixSettingsState> = _state.asStateFlow()

    @Volatile
    private var loaded = false

    /** Read the saved values once. Safe to call many times. */
    fun init(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            val defaults = AutoMixSettingsState()
            _state.value =
                sanitize(
                    AutoMixSettingsState(
                        mode = AutoMixMode.fromKey(prefs.getString(KEY_MODE, null)),
                        outgoingRangePercent = prefs.getInt(KEY_RANGE_OUT, defaults.outgoingRangePercent),
                        incomingRangePercent = prefs.getInt(KEY_RANGE_IN, defaults.incomingRangePercent),
                        blendBeats = prefs.getInt(KEY_BEATS, defaults.blendBeats),
                        useTtmlVocalTiming = prefs.getBoolean(KEY_TTML, defaults.useTtmlVocalTiming),
                    ),
                )
            loaded = true
        }
    }

    fun update(
        context: Context,
        transform: (AutoMixSettingsState) -> AutoMixSettingsState,
    ) {
        init(context)
        val next = sanitize(transform(_state.value))
        _state.value = next
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, next.mode.key)
            .putInt(KEY_RANGE_OUT, next.outgoingRangePercent)
            .putInt(KEY_RANGE_IN, next.incomingRangePercent)
            .putInt(KEY_BEATS, next.blendBeats)
            .putBoolean(KEY_TTML, next.useTtmlVocalTiming)
            .apply()
    }

    private fun sanitize(value: AutoMixSettingsState): AutoMixSettingsState =
        value.copy(
            outgoingRangePercent = value.outgoingRangePercent.coerceIn(RANGE_PERCENT_RANGE),
            incomingRangePercent = value.incomingRangePercent.coerceIn(RANGE_PERCENT_RANGE),
            blendBeats = if (value.blendBeats in BLEND_BEATS_OPTIONS) value.blendBeats else 32,
        )
}
