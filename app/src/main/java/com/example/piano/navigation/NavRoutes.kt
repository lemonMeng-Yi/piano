package com.example.piano.navigation

/**
 * 定义应用中的导航路由
 * 
 * 路由分为两个层级：
 * 1. 认证路由（Auth Routes）：登录、注册等认证相关页面
 * 2. 主功能路由（Main Routes）：首页、陪练、课程、个人资料等核心功能页面
 */
object NavRoutes {
    // ========== 认证路由 ==========
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    
    // ========== 主功能路由 ==========
    const val HOME = "home"
    const val PRACTICE_FOLLOW_ALONG = "practice_follow_along"
    /** 陪练（曲谱库） */
    const val ACCOMPANY = "accompany"
    const val COURSES = "courses"
    const val PROFILE = "profile"
    const val PROFILE_EDIT = "profile_edit"
    const val PROFILE_PERMISSIONS = "profile_permissions"

    /** 琴音检测（麦克风/蓝牙 MIDI 实时音高显示） */
    const val PITCH_DETECTION = "pitch_detection"

    /** 课程视频全屏播放，路径参数：course_video/{encodedVideoUrl} */
    const val COURSE_VIDEO = "course_video"

    /** 课程详情页（子课时列表），路径参数：course_detail/{courseId} */
    const val COURSE_DETAIL = "course_detail"

    /** 曲谱详情页（sheetDataUrl 图片），路径参数：sheet_detail/{sheetId} */
    const val SHEET_DETAIL = "sheet_detail"

    /** 曲谱虚拟键盘练琴页，路径参数：sheet_virtual_practice/{sheetId} */
    const val SHEET_VIRTUAL_PRACTICE = "sheet_virtual_practice"
}
