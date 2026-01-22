package com.example.piano.domain.auth.api

/**
 * API 路径常量
 * 统一管理所有 API 请求路径
 */
object ApiPaths {
    
    // ========== 认证相关 ==========
    
    /** 用户登录 */
    const val LOGIN = "users/auth/login"
    
    /** 用户注册 */
    const val REGISTER = "users/auth/register"
    
    /** 忘记密码 */
    const val FORGOT_PASSWORD = "users/auth/forgot-password"

}
