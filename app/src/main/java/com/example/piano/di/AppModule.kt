package com.example.piano.di

import com.example.piano.core.network.NetworkClient
import com.example.piano.data.auth.api.AuthApi
import com.example.piano.data.auth.repository.AuthRepositoryImpl
import com.example.piano.domain.auth.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 应用级别的依赖注入模块
 * 提供全局单例依赖
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    
    /**
     * 提供 AuthApi 实例
     */
    companion object {
        @Provides
        @Singleton
        fun provideAuthApi(): AuthApi {
            return NetworkClient.createService(AuthApi::class.java)
        }
    }
    
    /**
     * 绑定 AuthRepository 接口到 AuthRepositoryImpl 实现
     * Hilt 会自动注入 AuthApi 到 AuthRepositoryImpl 的构造函数
     * 
     * 注意：ThemeManager 不需要在这里提供，因为它已经有 @Singleton 和 @Inject 注解
     * Hilt 会自动处理它的依赖注入
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
