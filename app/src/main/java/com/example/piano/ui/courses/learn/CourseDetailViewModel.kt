package com.example.piano.ui.courses.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.manager.TokenManager
import com.example.piano.core.network.util.ResponseState
import com.example.piano.domain.course.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 详情页单课时 */
data class LessonUi(
    val courseId: Int,
    val title: String,
    val videoUrl: String,
    val isCompleted: Boolean = false,
    val isLocked: Boolean = false
)

/** 详情页状态 */
sealed class CourseDetailUiState {
    data object Loading : CourseDetailUiState()
    data class Success(val categoryName: String, val lessons: List<LessonUi>) : CourseDetailUiState()
    data class Error(val message: String) : CourseDetailUiState()
}

private const val PLACEHOLDER_VIDEO_URL = "https://piano-course.oss-cn-beijing.aliyuncs.com/course/54eadcdcf136dd33b0fdbf0afbd24061.mp4"

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val categoryId: Int = savedStateHandle.get<Int>("courseId") ?: 0

    private val _uiState = MutableStateFlow<CourseDetailUiState>(CourseDetailUiState.Loading)
    val uiState: StateFlow<CourseDetailUiState> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = CourseDetailUiState.Loading
            when (val result = courseRepository.getCategories()) {
                is ResponseState.Success -> {
                    val category = result.body.find { it.categoryId == categoryId }
                    if (category == null) {
                        _uiState.value = CourseDetailUiState.Error("未找到该课程")
                    } else {
                        val categoryLocked = category.isLocked ?: !TokenManager.isLoggedIn()
                        val lessons = category.courses.map { c ->
                            LessonUi(
                                courseId = c.courseId,
                                title = c.title,
                                videoUrl = c.videoUrl?.takeIf { it.isNotBlank() } ?: PLACEHOLDER_VIDEO_URL,
                                isCompleted = c.isCompleted == 1,
                                isLocked = categoryLocked
                            )
                        }
                        _uiState.value = CourseDetailUiState.Success(category.categoryName, lessons)
                    }
                }
                is ResponseState.NetworkError -> _uiState.value = CourseDetailUiState.Error(result.msg)
                is ResponseState.UnknownError -> _uiState.value = CourseDetailUiState.Error(
                    result.throwable?.message ?: "加载失败"
                )
            }
        }
    }
}
