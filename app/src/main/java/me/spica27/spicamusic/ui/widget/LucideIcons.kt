package me.spica27.spicamusic.ui.widget

import android.content.Context
import androidx.annotation.DrawableRes
import me.spica27.spicamusic.R
import java.util.concurrent.ConcurrentHashMap

/**
 * Lucide 图标（com.composables:icons-lucide-android，以矢量 drawable 提供）。
 * 按名称解析，如 "music"、"audio-lines"；找不到时返回 0，由调用方回退到内置图标。
 */
object LucideIcons {
    private val prefixes = listOf("lucide_ic_", "ic_lucide_", "lucide_")
    private val cache = ConcurrentHashMap<String, Int>()

    private fun normalize(name: String) =
        name
            .trim()
            .lowercase()
            .replace('-', '_')
            .replace(' ', '_')

    @DrawableRes
    fun resolve(
        context: Context,
        name: String,
    ): Int {
        val key = normalize(name)
        if (key.isEmpty()) return 0
        return cache.getOrPut(key) {
            prefixes
                .asSequence()
                .map { context.resources.getIdentifier(it + key, "drawable", context.packageName) }
                .firstOrNull { it != 0 } ?: 0
        }
    }

    /** 从 R.drawable 反射得到全部 Lucide 图标名（短横线形式）；失败时为空。 */
    private val reflectedNames: List<String> by lazy {
        runCatching {
            R.drawable::class.java.fields
                .map { it.name }
                .mapNotNull { field ->
                    val prefix = prefixes.firstOrNull { field.startsWith(it) } ?: return@mapNotNull null
                    field.removePrefix(prefix).replace('_', '-')
                }.distinct()
                .sorted()
        }.getOrDefault(emptyList())
    }

    /** 反射不可用时的常用名称（只会显示实际存在的图标）。 */
    private val commonNames =
        listOf(
            "house", "home", "music", "music-2", "music-3", "music-4", "library", "library-big", "search",
            "compass", "heart", "star", "settings", "user", "users", "disc", "disc-2", "disc-3", "headphones",
            "mic", "mic-vocal", "play", "pause", "radio", "folder", "folder-open", "album", "audio-lines",
            "audio-waveform", "list-music", "list", "layers", "layout-grid", "bookmark", "clock", "history",
            "download", "upload", "cloud", "sparkles", "flame", "zap", "sun", "moon", "globe", "map", "tag",
            "hash", "shuffle", "repeat", "volume-2", "speaker", "guitar", "piano", "drum", "podcast",
            "square-play", "circle-play", "telescope", "binoculars", "rocket", "gem", "crown", "grid-2x2",
            "layout-dashboard", "list-video", "disc-album", "heart-handshake", "ear", "smile", "bell",
            "calendar", "chart-no-axes-column", "trending-up", "newspaper", "shopping-bag", "gift",
        )

    fun searchNames(
        context: Context,
        query: String,
        limit: Int = 24,
    ): List<String> {
        val q = query.trim().lowercase().replace(' ', '-').replace('_', '-')
        if (q.isEmpty()) return emptyList()
        val source = reflectedNames.ifEmpty { commonNames }
        return source
            .filter { it.contains(q) && resolve(context, it) != 0 }
            .sortedWith(compareBy({ !it.startsWith(q) }, { it.length }))
            .take(limit)
    }
}
