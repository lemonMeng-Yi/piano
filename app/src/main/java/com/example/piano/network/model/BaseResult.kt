package com.example.piano.network.model

import com.google.gson.annotations.SerializedName

/**
 * 统一 API 响应格式
 * 
 * @param T 数据类型
 * @param code 返回码，0 表示成功
 * @param msg 返回信息，"Success" 表示成功
 * @param data 响应数据
 */
data class BaseResult<T>(
    @SerializedName("code")
    val code: Int,
    
    @SerializedName("msg")
    val msg: String,
    
    @SerializedName("data")
    val data: T? = null
)
