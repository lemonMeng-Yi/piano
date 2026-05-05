package com.example.piano.data.evaluation.api.dto

import com.google.gson.annotations.SerializedName

/**
 * AI 测评响应数据
 */
data class EvaluationResponse(
    @SerializedName("totalScore")
    val totalScore: Int,

    @SerializedName("comment")
    val comment: String,

    @SerializedName("suggestion")
    val suggestion: String
)
