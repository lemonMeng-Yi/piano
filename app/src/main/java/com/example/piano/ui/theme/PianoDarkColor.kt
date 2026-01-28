package com.example.piano.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 深色主题颜色方案（夜间模式）
 * 实现 PianoColor 接口
 */
object PianoDarkColor : PianoColor {
    // ========== 主要颜色 ==========
    override val primary: Color = PianoBlue80
    override val secondary: Color = PianoBlueGrey80
    override val accent: Color = PianoAccent80
    override val tertiary: Color = PianoAccent80  // 使用强调色作为第三色调
    
    // ========== 主色调相关 ==========
    override val primaryContainer: Color = Color(0xFF3D4A6B)  // 深蓝色容器
    override val onPrimary: Color = Color(0xFF1C2A4A)
    override val onPrimaryContainer: Color = Color(0xFFD6E4FF)
    
    // ========== 次要色调相关 ==========
    override val secondaryContainer: Color = Color(0xFF3D4A5C)  // 深蓝灰色容器
    override val onSecondary: Color = Color(0xFF2A3440)
    override val onSecondaryContainer: Color = Color(0xFFD6E0F0)
    
    // ========== 背景颜色 ==========
    override val background: Color = Color(0xFF121212)             // 深色背景
    override val onBackground: Color = Color(0xFFE0E0E0)            // 浅色文字
    override val surface: Color = Color(0xFF1E1E1E)                // 卡片背景
    override val onSurface: Color = Color(0xFFE0E0E0)               // 浅色文字
    override val surfaceVariant: Color = Color(0xFF2C2C2C)          // 次要表面
    override val onSurfaceVariant: Color = Color(0xFFB0B0B0)         // 灰色文字
    
    // ========== 文本颜色（便捷属性） ==========
    override val textPrimary: Color = onSurface
    override val textSecondary: Color = Color(0xFFB0B0B0)
    override val textTertiary: Color = Color(0xFF757575)
    override val textDisabled: Color = Color(0xFF616161)
    
    // ========== 系统栏颜色 ==========
    override val statusBarColor: Color = Color(0xFF121212)
    override val navigationBarColor: Color = Color(0xFF121212)
    
    // ========== 边框和分割线 ==========
    override val border: Color = Color(0xFF424242)
    override val divider: Color = Color(0xFF424242)
    override val outline: Color = Color(0xFF938F99)
    override val outlineVariant: Color = Color(0xFF49454F)
    
    // ========== 状态颜色 ==========
    override val success: Color = Color(0xFF66BB6A)
    override val warning: Color = Color(0xFFFFB74D)
    override val error: Color = Color(0xFFEF5350)
    override val errorContainer: Color = Color(0xFF93000A)
    override val onError: Color = Color(0xFFFFDAD6)
    override val onErrorContainer: Color = Color(0xFFFFDAD6)
    override val info: Color = Color(0xFF42A5F5)
    
    // ========== 其他 ==========
    override val inverseSurface: Color = Color(0xFFE0E0E0)
    override val inverseOnSurface: Color = Color(0xFF313033)
    override val inversePrimary: Color = PianoBlue40
}
