package com.example.piano.data.course.api

import com.example.piano.core.network.model.BaseResult
import com.example.piano.data.course.api.dto.CategoryWithCoursesDTO
import retrofit2.Response
import retrofit2.http.GET

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
}
