package com.example.piano

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.piano.navigation.AuthNavHost
import com.example.piano.navigation.MainNavHost
import com.example.piano.core.network.util.TokenManager
import com.example.piano.ui.components.AppSnackBarHost
import com.example.piano.ui.theme.PianoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // 安装Splash NavRoutes
        installSplashScreen()
        
        super.onCreate(savedInstanceState)
        setContent {
            PianoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PianoTutorApp()
                        // 全局 Snackbar 宿主
                        AppSnackBarHost()
                    }
                }
            }
        }
    }
}

@Composable
fun PianoTutorApp() {
    val navController = rememberNavController()
    // 从 TokenManager 读取已保存的 token 来判断初始登录状态
    var isLoggedIn by remember { mutableStateOf(TokenManager.isLoggedIn()) }
    
    if (!isLoggedIn) {
        // 认证导航：管理登录和注册页面
        AuthNavHost(
            navController = navController,
            onLoginSuccess = {
                // 登录成功，更新登录状态
                isLoggedIn = true
            }
        )
    } else {
        // 主功能导航：管理应用核心功能页面
        MainNavHost(
            navController = navController,
            onLogout = {
                // 退出登录，直接更新状态，界面会自动切换
                isLoggedIn = false
            }
        )
    }
}
