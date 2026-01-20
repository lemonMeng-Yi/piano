package com.example.piano.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Piano 应用主题颜色定义
 * 
 * 颜色命名规则：
 * - 80: 浅色系（用于深色主题）
 * - 40: 深色系（用于浅色主题）
 */

// ========== 浅色系（深色主题使用） ==========

/** 浅蓝色 - 主色调 */
val PianoBlue80 = Color(0xFF9BB3E8)

/** 浅蓝灰色 - 次要色调 */
val PianoBlueGrey80 = Color(0xFFB8C5D9)

/** 浅强调色 - 强调色调 */
val PianoAccent80 = Color(0xFFA8C5E8)

// ========== 深色系（浅色主题使用） ==========

/** 主蓝色 - 主要按钮、链接颜色 */
val PianoBlue40 = Color(0xFF5A73B8)

/** 深蓝灰色 - 标题、slogan 颜色 */
val PianoBlueGrey40 = Color(0xFF475D92)

/** 强调蓝色 - 强调按钮、重要元素 */
val PianoAccent40 = Color(0xFF4070F0)