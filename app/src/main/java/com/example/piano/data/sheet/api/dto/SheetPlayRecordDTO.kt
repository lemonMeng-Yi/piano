package com.example.piano.data.sheet.api.dto

import com.google.gson.annotations.SerializedName

/**
 * 最近练习记录 DTO
 * 对应后端 GET /sheets/plays/recent 返回的 data 元素
 */
data class SheetPlayRecordDTO(
    @SerializedName("playedAt")
    val playedAt: String,

    @SerializedName("sheet")
    val sheet: SheetItemDTO
)
