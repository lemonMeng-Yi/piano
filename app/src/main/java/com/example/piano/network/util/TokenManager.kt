package com.example.piano.network.util

/**
 * Token 管理器
 * 用于管理用户认证 Token
 */
object TokenManager {
    
    @Volatile
    private var token: String? = null
    
    /**
     * 获取当前 Token
     */
    fun getToken(): String? = token
    
    /**
     * 设置 Token
     */
    fun setToken(newToken: String?) {
        token = newToken
    }
    
    /**
     * 清除 Token
     */
    fun clearToken() {
        token = null
    }
    
    /**
     * 判断是否已登录
     */
    fun isLoggedIn(): Boolean = token != null
}
