package com.example.piano.network.config

/**
 * 网络配置常量
 */
object NetworkConfig {
    /**
     * API 基础 URL
     */
    const val BASE_URL = "http://localhost:8080/"
    
    /**
     * 连接超时时间（秒）
     */
    const val CONNECT_TIMEOUT = 30L
    
    /**
     * 读取超时时间（秒）
     */
    const val READ_TIMEOUT = 30L
    
    /**
     * 写入超时时间（秒）
     */
    const val WRITE_TIMEOUT = 30L
}
