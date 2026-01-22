package com.example.piano.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Snackbar 状态类型
 */
enum class SnackState {
    /** 无图标 */
    Idle,
    /** 错误信息 */
    Error,
    /** 成功信息 */
    Success
}

/**
 * Snackbar 管理器
 * 提供全局统一的提示消息显示功能
 */
object SnackBarManager {
    val snackBarHostState = SnackbarHostState()
    var iconState = SnackState.Idle
    private val scope = CoroutineScope(Dispatchers.Main)

    /**
     * 显示 Snackbar
     * 
     * @param message 消息内容
     * @param duration 显示时长，默认 Short
     * @param state 图标状态，默认 Error
     */
    fun showSnackBar(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        state: SnackState = SnackState.Error
    ) {
        iconState = state
        // 如果已有 Snackbar 显示，先关闭它
        snackBarHostState.currentSnackbarData?.dismiss()
        scope.launch {
            snackBarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }

    /**
     * 显示成功消息
     */
    fun showSuccess(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        showSnackBar(message, duration, SnackState.Success)
    }

    /**
     * 显示错误消息
     */
    fun showError(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        showSnackBar(message, duration, SnackState.Error)
    }

    /**
     * 显示普通消息（无图标）
     */
    fun showInfo(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        showSnackBar(message, duration, SnackState.Idle)
    }
}
