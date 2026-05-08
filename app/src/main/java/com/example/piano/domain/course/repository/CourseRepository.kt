package com.example.piano.domain.course.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.course.api.dto.CategoryWithCoursesDTO
import com.example.piano.data.course.api.dto.CourseDetailDTO

/**
 * 课程 Repository 接口
 */
interface CourseRepository {

    /**
     * 获取大模块及对应小模块列表
     */
    suspend fun getCategories(): ResponseState<List<CategoryWithCoursesDTO>>

    /**
     * 获取单个课程详情
     */
    suspend fun getCourseById(courseId: Int): ResponseState<CourseDetailDTO>

    /**
     * 标记课程完成
     */
    suspend fun markCourseComplete(courseId: Int): ResponseState<Unit>
}
