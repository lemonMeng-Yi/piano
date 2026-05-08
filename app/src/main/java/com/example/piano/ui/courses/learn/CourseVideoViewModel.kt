package com.example.piano.ui.courses.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.course.api.dto.CourseDetailDTO
import com.example.piano.domain.course.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseVideoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val courseRepository: CourseRepository
) : ViewModel() {

    val courseId: Int = savedStateHandle.get<Int>("courseId") ?: 0
    val videoUrl: String = savedStateHandle.get<String>("videoUrl").orEmpty()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _playWhenReady = MutableStateFlow(true)
    val playWhenReady: StateFlow<Boolean> = _playWhenReady.asStateFlow()

    private val _courseDetail = MutableStateFlow<CourseDetailDTO?>(null)
    val courseDetail: StateFlow<CourseDetailDTO?> = _courseDetail.asStateFlow()

    private val _isMarkingComplete = MutableStateFlow(false)
    val isMarkingComplete: StateFlow<Boolean> = _isMarkingComplete.asStateFlow()

    private val _completeResult = MutableStateFlow<ResponseState<Unit>?>(null)
    val completeResult: StateFlow<ResponseState<Unit>?> = _completeResult.asStateFlow()

    init {
        loadCourseDetail()
    }

    private fun loadCourseDetail() {
        if (courseId <= 0) return
        viewModelScope.launch {
            when (val result = courseRepository.getCourseById(courseId)) {
                is ResponseState.Success -> _courseDetail.value = result.body
                else -> { /* 静默处理，不影响视频播放 */ }
            }
        }
    }

    fun markComplete() {
        if (courseId <= 0 || _isMarkingComplete.value) return
        viewModelScope.launch {
            _isMarkingComplete.value = true
            _completeResult.value = null
            val result = courseRepository.markCourseComplete(courseId)
            _completeResult.value = result
            if (result is ResponseState.Success) {
                _courseDetail.value = _courseDetail.value?.copy(isCompleted = 1)
            }
            _isMarkingComplete.value = false
        }
    }

    /** 保存当前播放位置与是否自动播放，在界面因配置变更销毁前调用 */
    fun saveState(positionMs: Long, playWhenReady: Boolean) {
        _currentPositionMs.value = positionMs
        _playWhenReady.value = playWhenReady
    }
}
