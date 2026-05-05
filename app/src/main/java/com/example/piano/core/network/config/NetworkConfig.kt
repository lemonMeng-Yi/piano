package com.example.piano.core.network.config

/**
 * 网络配置常量
 */
object NetworkConfig {
    /**
     * API 基础 URL
     */
    const val BASE_URL = "http://192.168.13.102:8080/"
    
    /**
     * 连接超时时间（秒）
     */
    const val CONNECT_TIMEOUT = 60L
    
    /**
     * 读取超时时间（秒）
     */
    const val READ_TIMEOUT = 60L
    
    /**
     * 写入超时时间（秒）
     */
    const val WRITE_TIMEOUT = 60L
}
