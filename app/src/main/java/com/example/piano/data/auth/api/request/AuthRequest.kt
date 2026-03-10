package com.example.piano.data.auth.api.request

/**
 * 登录请求
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * 注册请求
 * 根据接口文档，只需要 username 和 password
 */
data class RegisterRequest(
    val username: String,
    val password: String
)

/**
 * 忘记密码请求
 */
data class ForgotPasswordRequest(
    val username: String,
    val password: String
)

/**
 * 更新个人信息请求（传哪个字段就更新哪个，不传保持不变）
 * 对应后端 UpdateProfileDTO
 */
data class UpdateProfileRequest(
    val nickname: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val avatar: String? = null
)
