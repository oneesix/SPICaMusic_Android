package me.spica27.spicamusic.ui.player

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.chrisbanes.haze.HazeState
import me.spica27.spicamusic.ui.glass.LocalGlassV2Backdrop
import me.spica27.spicamusic.ui.glass.LocalLiquidGlassConfig
import me.spica27.spicamusic.ui.glass.LocalLiquidGlassV2Config
import me.spica27.spicamusic.ui.home.player_bar.playerBarSurface

/** true when the app's liquid glass (V2 or V1) can be drawn on the lyrics bars. */
@Composable
internal fun lyricsGlassAvailable(hazeState: HazeState?): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
    val v2 = LocalLiquidGlassV2Config.current.enabled && LocalGlassV2Backdrop.current != null
    val v1 = LocalLiquidGlassConfig.current.enabled && hazeState != null
    return v2 || v1
}

/**
 * Glass surface for the lyrics bars. It is the same engine and the same settings as the home bars
 * (V2 first, then V1), so the Liquid glass settings apply here too.
 * Returns null when liquid glass is off, so the caller keeps its normal look.
 */
@Composable
internal fun lyricsGlassSurfaceOrNull(
    hazeState: HazeState?,
    shape: RoundedCornerShape,
    containerColor: Color,
): Modifier? =
    if (lyricsGlassAvailable(hazeState)) {
        Modifier.playerBarSurface(
            hazeState = hazeState,
            containerColor = containerColor,
            glassEnabled = true,
            shape = shape,
            expandLayerBounds = false,
        )
    } else {
        null
    }
