package com.example.piano.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pianotutor.ui.screens.HomePage
import com.example.pianotutor.ui.screens.PracticePage
import com.example.pianotutor.ui.screens.ProfilePage
import com.example.pianotutor.ui.screens.ProgressPage

/**
 * 功能级导航 (MainNavHost)
 *
 * 职责：
 * 1. 主要功能页面管理
 *    - 底部导航栏页面（首页、练习、进度、个人资料）
 *    - 这些页面共享同一个底部导航栏，形成主要的用户交互区域
 *
 * 2. 功能级页面跳转
 *    - 负责应用核心功能的页面组织和导航
 *    - 这些页面不显示底部导航栏，提供独立的用户体验
 *    - 作为AppNavHost的子导航
 *
 * 注意：这是应用主功能区域的导航，在用户通过引导页和广告页后，
 * 负责管理用户日常使用的所有功能页面
 *
 * @param navController 导航控制器，用于管理页面跳转
 * @param onLogout 退出登录回调，用于更新登录状态
 * @param modifier 修饰符，用于自定义布局
 */
@Composable
fun MainNavHost(
    navController: NavHostController,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navigationActions = NavigationActions(navController)
    
    // 获取当前路由，用于底部导航栏选中状态
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                    label = { Text("首页") },
                    selected = currentRoute == NavRoutes.HOME,
                    onClick = { navigationActions.navigateToHome() }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.MusicNote, contentDescription = "练习") },
                    label = { Text("练习") },
                    selected = currentRoute == NavRoutes.PRACTICE,
                    onClick = { navigationActions.navigateToPractice() }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "进度") },
                    label = { Text("进度") },
                    selected = currentRoute == NavRoutes.PROGRESS,
                    onClick = { navigationActions.navigateToProgress() }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                    label = { Text("我的") },
                    selected = currentRoute == NavRoutes.PROFILE,
                    onClick = { navigationActions.navigateToProfile() }
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.HOME
            ) {
                composable(NavRoutes.HOME) {
                    HomePage()
                }
                
                composable(NavRoutes.PRACTICE) {
                    PracticePage()
                }
                
                composable(NavRoutes.PROGRESS) {
                    ProgressPage()
                }
                
                composable(NavRoutes.PROFILE) {
                    ProfilePage(onLogout = onLogout)
                }
            }
        }
    }
}
