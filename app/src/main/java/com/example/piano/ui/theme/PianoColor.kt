package com.example.piano.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Piano 应用颜色接口
 * 定义应用的所有颜色属性
 * 完全替代 MaterialTheme.colorScheme，保持蓝色色调
 */
interface PianoColor {
    // ========== 主要颜色 ==========
    /** 主色调 */
    val primary: Color
    /** 次要色调 */
    val secondary: Color
    /** 强调色 */
    val accent: Color
    /** 第三色调 */
    val tertiary: Color
    
    // ========== 主色调相关 ==========
    /** 主色调容器背景 */
    val primaryContainer: Color
    /** 主色调上的文字颜色 */
    val onPrimary: Color
    /** 主色调容器上的文字颜色 */
    val onPrimaryContainer: Color
    
    // ========== 次要色调相关 ==========
    /** 次要色调容器背景 */
    val secondaryContainer: Color
    /** 次要色调上的文字颜色 */
    val onSecondary: Color
    /** 次要色调容器上的文字颜色 */
    val onSecondaryContainer: Color
    
    // ========== 背景颜色 ==========
    /** 页面背景色 */
    val background: Color
    /** 背景上的文字颜色 */
    val onBackground: Color
    /** 卡片/表面背景色 */
    val surface: Color
    /** 表面上的文字颜色 */
    val onSurface: Color
    /** 次要表面背景色 */
    val surfaceVariant: Color
    /** 次要表面上的文字颜色 */
    val onSurfaceVariant: Color
    
    // ========== 文本颜色（便捷属性） ==========
    /** 主要文本颜色（等同于 onSurface） */
    val textPrimary: Color
    /** 次要文本颜色 */
    val textSecondary: Color
    /** 第三级文本颜色 */
    val textTertiary: Color
    /** 禁用文本颜色 */
    val textDisabled: Color
    
    // ========== 系统栏颜色 ==========
    /** 状态栏颜色 */
    val statusBarColor: Color
    /** 导航栏颜色 */
    val navigationBarColor: Color
    
    // ========== 边框和分割线 ==========
    /** 边框颜色 */
    val border: Color
    /** 分割线颜色 */
    val divider: Color
    /** 轮廓颜色 */
    val outline: Color
    /** 轮廓变化颜色 */
    val outlineVariant: Color
    
    // ========== 状态颜色 ==========
    /** 成功状态颜色 */
    val success: Color
    /** 警告状态颜色 */
    val warning: Color
    /** 错误状态颜色 */
    val error: Color
    /** 错误容器背景 */
    val errorContainer: Color
    /** 错误上的文字颜色 */
    val onError: Color
    /** 错误容器上的文字颜色 */
    val onErrorContainer: Color
    /** 信息状态颜色 */
    val info: Color
    
    // ========== 其他 ==========
    /** 反向表面（用于强调） */
    val inverseSurface: Color
    /** 反向表面上的文字 */
    val inverseOnSurface: Color
    /** 反向主色调 */
    val inversePrimary: Color
}
