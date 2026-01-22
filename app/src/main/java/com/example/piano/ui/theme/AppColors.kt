package com.example.piano.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.example.piano.R

/**
 * 应用颜色资源
 * 提供便捷的方法在 Compose 中使用颜色资源
 */
object AppColors {
    
    // ========== 文本颜色 ==========
    
    @Composable
    fun textPrimary() = colorResource(R.color.color_text_primary)
    
    @Composable
    fun textSecondary() = colorResource(R.color.color_text_secondary)
    
    @Composable
    fun textTertiary() = colorResource(R.color.color_text_tertiary)
    
    // ========== 反馈颜色 ==========
    
    @Composable
    fun feedbackError() = colorResource(R.color.color_feedback_error_default)
    
    @Composable
    fun feedbackSuccess() = colorResource(R.color.color_feedback_success_default)
    
    @Composable
    fun feedbackWarning() = colorResource(R.color.color_feedback_warning_default)
    
    // ========== Toast 颜色 ==========
    
    @Composable
    fun toastBackground() = colorResource(R.color.color_toast_background)
    
    // ========== 图标颜色 ==========
    
    @Composable
    fun iconPrimary() = colorResource(R.color.color_icon_primary)
    
    @Composable
    fun iconActive() = colorResource(R.color.color_icon_active)
    
    @Composable
    fun iconRise() = colorResource(R.color.color_icon_rise)
    
    @Composable
    fun iconFall() = colorResource(R.color.color_icon_fall)
    
    // ========== 按钮颜色 ==========
    
    @Composable
    fun buttonPrimary() = colorResource(R.color.color_button_background_primary_default)
    
    @Composable
    fun buttonPrimaryDisable() = colorResource(R.color.color_button_background_primary_disable)
    
    // ========== 背景颜色 ==========
    
    @Composable
    fun pageBackgroundPrimary() = colorResource(R.color.color_page_background_primary)
    
    @Composable
    fun surfaceBackgroundPrimary() = colorResource(R.color.color_surface_background_primary)
    
    // ========== 边框颜色 ==========
    
    @Composable
    fun borderLinePrimary() = colorResource(R.color.color_border_line_primary)
    
    @Composable
    fun borderLineActive() = colorResource(R.color.color_border_line_active)
}
