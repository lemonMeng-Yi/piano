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
 * 根据接口文档，只需要 username 和 password
 */
data class RegisterRequest(
    val username: String,
    val password: String
)
