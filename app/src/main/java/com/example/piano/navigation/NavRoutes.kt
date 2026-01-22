package com.example.piano.navigation

/**
 * 定义应用中的导航路由
 * 
 * 路由分为两个层级：
 * 1. 认证路由（Auth Routes）：登录、注册等认证相关页面
 * 2. 主功能路由（Main Routes）：首页、练习、进度、个人资料等核心功能页面
 */
object NavRoutes {
    // ========== 认证路由 ==========
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    
    // ========== 主功能路由 ==========
    const val HOME = "home"
    const val PRACTICE = "practice"
    const val PROGRESS = "progress"
    const val PROFILE = "profile"
}
