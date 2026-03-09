package com.example.piano.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import com.example.piano.ui.theme.PianoTheme
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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.ui.home.screen.HomePage
import com.example.piano.ui.courses.learn.CourseDetailPage
import com.example.piano.ui.courses.learn.CourseVideoScreen
import com.example.piano.ui.courses.learn.CourseVideoViewModel
import com.example.piano.ui.courses.CoursesPage
import com.example.piano.ui.courses.sheet.SheetDetailEntry
import com.example.piano.ui.courses.sheet.SheetDetailScreen
import com.example.piano.ui.practice.FollowAlongEntry
import com.example.piano.ui.practice.PracticePage
import com.example.piano.ui.profile.ProfilePage

/**
 * 功能级导航 (MainNavHost)
 *
 * 职责：
 * 1. 主要功能页面管理
 *    - 底部导航栏页面（首页、练习、课程、个人资料）
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
            if (currentRoute != NavRoutes.PRACTICE_FOLLOW_ALONG && !currentRoute.orEmpty().startsWith("${NavRoutes.COURSE_VIDEO}/") && !currentRoute.orEmpty().startsWith("${NavRoutes.COURSE_DETAIL}/") && !currentRoute.orEmpty().startsWith("${NavRoutes.SHEET_DETAIL}/")) {
            NavigationBar(
                containerColor = PianoTheme.colors.surface
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
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "课程") },
                    label = { Text("课程") },
                    selected = currentRoute == NavRoutes.COURSES,
                    onClick = { navigationActions.navigateToCourses() }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                    label = { Text("我的") },
                    selected = currentRoute == NavRoutes.PROFILE,
                    onClick = { navigationActions.navigateToProfile() }
                )
            }
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
                    PracticePage(navController = navController)
                }
                composable(NavRoutes.PRACTICE_FOLLOW_ALONG) {
                    FollowAlongEntry(pieceId = null, onBack = { navController.popBackStack() })
                }
                composable(NavRoutes.COURSES) {
                    CoursesPage(
                        onPlayVideo = { navigationActions.navigateToCourseVideo(it) },
                        onOpenCourseDetail = { navigationActions.navigateToCourseDetail(it) },
                        onOpenSheetDetail = { navigationActions.navigateToSheetDetail(it) },
                        onNavigateToLogin = onLogout
                    )
                }
                composable(
                    route = "${NavRoutes.COURSE_DETAIL}/{courseId}",
                    arguments = listOf(navArgument("courseId") { type = NavType.StringType })
                ) { backStackEntry ->
                    CourseDetailPage(
                        onBack = { navController.popBackStack() },
                        onPlayVideo = { navigationActions.navigateToCourseVideo(it) }
                    )
                }
                composable(
                    route = "${NavRoutes.COURSE_VIDEO}/{videoUrl}",
                    arguments = listOf(navArgument("videoUrl") { type = NavType.StringType })
                ) { backStackEntry ->
                    val viewModel: CourseVideoViewModel = hiltViewModel(backStackEntry)
                    CourseVideoScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                }
                composable(
                    route = "${NavRoutes.SHEET_DETAIL}/{sheetId}",
                    arguments = listOf(navArgument("sheetId") { type = NavType.LongType })
                ) { backStackEntry ->
                    SheetDetailEntry(onBack = { navController.popBackStack() }) {
                        SheetDetailScreen(
                            onBack = { navController.popBackStack() },
                            viewModel = hiltViewModel(backStackEntry)
                        )
                    }
                }
                composable(NavRoutes.PROFILE) {
                    ProfilePage(onLogout = onLogout)
                }
            }
        }
    }
}
