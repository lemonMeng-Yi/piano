package com.example.piano.domain.sheet.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.sheet.api.dto.SheetItemDTO

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
     * 曲谱详情（含 sheetDataUrl 等）
     */
    suspend fun getById(id: Long): ResponseState<SheetItemDTO>
}
