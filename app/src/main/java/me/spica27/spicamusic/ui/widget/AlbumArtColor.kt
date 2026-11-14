package me.spica27.spicamusic.ui.widget

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import com.skydoves.landscapist.core.ImageRequest
import com.skydoves.landscapist.core.Landscapist
import com.skydoves.landscapist.core.model.CachePolicy
import com.skydoves.landscapist.core.model.ImageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import me.spica27.spicamusic.artwork.MusicArtwork
import kotlin.coroutines.resume

/** Main colour of an album cover, or null while loading / when there is no cover. */
@Composable
fun rememberAlbumArtColor(artwork: MusicArtwork?): Color? {
    var color by remember(artwork) { mutableStateOf<Color?>(null) }
    LaunchedEffect(artwork) {
        color = if (artwork == null || artwork.isEmpty) null else loadAlbumArtColor(artwork)
    }
    return color
}

private suspend fun loadAlbumArtColor(artwork: MusicArtwork): Color? {
    val bitmap =
        suspendCancellableCoroutine<Bitmap?> { cont ->
            val request =
                ImageRequest
                    .builder()
                    .model(artwork)
                    .size(64, 64)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build()
            val disposable =
                Landscapist.getInstance().enqueue(request) { result ->
                    when (result) {
                        is ImageResult.Success ->
                            if (result.isFinal && cont.isActive) cont.resume(result.data as? Bitmap)

                        is ImageResult.Failure -> if (cont.isActive) cont.resume(null)

                        ImageResult.Loading -> Unit
                    }
                }
            cont.invokeOnCancellation { disposable.dispose() }
        } ?: return null
    return withContext(Dispatchers.Default) {
        try {
            // Palette needs a software bitmap; the cached one is never recycled here.
            val source =
                if (bitmap.config == Bitmap.Config.HARDWARE) bitmap.copy(Bitmap.Config.ARGB_8888, false) else bitmap
            val palette = Palette.from(source).maximumColorCount(16).generate()
            val swatch = palette.vibrantSwatch ?: palette.dominantSwatch ?: palette.mutedSwatch
            swatch?.let { Color(it.rgb) }
        } catch (e: Exception) {
            null
        }
    }
}
