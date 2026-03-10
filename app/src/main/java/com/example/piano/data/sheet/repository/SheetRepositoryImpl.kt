package com.example.piano.data.sheet.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.core.network.util.toState
import com.example.piano.core.network.util.toStateUnit
import com.example.piano.data.sheet.api.SheetApi
import com.example.piano.data.sheet.api.dto.SheetItemDTO
import com.example.piano.data.sheet.api.dto.SheetPlayRecordDTO
import com.example.piano.domain.sheet.repository.SheetRepository
import javax.inject.Inject

/**
 * 曲谱库 Repository 实现
 */
class SheetRepositoryImpl @Inject constructor(
    private val sheetApi: SheetApi
) : SheetRepository {

    override suspend fun list(): ResponseState<List<SheetItemDTO>> {
        return try {
            sheetApi.list().toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun listFavorites(): ResponseState<List<SheetItemDTO>> {
        return try {
            sheetApi.favorites().toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun getById(id: Long): ResponseState<SheetItemDTO> {
        return try {
            sheetApi.getById(id).toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun addFavorite(id: Long): ResponseState<Unit> {
        return try {
            sheetApi.addFavorite(id).toStateUnit()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun removeFavorite(id: Long): ResponseState<Unit> {
        return try {
            sheetApi.removeFavorite(id).toStateUnit()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun recordPlay(id: Long): ResponseState<Unit> {
        return try {
            sheetApi.recordPlay(id).toStateUnit()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }

    override suspend fun listRecentPlays(limit: Int): ResponseState<List<SheetPlayRecordDTO>> {
        return try {
            sheetApi.recentPlays(limit).toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }
}
