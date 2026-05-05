package com.example.piano.domain.evaluation.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.evaluation.api.dto.EvaluationRequest
import com.example.piano.data.evaluation.api.dto.EvaluationResponse

/**
 * AI 测评 Repository 接口
 */
interface EvaluationRepository {

    /**
     * 提交弹奏结果，获取 AI 测评反馈
     */
    suspend fun evaluate(request: EvaluationRequest): ResponseState<EvaluationResponse>
}
