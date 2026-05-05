package com.example.piano.data.evaluation.api.dto

import com.google.gson.annotations.SerializedName

/**
 * AI 测评请求体
 */
data class EvaluationRequest(
    @SerializedName("sheetId")
    val sheetId: Long,

    @SerializedName("sheetTitle")
    val sheetTitle: String,

    @SerializedName("stats")
    val stats: EvaluationStats
)

/**
 * 弹奏统计数据
 */
data class EvaluationStats(
    @SerializedName("wrongNoteCount")
    val wrongNoteCount: Int,

    @SerializedName("missedNoteCount")
    val missedNoteCount: Int,

    @SerializedName("playedNoteCount")
    val playedNoteCount: Int,

    @SerializedName("totalNoteCount")
    val totalNoteCount: Int,

    @SerializedName("accuracyPercent")
    val accuracyPercent: Int,

    @SerializedName("wrongNotes")
    val wrongNotes: List<WrongNoteDetail>
)

/**
 * 单个错音明细
 */
data class WrongNoteDetail(
    @SerializedName("position")
    val position: Int,

    @SerializedName("expected")
    val expected: String,

    @SerializedName("actual")
    val actual: String
)
