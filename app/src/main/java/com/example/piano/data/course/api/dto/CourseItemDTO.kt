package com.example.piano.data.course.api.dto

import com.google.gson.annotations.SerializedName

/**
 * 小模块（课时）DTO
 * 对应后端 CategoryWithCoursesDTO.courses 中的元素
 */
data class CourseItemDTO(
    @SerializedName("courseId")
    val courseId: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("videoUrl")
    val videoUrl: String? = null
)
