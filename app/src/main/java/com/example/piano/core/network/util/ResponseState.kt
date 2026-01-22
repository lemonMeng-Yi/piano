package com.example.piano.core.network.util

import com.example.piano.core.network.model.BaseResult
import retrofit2.Response

/**
 * 网络请求响应状态
 * 
 * @param T 数据类型
 */
sealed class ResponseState<out T : Any> {
    /**
     * 成功
     */
    data class Success<T : Any>(val body: T) : ResponseState<T>()
    
    /**
     * 网络错误（HTTP 错误码或业务错误码）
     */
    data class NetworkError(val code: Int, val msg: String) : ResponseState<Nothing>()
    
    /**
     * 未知错误
     */
    data class UnknownError(val throwable: Throwable?) : ResponseState<Nothing>()
}

/**
 * 将 Retrofit Response<BaseResult<T>> 转换为 ResponseState<T>
 * 处理统一响应格式 {code, msg, data}
 * 
 * @param T 实际数据类型
 * @return ResponseState
 */
fun <T : Any> Response<BaseResult<T>>.toState(): ResponseState<T> {
    return if (this.isSuccessful) {
        val baseResult = this.body()
        if (baseResult == null) {
            ResponseState.UnknownError(Exception("响应体为空"))
        } else if (baseResult.isSuccess()) {
            val data = baseResult.data
            if (data == null) {
                ResponseState.UnknownError(Exception("数据为空"))
            } else {
                ResponseState.Success(data)
            }
        } else {
            // 业务错误（code != 0 或 msg != "Success"）
            ResponseState.NetworkError(baseResult.code, baseResult.msg)
        }
    } else {
        // HTTP 错误
        val code = this.code()
        val errorBody = this.errorBody()
        
        if (errorBody != null) {
            // 可以根据业务需求调整特殊错误码的处理
            if (code == 401) {
                // 未授权，可能需要重新登录
                ResponseState.NetworkError(code, "未授权，请重新登录")
            } else {
                ResponseState.NetworkError(code, errorBody.toString())
            }
        } else {
            ResponseState.UnknownError(null)
        }
    }
}
