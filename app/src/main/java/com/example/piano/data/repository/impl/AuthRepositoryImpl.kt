package com.example.piano.data.repository.impl

import com.example.piano.data.repository.AuthRepository
import com.example.piano.network.NetworkClient
import com.example.piano.network.api.request.LoginRequest
import com.example.piano.network.api.request.RegisterRequest
import com.example.piano.network.api.response.LoginResponse
import com.example.piano.network.util.ResponseState
import com.example.piano.network.util.toState

/**
 * 认证 Repository 实现
 */
class AuthRepositoryImpl : AuthRepository {

    private val authApiService = NetworkClient.authApiService

    override suspend fun login(username: String, password: String): ResponseState<LoginResponse> {
        return authApiService.login(LoginRequest(username, password)).toState()
    }

    override suspend fun register(
        username: String,
        password: String,
        confirmPassword: String
    ): ResponseState<Unit> {
        return authApiService.register(RegisterRequest(username, password, confirmPassword)).toState()
    }
}