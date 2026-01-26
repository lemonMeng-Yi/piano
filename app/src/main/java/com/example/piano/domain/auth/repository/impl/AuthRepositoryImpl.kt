package com.example.piano.domain.auth.repository.impl

import com.example.piano.core.network.NetworkClient
import com.example.piano.core.network.util.ResponseState
import com.example.piano.core.network.util.toState
import com.example.piano.domain.auth.api.AuthApi
import com.example.piano.domain.auth.api.request.ForgotPasswordRequest
import com.example.piano.domain.auth.api.request.LoginRequest
import com.example.piano.domain.auth.api.request.RegisterRequest
import com.example.piano.domain.auth.api.response.LoginResponse
import com.example.piano.domain.auth.repository.AuthRepository

/**
 * 认证 Repository 实现
 */
class AuthRepositoryImpl : AuthRepository {

    private val authApiService: AuthApi = NetworkClient.createService(AuthApi::class.java)

    override suspend fun login(username: String, password: String): ResponseState<LoginResponse> {
        return authApiService.login(LoginRequest(username, password)).toState()
    }

    override suspend fun register(
        username: String,
        password: String,
        confirmPassword: String
    ): ResponseState<String> {
        // 注意：confirmPassword 仅用于前端验证，不发送到后端
        // 后端接口只需要 username 和 password
        return authApiService.register(RegisterRequest(username, password)).toState()
    }
    
    override suspend fun forgotPassword(
        username: String,
        password: String
    ): ResponseState<String> {
        return authApiService.forgotPassword(ForgotPasswordRequest(username, password)).toState()
    }
    
    override suspend fun logout(): ResponseState<String> {
        return try {
            authApiService.logout().toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }
}
