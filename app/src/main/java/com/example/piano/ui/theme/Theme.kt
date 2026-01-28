package com.example.piano.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Build
import android.view.View
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext


@SuppressLint("ContextCastToActivity")
@Composable
fun PianoTheme(
    darkTheme: Boolean? = null,
    content: @Composable () -> Unit
) {
    // 如果 darkTheme 为 null，使用系统设置；否则使用传入的值
    val isDarkTheme = darkTheme ?: isSystemInDarkTheme()
    
    // Piano 自定义颜色方案（根据主题选择）
    val pianoColors = if (isDarkTheme) PianoDarkColor else PianoLightColor
    
    // 将 PianoColor 转换为 MaterialTheme 的 ColorScheme（保持兼容性）
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = pianoColors.primary,
            onPrimary = pianoColors.onPrimary,
            primaryContainer = pianoColors.primaryContainer,
            onPrimaryContainer = pianoColors.onPrimaryContainer,
            secondary = pianoColors.secondary,
            onSecondary = pianoColors.onSecondary,
            secondaryContainer = pianoColors.secondaryContainer,
            onSecondaryContainer = pianoColors.onSecondaryContainer,
            tertiary = pianoColors.tertiary,
            background = pianoColors.background,
            onBackground = pianoColors.onBackground,
            surface = pianoColors.surface,
            onSurface = pianoColors.onSurface,
            surfaceVariant = pianoColors.surfaceVariant,
            onSurfaceVariant = pianoColors.onSurfaceVariant,
            error = pianoColors.error,
            onError = pianoColors.onError,
            errorContainer = pianoColors.errorContainer,
            onErrorContainer = pianoColors.onErrorContainer,
            outline = pianoColors.outline,
            outlineVariant = pianoColors.outlineVariant,
            inverseSurface = pianoColors.inverseSurface,
            inverseOnSurface = pianoColors.inverseOnSurface,
            inversePrimary = pianoColors.inversePrimary
        )
    } else {
        lightColorScheme(
            primary = pianoColors.primary,
            onPrimary = pianoColors.onPrimary,
            primaryContainer = pianoColors.primaryContainer,
            onPrimaryContainer = pianoColors.onPrimaryContainer,
            secondary = pianoColors.secondary,
            onSecondary = pianoColors.onSecondary,
            secondaryContainer = pianoColors.secondaryContainer,
            onSecondaryContainer = pianoColors.onSecondaryContainer,
            tertiary = pianoColors.tertiary,
            background = pianoColors.background,
            onBackground = pianoColors.onBackground,
            surface = pianoColors.surface,
            onSurface = pianoColors.onSurface,
            surfaceVariant = pianoColors.surfaceVariant,
            onSurfaceVariant = pianoColors.onSurfaceVariant,
            error = pianoColors.error,
            onError = pianoColors.onError,
            errorContainer = pianoColors.errorContainer,
            onErrorContainer = pianoColors.onErrorContainer,
            outline = pianoColors.outline,
            outlineVariant = pianoColors.outlineVariant,
            inverseSurface = pianoColors.inverseSurface,
            inverseOnSurface = pianoColors.inverseOnSurface,
            inversePrimary = pianoColors.inversePrimary
        )
    }
    
    // 提供 Piano 颜色给子组件
    ProvidePianoColors(colors = pianoColors) {
        val currentColors = PianoTheme.colors // 获取当前颜色
        val context = LocalContext.current
        
        // 同步系统栏颜色和图标样式
        SideEffect {
            val activity = context as? Activity ?: return@SideEffect
            val window = activity.window
            
            // 更新状态栏颜色
            window.statusBarColor = currentColors.statusBarColor.toArgb()
            
            // 更新导航栏颜色
            window.navigationBarColor = currentColors.navigationBarColor.toArgb()
            
            // 同步系统栏图标（状态栏与导航栏）的明暗样式
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = window.insetsController
                val appearanceMask =
                    android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS or
                            android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                if (isDarkTheme) {
                    // 深色主题：使用"深色图标"→ 关闭浅色图标样式
                    controller?.setSystemBarsAppearance(0, appearanceMask)
                } else {
                    // 浅色主题：使用"浅色图标"→ 打开浅色图标样式
                    controller?.setSystemBarsAppearance(appearanceMask, appearanceMask)
                }
            } else {
                var flags = window.decorView.systemUiVisibility
                if (isDarkTheme) {
                    // 深色主题：清除浅色状态栏/导航栏图标
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        flags = flags and View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR.inv()
                    }
                } else {
                    // 浅色主题：启用浅色状态栏/导航栏图标
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    }
                }
                window.decorView.systemUiVisibility = flags
            }
        }
        
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
