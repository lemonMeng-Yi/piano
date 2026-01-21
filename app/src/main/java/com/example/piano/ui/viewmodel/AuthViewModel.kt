package com.example.piano.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.data.repository.AuthRepository
import com.example.piano.network.util.ResponseState
import com.example.piano.network.util.TokenManager
import kotlinx.coroutines.launch

/**
 * 认证 ViewModel
 * 处理登录、注册等认证相关的 UI 逻辑
 */
class AuthViewModel(
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
     * @param confirmPassword 确认密码
     * @param onResult 结果回调 (success: Boolean, errorMessage: String?)
     */
    fun register(
        username: String,
        password: String,
        confirmPassword: String,
        onResult: (Boolean, String?) -> Unit
    ) {
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
}
