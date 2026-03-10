package com.example.piano.domain.auth.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.auth.api.response.LoginResponse
import com.example.piano.data.auth.api.response.ProfileDTO

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

    /**
     * 获取当前用户个人信息（需登录）
     */
    suspend fun getProfile(): ResponseState<ProfileDTO>

    /**
     * 更新当前用户个人信息（昵称、邮箱、手机号、头像 url）
     * 传哪个字段就更新哪个；头像填上传接口返回的 url。后端返回 "修改成功"，成功后需自行拉取 profile 刷新
     */
    suspend fun updateProfile(
        nickname: String?,
        email: String?,
        phone: String?,
        avatar: String? = null
    ): ResponseState<String>

    /**
     * 上传头像到 OSS，返回可访问的 url（需再调用 updateProfile(avatar=url) 写入资料）
     */
    suspend fun uploadAvatar(file: java.io.File): ResponseState<String>
}
