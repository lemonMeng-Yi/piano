package com.example.piano.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pianotutor.ui.screens.LoginPage
import com.example.pianotutor.ui.screens.RegisterPage

/**
 * 认证导航 (AuthNavHost)
 *
 * 职责：
 * 1. 管理认证相关页面
 *    - 登录页面
 *    - 注册页面
 *
 * 2. 认证流程导航
 *    - 处理登录和注册之间的页面跳转
 *    - 作为 AppNavHost 的子导航
 *
 * 注意：这是应用认证流程的导航，在用户未登录时显示，
 * 负责管理用户登录和注册的所有页面
 *
 * @param navController 导航控制器，用于管理页面跳转
 * @param onLoginSuccess 登录成功回调
 * @param onRegisterSuccess 注册成功回调
 */
@Composable
fun AuthNavHost(
    navController: NavHostController,
    onLoginSuccess: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val navigationActions = NavigationActions(navController)
    
    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOGIN
    ) {
        composable(NavRoutes.LOGIN) {
            LoginPage(
                navigationActions = navigationActions,
                onLoginClick = { username, password ->
                    // TODO: 这里后续添加登录请求
                    // 暂时直接登录成功
                    onLoginSuccess()
                }
            )
        }
        
        composable(NavRoutes.REGISTER) {
            RegisterPage(
                navigationActions = navigationActions,
                onRegisterClick = { username, password, confirmPassword ->
                    // TODO: 这里后续添加注册请求
                    // 暂时直接注册成功并登录
                    onRegisterSuccess()
                }
            )
        }
    }
}
