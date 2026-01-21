package com.example.piano.data.repository

import com.example.piano.network.api.request.LoginRequest
import com.example.piano.network.api.request.RegisterRequest
import com.example.piano.network.api.response.LoginResponse
import com.example.piano.network.util.ResponseState

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
    
}
