package com.example.piano.data.auth.api

import com.example.piano.core.network.model.BaseResult
import com.example.piano.data.auth.api.request.ForgotPasswordRequest
import com.example.piano.data.auth.api.request.LoginRequest
import com.example.piano.data.auth.api.request.RegisterRequest
import com.example.piano.data.auth.api.request.UpdateProfileRequest
import com.example.piano.data.auth.api.response.LoginResponse
import com.example.piano.data.auth.api.response.ProfileDTO
import com.example.piano.data.auth.api.response.AvatarUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part

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
    
    /**
     * 退出登录
     * 路径: users/auth/logout
     * 
     * @return 退出登录响应（成功消息）
     */
    @POST("users/auth/logout")
    suspend fun logout(): Response<BaseResult<String>>

    /**
     * 获取当前登录用户个人信息（需登录）
     * GET /users/profile
     */
    @GET("users/profile")
    suspend fun getProfile(): Response<BaseResult<ProfileDTO>>

    /**
     * 上传头像到 OSS，返回可访问的 HTTP 地址（需登录）
     * POST /users/avatar/upload，前端拿到 url 后再调用 PUT /users/profile/update 将 avatar 设为该 url
     */
    @Multipart
    @POST("users/avatar/upload")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<BaseResult<AvatarUploadResponse>>

    /**
     * 修改当前用户个人信息：传哪个字段就更新哪个，不传的保持不变；头像填上传接口返回的 url
     * PUT /users/profile/update
     */
    @PUT("users/profile/update")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<BaseResult<String>>
}
