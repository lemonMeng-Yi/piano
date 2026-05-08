package com.example.piano.data.course.api.dto

import com.google.gson.annotations.SerializedName

/**
 * 课程详情 DTO
 * 对应后端 GET /course/{id} 返回的 data
 */
data class CourseDetailDTO(
    @SerializedName("courseId")
    val courseId: Int,

    @SerializedName("categoryId")
    val categoryId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("videoUrl")
    val videoUrl: String? = null,

    @SerializedName("sortOrder")
    val sortOrder: Int = 0,

    @SerializedName("isCompleted")
    val isCompleted: Int = 0,

    @SerializedName("completedAt")
    val completedAt: String? = null
)
