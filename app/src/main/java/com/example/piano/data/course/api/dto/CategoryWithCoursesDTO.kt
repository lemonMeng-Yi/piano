package com.example.piano.data.course.api.dto

import com.google.gson.annotations.SerializedName

/**
 * 大模块及对应小模块 DTO
 * 对应后端 GET /course/categories 返回的 data 中单个元素
 */
data class CategoryWithCoursesDTO(
    @SerializedName("categoryId")
    val categoryId: Int,

    @SerializedName("categoryName")
    val categoryName: String,

    @SerializedName("courses")
    val courses: List<CourseItemDTO> = emptyList()
)
