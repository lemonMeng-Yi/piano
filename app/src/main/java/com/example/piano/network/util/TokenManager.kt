package com.example.piano.network.util

import com.blankj.utilcode.util.SPUtils

/**
 * Token 管理器
 * 用于管理用户认证 Token，支持持久化存储
 * 使用 utilcodex 的 SPUtils 简化 SharedPreferences 操作
 */
object TokenManager {
    
    private const val KEY_TOKEN = "token"
    private const val KEY_TOKEN_TIMESTAMP = "authTokenTimestamp"
    
    /**
     * 初始化 TokenManager
     * 在 Application.onCreate() 中调用，确保 Utils.init() 已执行
     */
    fun init() {
        // SPUtils 已通过 Utils.init() 初始化，无需额外操作
        // 这里可以添加其他初始化逻辑
    }
    
    /**
     * 获取当前 Token
     * 如果 Token 已过期，会自动清除并返回 null
     */
    fun getToken(): String? {
        val token = SPUtils.getInstance().getString(KEY_TOKEN, null)
        
        // 如果 token 存在但已过期，清除它
        if (token != null && isTokenExpired()) {
            clearToken()
            return null
        }
        
        return token
    }
    
    /**
     * 设置 Token
     * 同时保存 Token 和时间戳
     * 
     * @param newToken 新的 Token，如果为 null 则清除 Token
     */
    fun setToken(newToken: String?) {
        if (newToken != null) {
            SPUtils.getInstance().put(KEY_TOKEN, newToken)
            SPUtils.getInstance().put(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis())
        } else {
            clearToken()
        }
    }
    
    /**
     * 清除 Token
     * 同时清除 Token 和时间戳
     */
    fun clearToken() {
        SPUtils.getInstance().remove(KEY_TOKEN)
        SPUtils.getInstance().remove(KEY_TOKEN_TIMESTAMP)
    }
    
    /**
     * 判断是否已登录
     * 会检查 Token 是否过期，过期则返回 false
     * 注意：getToken() 内部已经检查过期并清除，所以这里直接判断 token 是否存在即可
     */
    fun isLoggedIn(): Boolean {
        return getToken() != null
    }
    
    /**
     * 获取 Token 保存的时间戳
     * 可用于判断 Token 是否过期
     */
    fun getTokenTimestamp(): Long {
        return SPUtils.getInstance().getLong(KEY_TOKEN_TIMESTAMP, 0L)
    }
    
    /**
     * 判断 Token 是否过期
     * 
     * @param expireTimeMillis Token 有效期（毫秒），例如 7 天 = 7 * 24 * 60 * 60 * 1000
     * @return true 表示已过期，false 表示未过期
     */
    // todo token有效期暂时设为1s
    fun isTokenExpired(expireTimeMillis: Long = 1000L): Boolean {
        val timestamp = getTokenTimestamp()
        if (timestamp == 0L) return true // 没有时间戳，视为过期
        
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - timestamp
        
        return elapsedTime > expireTimeMillis
    }
}
