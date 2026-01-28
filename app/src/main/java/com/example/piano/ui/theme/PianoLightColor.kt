package com.example.piano.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 浅色主题颜色方案（白天模式）
 * 实现 PianoColor 接口
 */
object PianoLightColor : PianoColor {
    // ========== 主要颜色 ==========
    override val primary: Color = PianoBlue40
    override val secondary: Color = PianoBlueGrey40
    override val accent: Color = PianoAccent40
    override val tertiary: Color = PianoAccent40  // 使用强调色作为第三色调
    
    // ========== 主色调相关 ==========
    override val primaryContainer: Color = Color(0xFFE3E8F5)  // 浅蓝色容器
    override val onPrimary: Color = Color.White
    override val onPrimaryContainer: Color = PianoBlue40
    
    // ========== 次要色调相关 ==========
    override val secondaryContainer: Color = Color(0xFFE8EBF0)  // 浅蓝灰色容器
    override val onSecondary: Color = Color.White
    override val onSecondaryContainer: Color = PianoBlueGrey40
    
    // ========== 背景颜色 ==========
    override val background: Color = Color(0xFFFFFBFE)           // 白色背景
    override val onBackground: Color = Color(0xFF1C1B1F)          // 深色文字
    override val surface: Color = Color(0xFFFFFFFF)                // 卡片背景
    override val onSurface: Color = Color(0xFF1C1B1F)             // 深色文字
    override val surfaceVariant: Color = Color(0xFFF5F5F5)        // 次要表面
    override val onSurfaceVariant: Color = Color(0xFF757575)       // 灰色文字
    
    // ========== 文本颜色（便捷属性） ==========
    override val textPrimary: Color = onSurface
    override val textSecondary: Color = Color(0xFF757575)
    override val textTertiary: Color = Color(0xFF9E9E9E)
    override val textDisabled: Color = Color(0xFFBDBDBD)
    
    // ========== 系统栏颜色 ==========
    override val statusBarColor: Color = Color(0xFFFFFBFE)
    override val navigationBarColor: Color = Color(0xFFFFFBFE)
    
    // ========== 边框和分割线 ==========
    override val border: Color = Color(0xFFE0E0E0)
    override val divider: Color = Color(0xFFE0E0E0)
    override val outline: Color = Color(0xFF79747E)
    override val outlineVariant: Color = Color(0xFFCAC4D0)
    
    // ========== 状态颜色 ==========
    override val success: Color = Color(0xFF4CAF50)
    override val warning: Color = Color(0xFFFF9800)
    override val error: Color = Color(0xFFF44336)
    override val errorContainer: Color = Color(0xFFFFDAD6)
    override val onError: Color = Color.White
    override val onErrorContainer: Color = Color(0xFF410002)
    override val info: Color = Color(0xFF2196F3)
    
    // ========== 其他 ==========
    override val inverseSurface: Color = Color(0xFF313033)
    override val inverseOnSurface: Color = Color(0xFFF4EFF4)
    override val inversePrimary: Color = Color(0xFFB0C4FF)
}
