package com.example.piano.domain.auth.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.auth.api.response.LoginResponse

/**
 * 认证 Repository 接口
 */
interface AuthRepository {
    /**
     * 用户登录
     */
    suspend fun login(username: String, password: String): ResponseState<LoginResponse>
    
    /**
     * 用户注册
     */
    suspend fun register(
        username: String,
        password: String,
        confirmPassword: String
    ): ResponseState<String>
    
    /**
     * 忘记密码
     */
    suspend fun forgotPassword(
        username: String,
        password: String
    ): ResponseState<String>
    
    /**
     * 退出登录
     */
    suspend fun logout(): ResponseState<String>
}
