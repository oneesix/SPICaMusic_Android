package me.spica27.spicamusic.ui.player

import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.Backdrop
import dev.chrisbanes.haze.HazeState
import me.spica27.spicamusic.ui.glass.LocalLiquidGlassConfig
import me.spica27.spicamusic.ui.glass.LocalLiquidGlassV2Config
import me.spica27.spicamusic.ui.glass.liquidGlassV2Surface
import me.spica27.spicamusic.ui.home.player_bar.playerBarSurface

/** Liquid glass V2 only: what the lyrics bars see behind them (player background + lyrics). */
internal val LocalLyricsGlassBackdrop = staticCompositionLocalOf<Backdrop?> { null }

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
): Modifier? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return null
    val v2Config = LocalLiquidGlassV2Config.current
    if (v2Config.enabled) {
        val backdrop = LocalLyricsGlassBackdrop.current ?: return null
        return Modifier.liquidGlassV2Surface(
            backdrop = backdrop,
            tuning = v2Config.tuning,
            shape = shape,
            surfaceColor = containerColor,
        )
    }
    if (LocalLiquidGlassConfig.current.enabled && hazeState != null) {
        return Modifier.playerBarSurface(
            hazeState = hazeState,
            containerColor = containerColor,
            glassEnabled = true,
            shape = shape,
            expandLayerBounds = false,
        )
    }
    return null
}
