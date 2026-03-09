package com.example.piano.di

import android.content.Context
import com.example.piano.core.network.NetworkClient
import com.example.piano.core.storage.DataStoreManager
import com.example.piano.data.auth.api.AuthApi
import com.example.piano.data.auth.repository.AuthRepositoryImpl
import com.example.piano.data.course.api.CourseApi
import com.example.piano.data.course.repository.CourseRepositoryImpl
import com.example.piano.data.sheet.api.SheetApi
import com.example.piano.data.sheet.repository.SheetRepositoryImpl
import com.example.piano.domain.auth.repository.AuthRepository
import com.example.piano.domain.course.repository.CourseRepository
import com.example.piano.domain.sheet.repository.SheetRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        fun provideDataStoreManager(@ApplicationContext context: Context): DataStoreManager {
            return DataStoreManager.getInstance(context)
        }

        @Provides
        @Singleton
        fun provideAuthApi(): AuthApi {
            return NetworkClient.createService(AuthApi::class.java)
        }

        @Provides
        @Singleton
        fun provideCourseApi(): CourseApi {
            return NetworkClient.createService(CourseApi::class.java)
        }

        @Provides
        @Singleton
        fun provideSheetApi(): SheetApi {
            return NetworkClient.createService(SheetApi::class.java)
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

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        courseRepositoryImpl: CourseRepositoryImpl
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindSheetRepository(
        sheetRepositoryImpl: SheetRepositoryImpl
    ): SheetRepository
}
