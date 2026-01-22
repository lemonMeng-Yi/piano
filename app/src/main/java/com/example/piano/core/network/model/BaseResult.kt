package com.example.piano.core.network.model

import com.google.gson.annotations.SerializedName

/**
 * 统一 API 响应格式
 * 
 * @param T 数据类型
 * @param code 返回码，200 表示成功
 * @param msg 返回信息
 * @param data 响应数据
 */
data class BaseResult<T>(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("msg")
    val msg: String,
    
    @SerializedName("data")
    val data: T? = null
) {
    /**
     * 判断请求是否成功
     */
    fun isSuccess(): Boolean {
        return code == 200
    }
}
