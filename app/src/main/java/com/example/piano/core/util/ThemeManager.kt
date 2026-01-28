package com.example.piano.core.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import com.blankj.utilcode.util.SPUtils
import com.example.piano.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CompositionLocal 提供全局主题管理器访问
 */
val LocalThemeManager = staticCompositionLocalOf<ThemeManager> {
    error("请在根组件中通过 CompositionLocalProvider 提供 ThemeManager")
}

private const val APP_THEME_KEY = "app_theme"

/**
 * 主题管理器
 * 管理应用的深色模式状态，并持久化到本地
 */
@Singleton
class ThemeManager @Inject constructor() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val spUtils = SPUtils.getInstance("theme_prefs")
    
    // 主题默认设置为跟随系统
    private val _currentTheme = MutableStateFlow(AppTheme.System)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()
    
    /**
     * 加载保存的主题
     */
    suspend fun loadSavedTheme() {
        try {
            val savedThemeName = spUtils.getString(APP_THEME_KEY, AppTheme.System.name)
            if (savedThemeName.isNotEmpty()) {
                val theme = try {
                    AppTheme.valueOf(savedThemeName)
                } catch (e: IllegalArgumentException) {
                    AppTheme.System
                }
                _currentTheme.value = theme
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 失败则保持默认 System
        }
    }
    
    /**
     * 切换主题
     */
    fun toggleTheme(currentTheme: AppTheme) {
        val newTheme = when (currentTheme) {
            AppTheme.Light -> AppTheme.Dark
            AppTheme.Dark -> AppTheme.Light
            AppTheme.System -> AppTheme.Light
        }
        setTheme(newTheme)
    }
    
    /**
     * 设置主题
     */
    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        persistAsync(theme)
    }
    
    /**
     * 获取实际应用的主题，考虑系统主题
     */
    @Composable
    fun getActualTheme(): AppTheme {
        return when (_currentTheme.collectAsState().value) {
            AppTheme.System -> if (isSystemInDarkTheme()) AppTheme.Dark else AppTheme.Light
            else -> _currentTheme.collectAsState().value
        }
    }
    
    /**
     * 异步持久化主题
     */
    private fun persistAsync(theme: AppTheme) {
        scope.launch {
            try {
                spUtils.put(APP_THEME_KEY, theme.name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
