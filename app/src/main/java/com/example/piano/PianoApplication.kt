package com.example.piano

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.example.piano.network.util.TokenManager

/**
 * 应用程序入口类
 * 用于初始化全局组件
 */
class PianoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 utilcodex 工具库
        Utils.init(this)
        
        // 初始化 TokenManager
        TokenManager.init()
    }
}
