package com.example.piano.core.network.interceptor

import com.example.piano.core.manager.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 认证拦截器
 * 用于在请求头中添加认证信息（如 Token）
 */
class AuthInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 从 TokenManager 获取 Token（会自动检查是否过期）
        val token = TokenManager.getToken()
        
        // 如果存在且未过期的 Token，添加到请求头
        val newRequest = if (token != null && !TokenManager.isTokenExpired()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }
        
        return chain.proceed(newRequest)
    }
}
