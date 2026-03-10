package com.example.piano.data.auth.api.response

import com.google.gson.annotations.SerializedName

/**
 * 个人信息 DTO
 * 对应接口 GET /profile 返回的 data 结构
 */
data class ProfileDTO(
    @SerializedName("username")
    val username: String? = null,

    /** 昵称（若后端未单独返回则用 username 展示） */
    @SerializedName("nickname")
    val nickname: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("avatar")
    val avatar: String? = null
) {
    /** 展示用名称：优先昵称，否则用户名 */
    fun displayName(): String = nickname?.takeIf { it.isNotBlank() } ?: username ?: ""
}
