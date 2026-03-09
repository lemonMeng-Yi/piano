package com.example.piano.ui.courses.sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.audio.SheetAudioPlaybackManager
import com.example.piano.core.midi.MidiFileParser
import com.example.piano.core.network.util.ResponseState
import com.example.piano.domain.practice.Note
import com.example.piano.domain.sheet.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL
import javax.inject.Inject

/** 曲谱详情页 UI 状态 */
sealed class SheetDetailUiState {
    data object Loading : SheetDetailUiState()
    data class Success(
        val title: String,
        val staffSheetDataUrl: String?,
        val simplifiedSheetDataUrl: String?,
        val mp3Url: String?,
        val midiUrl: String?,
        val favorited: Boolean
    ) : SheetDetailUiState()
    data class Error(val message: String) : SheetDetailUiState()
}

@HiltViewModel
class SheetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val sheetRepository: SheetRepository,
    private val audioPlayback: SheetAudioPlaybackManager
) : ViewModel() {

    private val sheetId: Long = savedStateHandle.get<Long>("sheetId") ?: 0L

    private val _state = MutableStateFlow<SheetDetailUiState>(SheetDetailUiState.Loading)
    val state: StateFlow<SheetDetailUiState> = _state.asStateFlow()

    /** 当前是否使用五线谱（false = 简谱） */
    private val _useStaffNotation = MutableStateFlow(false)
    val useStaffNotation: StateFlow<Boolean> = _useStaffNotation.asStateFlow()

    /** 收藏状态（与 Success.favorited 同步） */
    private val _favorited = MutableStateFlow(false)
    val favorited: StateFlow<Boolean> = _favorited.asStateFlow()

    val playingSheetId: StateFlow<Long?> = audioPlayback.playingSheetId
    val isPlaying: StateFlow<Boolean> = audioPlayback.isPlaying

    /** 当前详情页曲谱 id，用于 UI 判断是否正在播放本条 */
    val currentSheetId: Long get() = sheetId

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    /** 随 MIDI 播放高亮的琴键（MIDI 音高集合），仅在本条曲谱播放且有 midiUrl 时有值 */
    private val _activeMidiKeys = MutableStateFlow<Set<Int>>(emptySet())
    val activeMidiKeys: StateFlow<Set<Int>> = _activeMidiKeys.asStateFlow()

    /** 当前曲谱的 MIDI 音符区间 [startMs, endMs) -> midi，解析成功后缓存 */
    private var midiSegments: List<Triple<Long, Long, Int>> = emptyList()
    private var loadedMidiUrl: String? = null

    /** 虚拟键盘练琴：是否显示底部键盘 */
    private val _showVirtualPracticeKeyboard = MutableStateFlow(false)
    val showVirtualPracticeKeyboard: StateFlow<Boolean> = _showVirtualPracticeKeyboard.asStateFlow()

    /** 虚拟键盘练琴：MIDI 解析出的按时间顺序的音符列表，加载成功后非空 */
    private val _virtualPracticeNotes = MutableStateFlow<List<Note>?>(null)
    val virtualPracticeNotes: StateFlow<List<Note>?> = _virtualPracticeNotes.asStateFlow()

    /** 虚拟键盘练琴：是否正在加载 MIDI 音符 */
    private val _virtualPracticeLoading = MutableStateFlow(false)
    val virtualPracticeLoading: StateFlow<Boolean> = _virtualPracticeLoading.asStateFlow()

    fun clearSnackbarMessage() { _snackbarMessage.value = null }

    fun setUseStaffNotation(useStaff: Boolean) {
        _useStaffNotation.value = useStaff
    }

    /** 播放/暂停；无音频时提示 */
    fun togglePlayPause(mp3Url: String?) {
        audioPlayback.toggle(
            sheetId = sheetId,
            mp3Url = mp3Url,
            onNoAudio = { _snackbarMessage.value = "暂无音频" }
        )
    }

    /** 收藏/取消收藏 */
    fun toggleFavorite() {
        val current = _favorited.value
        viewModelScope.launch {
            val result = if (current) sheetRepository.removeFavorite(sheetId) else sheetRepository.addFavorite(sheetId)
            when (result) {
                is ResponseState.Success -> _favorited.value = !current
                is ResponseState.NetworkError -> {
                    if (result.code == 401) _snackbarMessage.value = "请先登录"
                    else _snackbarMessage.value = result.msg
                }
                is ResponseState.UnknownError -> _snackbarMessage.value = result.throwable?.message ?: "操作失败"
            }
        }
    }

    init {
        loadDetail()
        viewModelScope.launch {
            combine(
                audioPlayback.playingSheetId,
                audioPlayback.isPlaying,
                audioPlayback.playbackPositionMs,
                _state
            ) { playingId, playing, positionMs, uiState ->
                Triple(playingId == sheetId && playing, positionMs, uiState)
            }.collect { (isThisSheetPlaying, positionMs, uiState) ->
                if (!isThisSheetPlaying) {
                    _activeMidiKeys.value = emptySet()
                    return@collect
                }
                val success = uiState as? SheetDetailUiState.Success ?: return@collect
                val midiUrl = success.midiUrl
                if (midiUrl.isNullOrBlank()) {
                    _activeMidiKeys.value = emptySet()
                    return@collect
                }
                if (midiSegments.isEmpty() || loadedMidiUrl != midiUrl) {
                    loadedMidiUrl = midiUrl
                    midiSegments = loadMidiSegments(midiUrl)
                }
                _activeMidiKeys.value = midiSegments
                    .filter { (start, end, _) -> positionMs >= start && positionMs < end }
                    .map { it.third }
                    .toSet()
            }
        }
    }

    /** 从 midiUrl 下载并解析 MIDI，返回 [startMs, endMs, midi] 区间列表 */
    private suspend fun loadMidiSegments(midiUrl: String): List<Triple<Long, Long, Int>> {
        return withContext(Dispatchers.IO) {
            try {
                val bytes = URL(midiUrl).openStream().use { it.readBytes() }
                val events = MidiFileParser.parseNoteEvents(bytes)
                buildNoteSegments(events)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /** 将按时间排序的 NoteEvent 转为 [onMs, offMs, midi] 区间 */
    private fun buildNoteSegments(events: List<MidiFileParser.NoteEvent>): List<Triple<Long, Long, Int>> {
        val byNote = events.groupBy { it.midiNote }.mapValues { (_, list) -> list.sortedBy { it.timeMs } }
        val segments = mutableListOf<Triple<Long, Long, Int>>()
        for ((midi, list) in byNote) {
            var lastOnMs: Long? = null
            for (e in list) {
                if (e.isOn) lastOnMs = e.timeMs
                else if (lastOnMs != null) {
                    segments.add(Triple(lastOnMs, e.timeMs, midi))
                    lastOnMs = null
                }
            }
        }
        return segments
    }

    /** 开始虚拟键盘练琴：加载 MIDI 并解析为按时间顺序的音符，成功后显示底部键盘 */
    fun startVirtualPractice() {
        val success = _state.value as? SheetDetailUiState.Success ?: run {
            _snackbarMessage.value = "请先加载曲谱"
            return
        }
        val midiUrl = success.midiUrl
        if (midiUrl.isNullOrBlank()) {
            _snackbarMessage.value = "该曲谱暂无 MIDI，无法使用虚拟键盘练琴"
            return
        }
        viewModelScope.launch {
            _virtualPracticeLoading.value = true
            _virtualPracticeNotes.value = null
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
            _virtualPracticeLoading.value = false
            if (notes.isEmpty()) {
                _snackbarMessage.value = "MIDI 解析无音符"
            } else {
                _virtualPracticeNotes.value = notes
                _showVirtualPracticeKeyboard.value = true
            }
        }
    }

    /** 关闭虚拟键盘练琴（收起键盘） */
    fun dismissVirtualPracticeKeyboard() {
        _showVirtualPracticeKeyboard.value = false
        _virtualPracticeNotes.value = null
    }

    fun loadDetail() {
        if (sheetId <= 0L) {
            _state.value = SheetDetailUiState.Error("无效的曲谱")
            return
        }
        viewModelScope.launch {
            _state.value = SheetDetailUiState.Loading
            when (val result = sheetRepository.getById(sheetId)) {
                is ResponseState.Success -> {
                    val dto = result.body
                    _state.value = SheetDetailUiState.Success(
                        title = dto.title,
                        staffSheetDataUrl = dto.staffSheetDataUrl,
                        simplifiedSheetDataUrl = dto.simplifiedSheetDataUrl,
                        mp3Url = dto.mp3Url,
                        midiUrl = dto.midiUrl,
                        favorited = dto.favorited
                    )
                    _favorited.value = dto.favorited
                }
                is ResponseState.NetworkError -> _state.value = SheetDetailUiState.Error(result.msg)
                is ResponseState.UnknownError -> _state.value = SheetDetailUiState.Error(
                    result.throwable?.message ?: "加载失败"
                )
            }
        }
    }
}
