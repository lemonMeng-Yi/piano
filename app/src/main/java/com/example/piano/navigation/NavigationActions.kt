package com.example.piano.navigation

import androidx.navigation.NavController

/**
 * NavigationActions 处理所有导航操作
 * 此类提供统一的导航接口，便于维护和测试
 */
class NavigationActions(private val navController: NavController) {
    
    // ========== 认证相关导航 ==========
    
    /**
     * 导航到登录页面
     */
    fun navigateToLogin() {
        navController.navigate(NavRoutes.LOGIN) {
            // 清除返回栈，避免用户按返回键回到登录页
            popUpTo(NavRoutes.LOGIN) { inclusive = true }
        }
    }
    
    /**
     * 导航到注册页面
     */
    fun navigateToRegister() {
        navController.navigate(NavRoutes.REGISTER)
    }
    
    /**
     * 返回上一页（用于从注册页返回登录页）
     */
    fun navigateUp() {
        navController.navigateUp()
    }
    
    /**
     * 导航到忘记密码页面
     */
    fun navigateToForgotPassword() {
        navController.navigate(NavRoutes.FORGOT_PASSWORD)
    }
    
    // ========== 主功能导航 ==========
    
    /**
     * 导航到首页
     */
    fun navigateToHome() {
        navController.navigate(NavRoutes.HOME) {
            // 清除返回栈，将首页设为根页面
            popUpTo(NavRoutes.HOME) { inclusive = true }
        }
    }
    
    /**
     * 导航到练习页面
     */
    fun navigateToPractice() {
        navController.navigate(NavRoutes.PRACTICE)
    }

    /**
     * 导航到跟弹纠错页面
     */
    fun navigateToPracticeFollowAlong() {
        navController.navigate(NavRoutes.PRACTICE_FOLLOW_ALONG)
    }

    /**
     * 导航到课程页面
     */
    fun navigateToCourses() {
        navController.navigate(NavRoutes.COURSES)
    }

    /**
     * 导航到个人资料页面
     */
    fun navigateToProfile() {
        navController.navigate(NavRoutes.PROFILE)
    }

    /**
     * 导航到编辑个人资料页面
     */
    fun navigateToProfileEdit() {
        navController.navigate(NavRoutes.PROFILE_EDIT)
    }

    /**
     * 导航到课程视频全屏播放页
     */
    fun navigateToCourseVideo(videoUrl: String) {
        val encoded = android.net.Uri.encode(videoUrl)
        navController.navigate("${NavRoutes.COURSE_VIDEO}/$encoded")
    }

    /**
     * 导航到课程详情页（子课时列表）
     */
    fun navigateToCourseDetail(courseId: String) {
        navController.navigate("${NavRoutes.COURSE_DETAIL}/$courseId")
    }

    /**
     * 导航到曲谱详情页（显示 sheetDataUrl 图片）
     */
    fun navigateToSheetDetail(sheetId: Long) {
        navController.navigate("${NavRoutes.SHEET_DETAIL}/$sheetId")
    }

    /**
     * 导航到曲谱虚拟键盘练琴页（按 MIDI 顺序点击琴键比对）
     */
    fun navigateToSheetVirtualPractice(sheetId: Long) {
        navController.navigate("${NavRoutes.SHEET_VIRTUAL_PRACTICE}/$sheetId")
    }
}
