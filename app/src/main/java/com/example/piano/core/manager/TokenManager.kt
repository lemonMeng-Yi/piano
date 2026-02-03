package com.example.piano.core.manager

import com.example.piano.core.storage.DataStoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Token 管理器
 * 用于管理用户认证 Token，使用 DataStore 持久化，内存缓存供拦截器等同步场景使用。
 */
object TokenManager {

    private const val KEY_TOKEN = "token"
    private const val KEY_TOKEN_TIMESTAMP = "authTokenTimestamp"

    @Volatile
    private var dataStoreManager: DataStoreManager? = null

    @Volatile
    private var cachedToken: String? = null

    @Volatile
    private var cachedTimestamp: Long = 0L

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 初始化 TokenManager，需在 Application 中调用。
     */
    fun init(manager: DataStoreManager) {
        if (dataStoreManager != null) return
        dataStoreManager = manager
        runBlocking {
            loadCacheFromDataStore()
        }
    }

    private suspend fun loadCacheFromDataStore() {
        val dm = dataStoreManager ?: return
        cachedToken = dm.getString(KEY_TOKEN, "").first().takeIf { it.isNotEmpty() }
        cachedTimestamp = dm.getLong(KEY_TOKEN_TIMESTAMP, 0L).first()
    }

    /**
     * 获取当前 Token（同步，供拦截器等使用）。若已过期会清除并返回 null。
     */
    fun getToken(): String? {
        if (dataStoreManager == null) return null
        if (cachedToken != null && isTokenExpired()) {
            scope.launch { clearToken() }
            cachedToken = null
            cachedTimestamp = 0L
            return null
        }
        return cachedToken
    }

    /**
     * 设置 Token（挂起，在协程中调用）。
     */
    suspend fun setToken(newToken: String?) {
        val dm = dataStoreManager ?: return
        if (newToken != null) {
            dm.setString(KEY_TOKEN, newToken)
            dm.setLong(KEY_TOKEN_TIMESTAMP, System.currentTimeMillis())
            cachedToken = newToken
            cachedTimestamp = System.currentTimeMillis()
        } else {
            clearToken()
        }
    }

    /**
     * 清除 Token（挂起，在协程中调用）。
     */
    suspend fun clearToken() {
        val dm = dataStoreManager ?: return
        dm.removeString(KEY_TOKEN)
        dm.removeLong(KEY_TOKEN_TIMESTAMP)
        cachedToken = null
        cachedTimestamp = 0L
    }

    fun isLoggedIn(): Boolean = getToken() != null

    fun getTokenTimestamp(): Long = cachedTimestamp

    fun isTokenExpired(expireTimeMillis: Long = 7 * 24 * 60 * 60 * 1000L): Boolean {
        if (cachedTimestamp == 0L) return true
        return (System.currentTimeMillis() - cachedTimestamp) > expireTimeMillis
    }
}
