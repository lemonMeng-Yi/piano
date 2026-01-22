package com.example.piano.domain.auth.api

import com.example.piano.core.network.model.BaseResult
import com.example.piano.domain.auth.api.request.ForgotPasswordRequest
import com.example.piano.domain.auth.api.request.LoginRequest
import com.example.piano.domain.auth.api.request.RegisterRequest
import com.example.piano.domain.auth.api.response.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 认证相关 API 接口
 */
interface AuthApi {
    
    /**
     * 用户登录
     * 路径: users/auth/login (参见 ApiPaths.LOGIN)
     * 
     * @param request 登录请求
     * @return 登录响应（包含 Token）
     */
    @POST("users/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<BaseResult<LoginResponse>>
    
    /**
     * 用户注册
     * 路径: users/auth/register (参见 ApiPaths.REGISTER)
     * 
     * @param request 注册请求
     * @return 注册响应（成功状态）
     */
    @POST("users/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResult<String>>
    
    /**
     * 忘记密码
     * 路径: users/auth/forgot-password (参见 ApiPaths.FORGOT_PASSWORD)
     * 
     * @param request 忘记密码请求
     * @return 忘记密码响应（成功消息）
     */
    @POST("users/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<BaseResult<String>>
}
