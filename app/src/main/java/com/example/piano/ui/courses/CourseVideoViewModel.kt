package com.example.piano.ui.courses

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * 课程视频页 ViewModel：在配置变更（如横竖屏翻转）时保留播放进度与播放/暂停状态，
 * 以便重建界面后从原位置继续播放。
 */
@HiltViewModel
class CourseVideoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val videoUrl: String = savedStateHandle.get<String>("videoUrl").orEmpty()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _playWhenReady = MutableStateFlow(true)
    val playWhenReady: StateFlow<Boolean> = _playWhenReady.asStateFlow()

    /** 保存当前播放位置与是否自动播放，在界面因配置变更销毁前调用 */
    fun saveState(positionMs: Long, playWhenReady: Boolean) {
        _currentPositionMs.value = positionMs
        _playWhenReady.value = playWhenReady
    }
}
