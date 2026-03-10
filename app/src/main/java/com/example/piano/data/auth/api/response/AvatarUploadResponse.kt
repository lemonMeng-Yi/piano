package com.example.piano.data.auth.api.response

import com.google.gson.annotations.SerializedName

/**
 * 头像上传接口返回的 data
 * POST /users/avatar/upload 返回 Result.success(Map.of("url", url))
 */
data class AvatarUploadResponse(
    @SerializedName("url")
    val url: String
)
