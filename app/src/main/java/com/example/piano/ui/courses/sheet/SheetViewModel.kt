package com.example.piano.ui.courses.sheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.network.util.ResponseState
import com.example.piano.data.sheet.api.dto.SheetItemDTO
import com.example.piano.domain.sheet.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 乐谱列表 Tab 状态 */
sealed class SheetListUiState {
    data object Loading : SheetListUiState()
    data class Success(val list: List<SheetItemDTO>) : SheetListUiState()
    data class Error(val message: String) : SheetListUiState()
}

/** 收藏 Tab 状态（401 时需提示登录） */
sealed class SheetFavoritesUiState {
    data object Loading : SheetFavoritesUiState()
    data class Success(val list: List<SheetItemDTO>) : SheetFavoritesUiState()
    data class Error(val message: String) : SheetFavoritesUiState()
    /** 未登录，需提示「请先登录」 */
    data object NeedLogin : SheetFavoritesUiState()
}

@HiltViewModel
class SheetViewModel @Inject constructor(
    private val sheetRepository: SheetRepository
) : ViewModel() {

    private val _listState = MutableStateFlow<SheetListUiState>(SheetListUiState.Loading)
    val listState: StateFlow<SheetListUiState> = _listState.asStateFlow()

    private val _favoritesState = MutableStateFlow<SheetFavoritesUiState>(SheetFavoritesUiState.Loading)
    val favoritesState: StateFlow<SheetFavoritesUiState> = _favoritesState.asStateFlow()

    init {
        loadList()
    }

    fun loadList() {
        viewModelScope.launch {
            _listState.value = SheetListUiState.Loading
            when (val result = sheetRepository.list()) {
                is ResponseState.Success -> _listState.value = SheetListUiState.Success(result.body)
                is ResponseState.NetworkError -> _listState.value = SheetListUiState.Error(result.msg)
                is ResponseState.UnknownError -> _listState.value = SheetListUiState.Error(
                    result.throwable?.message ?: "加载失败"
                )
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _favoritesState.value = SheetFavoritesUiState.Loading
            when (val result = sheetRepository.listFavorites()) {
                is ResponseState.Success -> _favoritesState.value = SheetFavoritesUiState.Success(result.body)
                is ResponseState.NetworkError -> {
                    if (result.code == 401) {
                        _favoritesState.value = SheetFavoritesUiState.NeedLogin
                    } else {
                        _favoritesState.value = SheetFavoritesUiState.Error(result.msg)
                    }
                }
                is ResponseState.UnknownError -> _favoritesState.value = SheetFavoritesUiState.Error(
                    result.throwable?.message ?: "加载失败"
                )
            }
        }
    }
}
