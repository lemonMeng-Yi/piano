package com.example.piano

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
                    PianoTutorApp()
                }
            }
        }
    }
}

@Composable
fun PianoTutorApp() {
    val navController = rememberNavController()
    var isLoggedIn by remember { mutableStateOf(false) }
    
    if (!isLoggedIn) {
        // 认证导航：管理登录和注册页面
        AuthNavHost(
            navController = navController,
            onLoginSuccess = {
                // TODO: 这里后续添加登录请求
                // 暂时直接登录成功
                isLoggedIn = true
            },
            onRegisterSuccess = {
                // TODO: 这里后续添加注册请求
                // 暂时直接注册成功并登录
                isLoggedIn = true
            }
        )
    } else {
        // 主功能导航：管理应用核心功能页面
        MainNavHost(navController = navController)
    }
}
