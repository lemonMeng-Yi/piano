package com.example.piano.core.manager

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.piano.core.storage.DataStoreManager
import com.example.piano.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
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
 * 管理应用的深色模式状态，使用 DataStore 持久化。
 */
@Singleton
class ThemeManager @Inject constructor(
    private val dataStoreManager: DataStoreManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentTheme = MutableStateFlow(AppTheme.System)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    suspend fun loadSavedTheme() {
        try {
            val savedThemeName = dataStoreManager.getString(APP_THEME_KEY, AppTheme.System.name).first()
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
        }
    }

    fun toggleTheme(currentTheme: AppTheme) {
        val newTheme = when (currentTheme) {
            AppTheme.Light -> AppTheme.Dark
            AppTheme.Dark -> AppTheme.Light
            AppTheme.System -> AppTheme.Light
        }
        setTheme(newTheme)
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        persistAsync(theme)
    }

    @Composable
    fun getActualTheme(): AppTheme {
        return when (_currentTheme.collectAsState().value) {
            AppTheme.System -> if (isSystemInDarkTheme()) AppTheme.Dark else AppTheme.Light
            else -> _currentTheme.collectAsState().value
        }
    }

    private fun persistAsync(theme: AppTheme) {
        scope.launch {
            try {
                dataStoreManager.setString(APP_THEME_KEY, theme.name)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
