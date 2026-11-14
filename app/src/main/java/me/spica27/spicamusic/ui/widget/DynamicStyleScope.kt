package me.spica27.spicamusic.ui.widget

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import me.spica27.spicamusic.core.preferences.LyricsStyleSettings

/**
 * 颜色风格“动态”：复用歌词设置里“播放器界面配色”的引擎和全部选项，
 * 让整个应用的前景色（文字、图标、按钮、进度条）跟随当前专辑封面。
 * 背景着色开启时，在内容上方叠一层不拦截触摸的封面色。
 */
@Composable
fun DynamicStyleScope(
    enabled: Boolean,
    settings: LyricsStyleSettings,
    artworkUri: Uri?,
    content: @Composable () -> Unit,
) {
    val colors = rememberDynamicPlayerColors(settings.copy(dynamicLyricsEnabled = enabled), artworkUri)
    DynamicPlayerColorScope(colors = colors) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        drawContent()
                        colors?.backgroundTint?.let { drawRect(it) }
                    },
        ) {
            content()
        }
    }
}
