package com.example.piano

import android.app.Application
import com.blankj.utilcode.util.Utils
import com.example.piano.core.manager.TokenManager
import com.example.piano.core.storage.DataStoreManager
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用程序入口类
 * 用于初始化全局组件
 */
@HiltAndroidApp
class PianoApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Utils.init(this)

        // DataStore 版 TokenManager 需在 Application 中初始化
        TokenManager.init(DataStoreManager.getInstance(this))
    }
}

