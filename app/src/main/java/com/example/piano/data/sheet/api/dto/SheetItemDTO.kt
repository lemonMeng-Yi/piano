package com.example.piano.data.sheet.api.dto

import com.google.gson.annotations.SerializedName

/**
 * 曲谱项 DTO
 * 对应后端 SheetItemDTO，GET /sheets 与 GET /sheets/favorites 返回的 data 元素
 */
data class SheetItemDTO(
    @SerializedName("id")
    val id: Long,

    @SerializedName("title")
    val title: String,

    @SerializedName("artist")
    val artist: String,

    @SerializedName("tags")
    val tags: String,

    @SerializedName("previewImageUrl")
    val previewImageUrl: String? = null,

    @SerializedName("sheetDataUrl")
    val sheetDataUrl: String? = null,

    @SerializedName("midiUrl")
    val midiUrl: String? = null,

    @SerializedName("mp3Url")
    val mp3Url: String? = null,

    @SerializedName("favoriteCount")
    val favoriteCount: Int = 0,

    @SerializedName("favorited")
    val favorited: Boolean = false
)
