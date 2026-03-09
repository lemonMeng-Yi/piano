package com.example.piano.ui.courses.sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.network.util.ResponseState
import com.example.piano.domain.sheet.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 曲谱详情页 UI 状态 */
sealed class SheetDetailUiState {
    data object Loading : SheetDetailUiState()
    data class Success(val title: String, val sheetDataUrl: String?) : SheetDetailUiState()
    data class Error(val message: String) : SheetDetailUiState()
}

@HiltViewModel
class SheetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sheetRepository: SheetRepository
) : ViewModel() {

    private val sheetId: Long = savedStateHandle.get<Long>("sheetId") ?: 0L

    private val _state = MutableStateFlow<SheetDetailUiState>(SheetDetailUiState.Loading)
    val state: StateFlow<SheetDetailUiState> = _state.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        if (sheetId <= 0L) {
            _state.value = SheetDetailUiState.Error("无效的曲谱")
            return
        }
        viewModelScope.launch {
            _state.value = SheetDetailUiState.Loading
            when (val result = sheetRepository.getById(sheetId)) {
                is ResponseState.Success -> _state.value = SheetDetailUiState.Success(
                    title = result.body.title,
                    sheetDataUrl = result.body.sheetDataUrl
                )
                is ResponseState.NetworkError -> _state.value = SheetDetailUiState.Error(result.msg)
                is ResponseState.UnknownError -> _state.value = SheetDetailUiState.Error(
                    result.throwable?.message ?: "加载失败"
                )
            }
        }
    }
}
