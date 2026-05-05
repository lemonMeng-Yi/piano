package com.example.piano.data.evaluation.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.core.network.util.toState
import com.example.piano.data.evaluation.api.EvaluationApi
import com.example.piano.data.evaluation.api.dto.EvaluationRequest
import com.example.piano.data.evaluation.api.dto.EvaluationResponse
import com.example.piano.domain.evaluation.repository.EvaluationRepository
import javax.inject.Inject

/**
 * AI 测评 Repository 实现
 */
class EvaluationRepositoryImpl @Inject constructor(
    private val evaluationApi: EvaluationApi
) : EvaluationRepository {

    override suspend fun evaluate(request: EvaluationRequest): ResponseState<EvaluationResponse> {
        return try {
            evaluationApi.evaluate(request).toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }
}
