package com.example.piano.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal 提供颜色访问
 */
@Suppress("ktlint:compose:compositionlocal-allowlist")
private val LocalPianoColors = staticCompositionLocalOf<PianoColor> {
    PianoLightColor
}

/**
 * Piano 主题对象
 * 提供类似 VGTheme.colors.xxx 的调用方式
 */
object PianoTheme {
    /**
     * 获取当前主题的颜色
     * 使用方式：PianoTheme.colors.textPrimary
     */
    val colors: PianoColor
        @Composable
        get() = LocalPianoColors.current
}

/**
 * 便捷函数：获取当前主题的颜色
 * 
 * 使用示例：
 * ```kotlin
 * val colors = pianoColors()
 * Box(modifier = Modifier.background(colors.background))
 * ```
 */
@Composable
fun pianoColors(): PianoColor {
    return PianoTheme.colors
}

/**
 * 提供 Piano 颜色给子组件
 */
@Composable
fun ProvidePianoColors(
    colors: PianoColor,
    content: @Composable () -> Unit
) {
    androidx.compose.runtime.CompositionLocalProvider(LocalPianoColors provides colors, content = content)
}
