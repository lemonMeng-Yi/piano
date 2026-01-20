package com.example.piano.network.interceptor

import com.example.piano.network.util.TokenManager
import okhttp3.Interceptor
import okhttp3.Response

/**
 * 认证拦截器
 * 用于在请求头中添加认证信息（如 Token）
 */
class AuthInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // 从 TokenManager 获取 Token
        val token = TokenManager.getToken()
        
        // 如果存在 Token，添加到请求头
        val newRequest = token?.let {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $it")
                .build()
        } ?: originalRequest
        
        return chain.proceed(newRequest)
    }
}
