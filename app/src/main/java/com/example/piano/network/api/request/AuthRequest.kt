package com.example.piano.network.api.request

/**
 * 登录请求
 */
data class LoginRequest(
    val username: String,
    val password: String
)

/**
 * 注册请求
 */
data class RegisterRequest(
    val username: String,
    val password: String,
    val confirmPassword: String
)
