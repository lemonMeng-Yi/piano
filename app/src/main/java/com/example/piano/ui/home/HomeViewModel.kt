package com.example.piano.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.sheet.api.dto.SheetItemDTO
import com.example.piano.data.sheet.api.dto.SheetPlayRecordDTO
import com.example.piano.domain.sheet.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 首页热门曲谱 UI 状态 */
sealed class HotSheetsUiState {
    data object Loading : HotSheetsUiState()
    data class Success(val list: List<SheetItemDTO>) : HotSheetsUiState()
    data class Error(val message: String) : HotSheetsUiState()
}

/** 首页最近练习 UI 状态 */
sealed class RecentPlaysUiState {
    data object Loading : RecentPlaysUiState()
    data class Success(val list: List<SheetPlayRecordDTO>) : RecentPlaysUiState()
    data class Error(val message: String) : RecentPlaysUiState()
    /** 未登录，不显示列表或提示先登录 */
    data object NeedLogin : RecentPlaysUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sheetRepository: SheetRepository
) : ViewModel() {

    private val _recentPlaysState = MutableStateFlow<RecentPlaysUiState>(RecentPlaysUiState.Loading)
    val recentPlaysState: StateFlow<RecentPlaysUiState> = _recentPlaysState.asStateFlow()

    /** 今日练习目标时长（分钟），可点击设置 */
    private val _todayPracticeGoalMinutes = MutableStateFlow(25)
    val todayPracticeGoalMinutes: StateFlow<Int> = _todayPracticeGoalMinutes.asStateFlow()

    /** 热门曲谱（乐谱列表前 N 条） */
    private val _hotSheetsState = MutableStateFlow<HotSheetsUiState>(HotSheetsUiState.Loading)
    val hotSheetsState: StateFlow<HotSheetsUiState> = _hotSheetsState.asStateFlow()

    fun setTodayPracticeGoal(minutes: Int) {
        _todayPracticeGoalMinutes.value = minutes
    }

    fun loadRecentPlays() {
        viewModelScope.launch {
            _recentPlaysState.value = RecentPlaysUiState.Loading
            when (val result = sheetRepository.listRecentPlays(limit = 3)) {
                is ResponseState.Success ->
                    _recentPlaysState.value = RecentPlaysUiState.Success(result.body)
                is ResponseState.NetworkError -> {
                    if (result.code == 401) {
                        _recentPlaysState.value = RecentPlaysUiState.NeedLogin
                    } else {
                        _recentPlaysState.value = RecentPlaysUiState.Error(result.msg)
                    }
                }
                is ResponseState.UnknownError ->
                    _recentPlaysState.value = RecentPlaysUiState.Error(
                        result.throwable?.message ?: "加载失败"
                    )
            }
        }
    }

    private val hotSheetsLimit = 6

    fun loadHotSheets() {
        viewModelScope.launch {
            _hotSheetsState.value = HotSheetsUiState.Loading
            when (val result = sheetRepository.list()) {
                is ResponseState.Success ->
                    _hotSheetsState.value = HotSheetsUiState.Success(result.body.take(hotSheetsLimit))
                is ResponseState.NetworkError ->
                    _hotSheetsState.value = HotSheetsUiState.Error(result.msg)
                is ResponseState.UnknownError ->
                    _hotSheetsState.value = HotSheetsUiState.Error(
                        result.throwable?.message ?: "加载失败"
                    )
            }
        }
    }
}
