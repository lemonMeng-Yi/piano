package com.example.piano.network.api

import com.example.piano.network.api.request.LoginRequest
import com.example.piano.network.api.request.RegisterRequest
import com.example.piano.network.api.response.LoginResponse
import com.example.piano.network.model.BaseResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * 认证相关 API 接口
 */
interface AuthApi {
    
    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @return 登录响应（包含 Token）
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<BaseResult<LoginResponse>>
    
    /**
     * 用户注册
     * 
     * @param request 注册请求
     * @return 注册响应（成功状态）
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<BaseResult<Unit>>
    

}
