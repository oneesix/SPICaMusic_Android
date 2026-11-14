package me.spica27.spicamusic.ui.widget

import android.net.Uri
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import me.spica27.spicamusic.core.preferences.LyricsStyleSettings

/**
 * 颜色风格“动态”：复用歌词设置里“播放器界面配色”的引擎和全部选项，
 * 让整个应用的前景色（文字、图标、按钮、进度条）跟随当前专辑封面。
 * 背景着色开启时，在内容上方叠一层不拦截触摸的封面色。
 *
 * 组合结构必须保持固定：开关动态风格时只改变颜色，不能让页面内容换位置，
 * 否则导航页面的状态会被重复注册而崩溃（Key ... was used multiple times）。
 */
@Composable
fun DynamicStyleScope(
    enabled: Boolean,
    settings: LyricsStyleSettings,
    artworkUri: Uri?,
    content: @Composable () -> Unit,
) {
    val colors = rememberDynamicPlayerColors(settings.copy(dynamicLyricsEnabled = enabled), artworkUri)
    val base = MaterialTheme.colorScheme
    val scheme = remember(base, colors) { if (colors == null) base else base.withDynamicColors(colors) }
    // MaterialTheme 会把点击反馈重置为水波纹，这里还原应用自己的点击反馈。
    val indication = LocalIndication.current
    MaterialTheme(
        colorScheme = scheme,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
    ) {
        CompositionLocalProvider(LocalIndication provides indication) {
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
}
