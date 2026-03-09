package com.example.piano.ui.courses.learn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.course.api.dto.CategoryWithCoursesDTO
import com.example.piano.data.course.api.dto.CourseItemDTO
import com.example.piano.domain.course.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 学钢琴 Tab 下单个大模块的 UI 数据 */
data class CategoryUi(
    /** 大模块 ID，对应后端 categoryId，用于进入详情页时传参 */
    val categoryId: Int,
    /** 大模块名称，如「认识钢琴和乐谱」 */
    val title: String,
    /** 子课时标题列表，用于卡片上展示为 • 条目 */
    val bullets: List<String>,
    /** 状态文案，如「进行中 0/3」「未开始」 */
    val statusText: String,
    /** 是否有进行中的子课时（有子课时时为 true） */
    val inProgress: Boolean,
    /** 是否有子课时；为 true 时点击「开始学习」进入详情页，否则不进入 */
    val hasSubCourses: Boolean,
    /** 该大模块下的子课时列表（courseId + title），详情页展示并点击播放视频 */
    val courses: List<CourseItemDTO>
)

/** 课程列表状态 */
sealed class CoursesUiState {
    data object Loading : CoursesUiState()
    data class Success(val categories: List<CategoryUi>) : CoursesUiState()
    data class Error(val message: String) : CoursesUiState()
}

@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CoursesUiState>(CoursesUiState.Loading)
    val uiState: StateFlow<CoursesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = CoursesUiState.Loading
            when (val result = courseRepository.getCategories()) {
                is ResponseState.Success -> {
                    val list = result.body.map { dto -> dto.toCategoryUi() }
                    _uiState.value = CoursesUiState.Success(list)
                }
                is ResponseState.NetworkError -> _uiState.value = CoursesUiState.Error(result.msg)
                is ResponseState.UnknownError -> _uiState.value = CoursesUiState.Error(
                    result.throwable?.message ?: "加载失败"
                )
            }
        }
    }

    private fun CategoryWithCoursesDTO.toCategoryUi(): CategoryUi {
        val hasSub = courses.isNotEmpty()
        val bullets = courses.map { it.title }
        val statusText = if (hasSub) "进行中 0/${courses.size}" else "未开始"
        return CategoryUi(
            categoryId = categoryId,
            title = categoryName,
            bullets = bullets,
            statusText = statusText,
            inProgress = hasSub,
            hasSubCourses = hasSub,
            courses = courses
        )
    }
}
