package com.example.piano.data.course.api

import com.example.piano.core.network.model.BaseResult
import com.example.piano.data.course.api.dto.CategoryWithCoursesDTO
import com.example.piano.data.course.api.dto.CourseDetailDTO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * 课程模块 API
 * 对应后端 CourseController
 */
interface CourseApi {

    /**
     * 获取大模块及对应小模块名称
     * GET /course/categories
     */
    @GET("course/categories")
    suspend fun listCategoriesWithCourses(): Response<BaseResult<List<CategoryWithCoursesDTO>>>

    /**
     * 获取单个课程详情
     * GET /course/{id}
     */
    @GET("course/{id}")
    suspend fun getCourseById(@Path("id") id: Int): Response<BaseResult<CourseDetailDTO>>

    /**
     * 标记课程完成
     * POST /course/{id}/complete
     */
    @POST("course/{id}/complete")
    suspend fun markComplete(@Path("id") id: Int): Response<BaseResult<Any>>
}
