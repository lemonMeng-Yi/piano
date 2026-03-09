package com.example.piano.ui.practice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.midi.MidiFileParser
import com.example.piano.core.network.util.ResponseState
import com.example.piano.domain.practice.Note
import com.example.piano.domain.sheet.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

/** 虚拟键盘练琴页 UI 状态 */
sealed class VirtualKeyboardPracticeUiState {
    data object Loading : VirtualKeyboardPracticeUiState()
    data class Success(val title: String, val notes: List<Note>) : VirtualKeyboardPracticeUiState()
    data class Error(val message: String) : VirtualKeyboardPracticeUiState()
}

/**
 * 虚拟键盘练琴 ViewModel：根据 sheetId 加载曲谱详情与 MIDI，解析出按时间顺序的音符列表供用户逐键比对。
 */
@HiltViewModel
class VirtualKeyboardPracticeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sheetRepository: SheetRepository
) : ViewModel() {

    private val sheetId: Long = savedStateHandle.get<Long>("sheetId") ?: 0L

    private val _uiState = MutableStateFlow<VirtualKeyboardPracticeUiState>(VirtualKeyboardPracticeUiState.Loading)
    val uiState: StateFlow<VirtualKeyboardPracticeUiState> = _uiState.asStateFlow()

    init {
        loadSheetAndMidiNotes()
    }

    private fun loadSheetAndMidiNotes() {
        if (sheetId <= 0L) {
            _uiState.value = VirtualKeyboardPracticeUiState.Error("无效的曲谱")
            return
        }
        viewModelScope.launch {
            _uiState.value = VirtualKeyboardPracticeUiState.Loading
            when (val result = sheetRepository.getById(sheetId)) {
                is ResponseState.Success -> {
                    val dto = result.body
                    val midiUrl = dto.midiUrl
                    if (midiUrl.isNullOrBlank()) {
                        _uiState.value = VirtualKeyboardPracticeUiState.Error("该曲谱暂无 MIDI，无法进行虚拟键盘练琴")
                        return@launch
                    }
                    val notes = withContext(Dispatchers.IO) {
                        try {
                            val bytes = URL(midiUrl).openStream().use { it.readBytes() }
                            val events = MidiFileParser.parseNoteEvents(bytes)
                            events
                                .filter { it.isOn }
                                .sortedBy { it.timeMs }
                                .map { Note(it.midiNote) }
                        } catch (e: Exception) {
                            emptyList()
                        }
                    }
                    if (notes.isEmpty()) {
                        _uiState.value = VirtualKeyboardPracticeUiState.Error("MIDI 解析无音符")
                    } else {
                        _uiState.value = VirtualKeyboardPracticeUiState.Success(title = dto.title, notes = notes)
                    }
                }
                is ResponseState.NetworkError -> _uiState.value = VirtualKeyboardPracticeUiState.Error(result.msg)
                is ResponseState.UnknownError -> _uiState.value = VirtualKeyboardPracticeUiState.Error(
                    result.throwable?.message ?: "加载失败"
                )
            }
        }
    }

    fun retry() {
        loadSheetAndMidiNotes()
    }
}
