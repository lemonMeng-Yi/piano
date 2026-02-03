package com.example.piano.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.piano.core.manager.LocalThemeManager
import com.example.piano.ui.theme.AppTheme
import com.example.piano.ui.theme.PianoDarkColor
import com.example.piano.ui.theme.PianoLightColor
import com.example.piano.ui.theme.PianoTheme

/**
 * 示例：如何使用自定义颜色
 * 
 * 这个文件展示了如何使用两套颜色方案（深色/浅色）
 * 根据主题自动切换
 */

// ========== 方式 1：使用 CompositionLocal（推荐） ==========

/**
 * 使用 CompositionLocal 获取自定义颜色
 * 这种方式会自动响应主题切换
 * 
 * 颜色会根据当前主题自动选择：
 * - 浅色主题 → LightCustomColors
 * - 深色主题 → DarkCustomColors
 */
@Composable
fun CustomColorExample1() {
    // 使用 PianoTheme.colors 获取颜色（类似 VGTheme.colors）
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PianoTheme.colors.background)  // ← 自动使用深色或浅色背景
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "使用 PianoTheme.colors",
                color = PianoTheme.colors.textPrimary  // ← 自动使用深色或浅色文字
            )
            Text(
                text = "次要文本",
                color = PianoTheme.colors.textSecondary  // ← 自动使用次要文字颜色
            )
            Text(
                text = "第三级文本",
                color = PianoTheme.colors.textTertiary  // ← 使用第三级文字颜色
            )
        }
    }
}

// ========== 方式 2：使用 ThemeManager 判断 ==========

/**
 * 在组件内根据主题动态选择颜色
 * 适合少量自定义颜色的场景
 * 
 * 注意：这种方式需要手动判断主题，不如方式1方便
 */
@Composable
fun CustomColorExample2() {
    val themeManager = LocalThemeManager.current
    val actualTheme = themeManager.getActualTheme()
    val isDarkTheme = actualTheme == AppTheme.Dark
    
    // 根据主题选择颜色（手动判断）
    val backgroundColor = if (isDarkTheme) {
        PianoDarkColor.background  // 深色背景
    } else {
        PianoLightColor.background  // 浅色背景
    }
    
    val textColor = if (isDarkTheme) {
        PianoDarkColor.textPrimary  // 深色文字
    } else {
        PianoLightColor.textPrimary  // 浅色文字
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)  // ← 自动响应主题
            .padding(16.dp)
    ) {
        Text(
            text = "使用 ThemeManager 判断",
            color = textColor  // ← 自动响应主题
        )
    }
}

// ========== 方式 3：混合使用 ==========

/**
 * 同时使用 MaterialTheme 和自定义颜色
 * 
 * 最佳实践：
 * - MaterialTheme 用于标准 Material 组件
 * - CustomColors 用于自定义 UI 元素
 */
@Composable
fun CustomColorExample3() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(PianoTheme.colors.background)  // ← 自定义背景颜色
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "混合使用",
                color = PianoTheme.colors.textPrimary,  // ← 自定义文本颜色
                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge  // ← MaterialTheme 字体
            )
            Text(
                text = "使用 MaterialTheme 的字体样式",
                color = PianoTheme.colors.textSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// ========== 实际使用示例 ==========

/**
 * 示例：使用完整的颜色方案创建卡片
 */
@Composable
fun CustomCardExample() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PianoTheme.colors.surface  // ← 使用自定义表面颜色
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "标题",
                color = PianoTheme.colors.textPrimary,
                style = androidx.compose.material3.MaterialTheme.typography.titleLarge
            )
            Text(
                text = "描述文本",
                color = PianoTheme.colors.textSecondary,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "第三级文本示例",
                color = PianoTheme.colors.textTertiary,  // ← 使用 textTertiary
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
            )
            
            // 使用状态颜色
            Row {
                Box(
                    modifier = Modifier
                        .background(PianoTheme.colors.success)
                        .padding(8.dp)
                ) {
                    Text("成功", color = PianoTheme.colors.textPrimary)
                }
                Box(
                    modifier = Modifier
                        .background(PianoTheme.colors.error)
                        .padding(8.dp)
                ) {
                    Text("错误", color = PianoTheme.colors.textPrimary)
                }
            }
        }
    }
}
