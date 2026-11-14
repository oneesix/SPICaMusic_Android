package me.spica27.spicamusic.ui.player

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Remembers the playlist the current queue was started from, so the new player UI can show its name.
 * The name is only shown while the queue still holds the same songs (see [PlaybackSource.matches]).
 */
object PlaybackSourceHolder {
    private val _source = MutableStateFlow<PlaybackSource?>(null)
    val source: StateFlow<PlaybackSource?> = _source.asStateFlow()

    fun set(
        name: String?,
        mediaIds: List<String>,
    ) {
        _source.value = if (name.isNullOrBlank()) null else PlaybackSource(name, mediaIds.toSet())
    }
}

data class PlaybackSource(
    val name: String,
    val mediaIds: Set<String>,
) {
    /** true when the queue holds exactly the songs of this playlist. */
    fun matches(queueMediaIds: Collection<String>): Boolean = queueMediaIds.size == mediaIds.size && queueMediaIds.toSet() == mediaIds
}
