package com.example.piano.domain.sheet.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.sheet.api.dto.SheetItemDTO
import com.example.piano.data.sheet.api.dto.SheetPlayRecordDTO

/**
 * 曲谱库 Repository 接口
 */
interface SheetRepository {

    /**
     * 乐谱列表（未登录可访问）
     */
    suspend fun list(): ResponseState<List<SheetItemDTO>>

    /**
     * 收藏列表（需登录，未登录返回 401）
     */
    suspend fun listFavorites(): ResponseState<List<SheetItemDTO>>

    /**
     * 曲谱详情（含 sheetDataUrl、mp3Url、favorited 等）
     */
    suspend fun getById(id: Long): ResponseState<SheetItemDTO>

    /**
     * 收藏曲谱（需登录）
     */
    suspend fun addFavorite(id: Long): ResponseState<Unit>

    /**
     * 取消收藏曲谱（需登录）
     */
    suspend fun removeFavorite(id: Long): ResponseState<Unit>

    /**
     * 记录播放/练习（用户练习某曲谱时调用，需登录）
     */
    suspend fun recordPlay(id: Long): ResponseState<Unit>

    /**
     * 最近练习记录（需登录），按播放时间倒序
     */
    suspend fun listRecentPlays(limit: Int = 20): ResponseState<List<SheetPlayRecordDTO>>
}
