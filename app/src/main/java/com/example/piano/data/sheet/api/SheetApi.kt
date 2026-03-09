package com.example.piano.data.sheet.api

import com.example.piano.core.network.model.BaseResult
import com.example.piano.data.sheet.api.dto.SheetItemDTO
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 曲谱库 API
 * 对应后端 SheetController
 */
interface SheetApi {

    /**
     * 乐谱列表（与界面「乐谱」Tab 对应）
     * 未登录可访问；登录后每条带 favorited
     */
    @GET("sheets")
    suspend fun list(): Response<BaseResult<List<SheetItemDTO>>>

    /**
     * 收藏列表（与界面「收藏」Tab 对应）
     * 需要登录，返回当前用户收藏的曲谱
     */
    @GET("sheets/favorites")
    suspend fun favorites(): Response<BaseResult<List<SheetItemDTO>>>

    /**
     * 曲谱详情
     * 未登录可访问；登录后带 favorited
     */
    @GET("sheets/{id}")
    suspend fun getById(@Path("id") id: Long): Response<BaseResult<SheetItemDTO>>

    /**
     * 收藏曲谱（需登录）
     */
    @POST("sheets/{id}/favorite")
    suspend fun addFavorite(@Path("id") id: Long): Response<BaseResult<Unit>>

    /**
     * 取消收藏曲谱（需登录）
     * DELETE /sheets/{id}/favorite/cancel
     */
    @DELETE("sheets/{id}/favorite/cancel")
    suspend fun removeFavorite(@Path("id") id: Long): Response<BaseResult<Unit>>
}
