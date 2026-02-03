package com.example.piano.ui.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.LogUtils
import com.example.piano.core.network.util.ResponseState
import com.example.piano.core.manager.TokenManager
import com.example.piano.domain.auth.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 认证 ViewModel
 * 处理登录、注册等认证相关的 UI 逻辑
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    /**
     * 登录
     * 
     * @param username 用户名
     * @param password 密码
     * @param onResult 结果回调 (success: Boolean, errorMessage: String?)
     */
    fun login(
        username: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            when (val result = authRepository.login(username, password)) {
                is ResponseState.Success -> {
                    // 登录成功，保存 Token
                    TokenManager.setToken(result.body.token)
                    LogUtils.d("登录成功，Token: ${result.body.token}")
                    onResult(true, null)
                }
                is ResponseState.NetworkError -> {
                    // 网络错误或业务错误
                    onResult(false, result.msg)
                }
                is ResponseState.UnknownError -> {
                    // 未知错误
                    onResult(false, result.throwable?.message ?: "未知错误")
                }
            }
        }
    }
    
    /**
     * 注册
     * 
     * @param username 用户名
     * @param password 密码
     * @param confirmPassword 确认密码（仅用于前端验证，不发送到后端）
     * @param onResult 结果回调 (success: Boolean, errorMessage: String?)
     */
    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // 前端验证：检查密码是否一致
        if (password != confirmPassword) {
            onResult(false, "两次输入的密码不一致")
            return
        }
        
        // 验证用户名和密码不能为空
        if (username.isBlank()) {
            onResult(false, "用户名不能为空")
            return
        }
        
        if (password.isBlank()) {
            onResult(false, "密码不能为空")
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.register(username, password, confirmPassword)) {
                is ResponseState.Success -> {
                    // 注册成功
                    onResult(true, null)
                }
                is ResponseState.NetworkError -> {
                    // 网络错误或业务错误
                    onResult(false, result.msg)
                }
                is ResponseState.UnknownError -> {
                    // 未知错误
                    onResult(false, result.throwable?.message ?: "未知错误")
                }
            }
        }
    }
    
    /**
     * 忘记密码
     * 
     * @param username 用户名
     * @param password 新密码
     * @param confirmPassword 确认密码（仅用于前端验证，不发送到后端）
     * @param onResult 结果回调 (success: Boolean, errorMessage: String?)
     */
    fun forgotPassword(
        username: String,
        password: String,
        confirmPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        // 前端验证：检查密码是否一致
        if (password != confirmPassword) {
            onResult(false, "两次输入的密码不一致")
            return
        }
        
        // 验证用户名和密码不能为空
        if (username.isBlank()) {
            onResult(false, "用户名不能为空")
            return
        }
        
        if (password.isBlank()) {
            onResult(false, "密码不能为空")
            return
        }
        
        viewModelScope.launch {
            when (val result = authRepository.forgotPassword(username, password)) {
                is ResponseState.Success -> {
                    // 忘记密码成功
                    onResult(true, null)
                }
                is ResponseState.NetworkError -> {
                    // 网络错误或业务错误
                    onResult(false, result.msg)
                }
                is ResponseState.UnknownError -> {
                    // 未知错误
                    onResult(false, result.throwable?.message ?: "未知错误")
                }
            }
        }
    }
    
    /**
     * 退出登录
     * 
     * @param onResult 结果回调 (success: Boolean, errorMessage: String?)
     */
    fun logout(onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = authRepository.logout()) {
                is ResponseState.Success -> {
                    // 退出登录成功，清除本地 Token
                    TokenManager.clearToken()
                    LogUtils.d("退出登录成功")
                    onResult(true, null)
                }
                is ResponseState.NetworkError -> {
                    // 网络错误或业务错误，即使失败也清除本地 Token
                    TokenManager.clearToken()
                    LogUtils.w("退出登录失败，但已清除本地 Token: ${result.msg}")
                    onResult(false, result.msg)
                }
                is ResponseState.UnknownError -> {
                    // 未知错误，即使失败也清除本地 Token
                    TokenManager.clearToken()
                    LogUtils.w("退出登录异常，但已清除本地 Token: ${result.throwable?.message}")
                    onResult(false, result.throwable?.message ?: "未知错误")
                }
            }
        }
    }
}
