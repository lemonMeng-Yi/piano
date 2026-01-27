package com.example.piano

import android.app.Application
import com.blankj.utilcode.util.Utils
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用程序入口类
 * 用于初始化全局组件
 */
@HiltAndroidApp
class PianoApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // 初始化 utilcodex 工具库
        // 如果注释掉，SPUtils 会在首次使用时自动初始化，但可能在某些场景下出现问题
        Utils.init(this)
    }
}
