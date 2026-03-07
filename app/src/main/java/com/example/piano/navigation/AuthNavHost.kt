package com.example.piano.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.piano.ui.auth.screen.ForgotPasswordPage
import com.example.piano.ui.auth.screen.LoginPage
import com.example.piano.ui.auth.screen.RegisterPage
import com.example.piano.ui.auth.viewmodel.AuthViewModel
import com.example.piano.ui.components.SnackBarManager

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
 */
@Composable
fun AuthNavHost(
    navController: NavHostController,
    onLoginSuccess: () -> Unit
) {
    val navigationActions = NavigationActions(navController)
    
    NavHost(
        navController = navController,
        startDestination = NavRoutes.LOGIN
    ) {
        composable(NavRoutes.LOGIN) {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginPage(
                navigationActions = navigationActions,
                onLoginClick = { username, password ->
                    authViewModel.login(username, password) { success, errorMessage ->
                        if (success) {
                            SnackBarManager.showSuccess("登录成功")
                            onLoginSuccess()
                        } else {
                            // 显示错误提示
                            SnackBarManager.showError(
                                errorMessage ?: "登录失败"
                            )
                        }
                    }
                }
            )
        }
        
        composable(NavRoutes.REGISTER) {
            val authViewModel: AuthViewModel = hiltViewModel()
            RegisterPage(
                navigationActions = navigationActions,
                onRegisterClick = { username, password, confirmPassword ->
                    authViewModel.register(username, password, confirmPassword) { success, errorMessage ->
                        if (success) {
                            // 注册成功后直接跳转到登录页面
                            navigationActions.navigateToLogin()
                            SnackBarManager.showSuccess("注册成功")
                        } else {
                            // 显示错误提示
                            SnackBarManager.showError(
                                errorMessage ?: "注册失败"
                            )
                        }
                    }
                }
            )
        }
        
        composable(NavRoutes.FORGOT_PASSWORD) {
            val authViewModel: AuthViewModel = hiltViewModel()
            ForgotPasswordPage(
                navigationActions = navigationActions,
                onForgotPasswordClick = { username, password, confirmPassword ->
                    authViewModel.forgotPassword(username, password, confirmPassword) { success, errorMessage ->
                        if (success) {
                            // 重置密码成功后跳转到登录页面
                            navigationActions.navigateToLogin()
                            SnackBarManager.showSuccess("密码重置成功")
                        } else {
                            // 显示错误提示
                            SnackBarManager.showError(
                                errorMessage ?: "密码重置失败"
                            )
                        }
                    }
                }
            )
        }
    }
}
