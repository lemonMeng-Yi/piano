package com.example.piano.data.evaluation.api

import com.example.piano.core.network.model.BaseResult
import com.example.piano.data.evaluation.api.dto.EvaluationRequest
import com.example.piano.data.evaluation.api.dto.EvaluationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * AI 测评 API 接口
 */
interface EvaluationApi {

    /**
     * 提交弹奏结果，获取 AI 测评反馈
     * POST /evaluation
     */
    @POST("evaluation")
    suspend fun evaluate(@Body request: EvaluationRequest): Response<BaseResult<EvaluationResponse>>
}
