package com.example.piano.data.course.repository

import com.example.piano.core.network.util.ResponseState
import com.example.piano.core.network.util.toState
import com.example.piano.data.course.api.CourseApi
import com.example.piano.data.course.api.dto.CategoryWithCoursesDTO
import com.example.piano.domain.course.repository.CourseRepository
import javax.inject.Inject

/**
 * 课程 Repository 实现
 */
class CourseRepositoryImpl @Inject constructor(
    private val courseApi: CourseApi
) : CourseRepository {

    override suspend fun getCategories(): ResponseState<List<CategoryWithCoursesDTO>> {
        return try {
            courseApi.listCategoriesWithCourses().toState()
        } catch (e: Exception) {
            ResponseState.UnknownError(e)
        }
    }
}
