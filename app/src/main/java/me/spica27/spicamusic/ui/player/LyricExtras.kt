package me.spica27.spicamusic.ui.player

import me.spica27.spicamusic.common.entity.LyricItem

/** true when at least one line carries a translation. */
internal fun List<LyricItem>.hasTranslation(): Boolean =
    any { item ->
        when (item) {
            is LyricItem.NormalLyric -> !item.translation.isNullOrBlank() || item.translationVariants.isNotEmpty()
            is LyricItem.WordsLyric ->
                item.translation.isNotEmpty() ||
                    item.translationVariants.isNotEmpty() ||
                    item.accompaniment.any { it.translation.isNotEmpty() || it.translationVariants.isNotEmpty() }
            else -> false
        }
    }

/** true when at least one line carries a transliteration (romanised text). */
internal fun List<LyricItem>.hasTransliteration(): Boolean =
    any { item ->
        when (item) {
            is LyricItem.NormalLyric -> !item.phonetic.isNullOrBlank() || item.transliterations.isNotEmpty()
            is LyricItem.WordsLyric ->
                !item.phonetic.isNullOrBlank() ||
                    item.transliterations.isNotEmpty() ||
                    item.words.any { !it.phonetic.isNullOrBlank() } ||
                    item.accompaniment.any { acc ->
                        !acc.phonetic.isNullOrBlank() ||
                            acc.transliterations.isNotEmpty() ||
                            acc.words.any { !it.phonetic.isNullOrBlank() }
                    }
            else -> false
        }
    }

/** Returns the lyrics without their translation and/or transliteration lines. */
internal fun List<LyricItem>.withExtras(
    showTranslation: Boolean,
    showTransliteration: Boolean,
): List<LyricItem> {
    if (showTranslation && showTransliteration) return this
    return map { item ->
        when (item) {
            is LyricItem.NormalLyric ->
                item.copy(
                    translation = if (showTranslation) item.translation else null,
                    translationVariants = if (showTranslation) item.translationVariants else emptyList(),
                    phonetic = if (showTransliteration) item.phonetic else null,
                    transliterations = if (showTransliteration) item.transliterations else emptyList(),
                )
            is LyricItem.WordsLyric ->
                item.copy(
                    translation = if (showTranslation) item.translation else emptyList(),
                    translationVariants = if (showTranslation) item.translationVariants else emptyList(),
                    phonetic = if (showTransliteration) item.phonetic else null,
                    transliterations = if (showTransliteration) item.transliterations else emptyList(),
                    words =
                        if (showTransliteration) item.words else item.words.map { it.copy(phonetic = null) },
                    accompaniment =
                        item.accompaniment.map { acc ->
                            acc.copy(
                                translation = if (showTranslation) acc.translation else emptyList(),
                                translationVariants = if (showTranslation) acc.translationVariants else emptyList(),
                                phonetic = if (showTransliteration) acc.phonetic else null,
                                transliterations = if (showTransliteration) acc.transliterations else emptyList(),
                                words =
                                    if (showTransliteration) acc.words else acc.words.map { it.copy(phonetic = null) },
                            )
                        },
                )
            else -> item
        }
    }
}
