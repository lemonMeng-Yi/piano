package com.example.piano.data.auth.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.core.network.util.toState
import com.example.piano.data.auth.api.AuthApi
import com.example.piano.data.auth.api.request.ForgotPasswordRequest
import com.example.piano.data.auth.api.request.LoginRequest
import com.example.piano.data.auth.api.request.RegisterRequest
import com.example.piano.data.auth.api.request.UpdateProfileRequest
import com.example.piano.data.auth.api.response.LoginResponse
import com.example.piano.data.auth.api.response.ProfileDTO
import com.example.piano.domain.auth.repository.AuthRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

/**
 * 认证 Repository 实现
 */
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi
) : AuthRepository {

    override suspend fun login(username: String, password: String): ResponseState<LoginResponse> {
        return try {
            authApi.login(LoginRequest(username, password)).toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun register(
        username: String,
        password: String,
        confirmPassword: String
    ): ResponseState<String> {
        return try {
            // 注意：confirmPassword 仅用于前端验证，不发送到后端
            // 后端接口只需要 username 和 password
            authApi.register(RegisterRequest(username, password)).toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }
    
    override suspend fun forgotPassword(
        username: String,
        password: String
    ): ResponseState<String> {
        return try {
            authApi.forgotPassword(ForgotPasswordRequest(username, password)).toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }
    
    override suspend fun logout(): ResponseState<String> {
        return try {
            authApi.logout().toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun getProfile(): ResponseState<ProfileDTO> {
        return try {
            authApi.getProfile().toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun updateProfile(
        nickname: String?,
        email: String?,
        phone: String?,
        avatar: String?
    ): ResponseState<String> {
        return try {
            authApi.updateProfile(
                UpdateProfileRequest(
                    nickname = nickname,
                    email = email,
                    phone = phone,
                    avatar = avatar
                )
            ).toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun uploadAvatar(file: java.io.File): ResponseState<String> {
        return try {
            val part = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("image/*".toMediaTypeOrNull())
            )
            when (val r = authApi.uploadAvatar(part).toState()) {
                is ResponseState.Success -> ResponseState.Success(r.body.url)
                is ResponseState.NetworkError -> r
                is ResponseState.UnknownError -> r
            }
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }
}
