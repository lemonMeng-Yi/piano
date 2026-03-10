package com.example.piano.ui.courses.sheet

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.midi.MidiManager
import android.os.Build
import android.os.ParcelUuid
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.audio.SheetAudioPlaybackManager
import com.example.piano.core.audio.AudioPitchCapture
import com.example.piano.core.audio.PitchResult
import com.example.piano.core.midi.MidiFileParser
import com.example.piano.core.midi.MidiPitchSource
import com.example.piano.core.network.util.ResponseState
import com.example.piano.domain.practice.Note
import com.example.piano.domain.sheet.repository.SheetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class SheetDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
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

    // ---------- 蓝牙 MIDI 练琴（参照 PracticeViewModel 的连接逻辑） ----------

    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as? MidiManager
    private val _midiPitchSourceHolder = MutableStateFlow(midiManager?.let { MidiPitchSource(it) })
    private val midiPitchSource: MidiPitchSource? get() = _midiPitchSourceHolder.value
    private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    private val _bluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    val bluetoothEnabled: StateFlow<Boolean> = _bluetoothEnabled.asStateFlow()

    private var bluetoothStateReceiver: BroadcastReceiver? = null

    private val _scannedBleMidiDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedBleMidiDevices: StateFlow<List<BluetoothDevice>> = _scannedBleMidiDevices.asStateFlow()

    private val _isScanningBle = MutableStateFlow(false)
    val isScanningBle: StateFlow<Boolean> = _isScanningBle.asStateFlow()

    private val _midiConnected = MutableStateFlow(false)
    val midiConnected: StateFlow<Boolean> = _midiConnected.asStateFlow()

    /** 当前已连接的蓝牙 MIDI 设备（用于弹窗中显示「已连接」） */
    private val _connectedBluetoothDevice = MutableStateFlow<BluetoothDevice?>(null)
    val connectedBluetoothDevice: StateFlow<BluetoothDevice?> = _connectedBluetoothDevice.asStateFlow()

    private val _midiConnectionError = MutableStateFlow<String?>(null)
    val midiConnectionError: StateFlow<String?> = _midiConnectionError.asStateFlow()

    /** 蓝牙练琴：是否显示底部键盘 */
    private val _showBluetoothPracticeKeyboard = MutableStateFlow(false)
    val showBluetoothPracticeKeyboard: StateFlow<Boolean> = _showBluetoothPracticeKeyboard.asStateFlow()

    /** 蓝牙练琴：连接后加载的 MIDI 音符列表 */
    private val _bluetoothPracticeNotes = MutableStateFlow<List<Note>?>(null)
    val bluetoothPracticeNotes: StateFlow<List<Note>?> = _bluetoothPracticeNotes.asStateFlow()

    private val _bluetoothPracticeLoading = MutableStateFlow(false)
    val bluetoothPracticeLoading: StateFlow<Boolean> = _bluetoothPracticeLoading.asStateFlow()

    /** 蓝牙练琴时当前 MIDI 输入音高（仅连接且显示键盘时有值） */
    val bluetoothPracticeCurrentPitch: StateFlow<PitchResult?> = combine(
        _showBluetoothPracticeKeyboard,
        _midiConnected,
        _midiPitchSourceHolder.flatMapLatest { it?.currentPitch ?: flowOf(null) }
    ) { show, connected, pitch ->
        if (show && connected) pitch else null
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isMidiSupported: Boolean = midiManager != null

    init {
        bluetoothStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
                    val enabled = state == BluetoothAdapter.STATE_ON
                    _bluetoothEnabled.value = enabled
                    if (!enabled) onBluetoothTurnedOff()
                }
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(bluetoothStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(bluetoothStateReceiver, filter)
        }
    }

    private fun onBluetoothTurnedOff() {
        midiPitchSource?.disconnect()
        _midiConnected.value = false
        _connectedBluetoothDevice.value = null
        _midiConnectionError.value = null
        stopBleMidiScan()
        _scannedBleMidiDevices.value = emptyList()
        _midiPitchSourceHolder.value = midiManager?.let { MidiPitchSource(it) }
    }

    fun isBluetoothEnabled(): Boolean = _bluetoothEnabled.value

    @SuppressLint("MissingPermission")
    fun startBleMidiScan() {
        val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) return
        if (_isScanningBle.value) return
        _scannedBleMidiDevices.value = emptyList()
        _isScanningBle.value = true
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid.fromString("03B80E5A-EDE8-4B33-A751-6CE34EC4C700"))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()
        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    val device = result.device
                    val current = _scannedBleMidiDevices.value
                    if (current.any { it.address == device.address }) return@launch
                    _scannedBleMidiDevices.value = current + device
                }
            }
            override fun onScanFailed(errorCode: Int) {
                viewModelScope.launch(Dispatchers.Main.immediate) {
                    _isScanningBle.value = false
                }
            }
        }
        bleScanCallback = callback
        try {
            scanner.startScan(listOf(filter), settings, callback)
        } catch (e: SecurityException) {
            _isScanningBle.value = false
            bleScanCallback = null
            return
        }
        viewModelScope.launch {
            delay(12_000)
            stopBleMidiScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopBleMidiScan() {
        val cb = bleScanCallback ?: run {
            _isScanningBle.value = false
            return
        }
        bleScanCallback = null
        try {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(cb)
        } catch (_: Exception) { }
        _isScanningBle.value = false
    }

    private var bleScanCallback: ScanCallback? = null

    fun getBluetoothDeviceDisplayName(device: BluetoothDevice): String {
        val name = device.name?.takeIf { it.isNotBlank() }
        return name ?: device.address
    }

    fun connectBluetoothMidi(device: BluetoothDevice) {
        midiPitchSource?.connectBluetooth(
            device,
            onConnected = {
                _midiConnected.value = true
                _connectedBluetoothDevice.value = device
                _midiConnectionError.value = null
                loadBluetoothPracticeNotesIfNeeded()
            },
            onError = {
                _midiConnected.value = false
                _connectedBluetoothDevice.value = null
                _midiConnectionError.value = it
            }
        ) ?: run {
            _midiConnectionError.value = "设备不支持 MIDI"
        }
    }

    fun disconnectMidi() {
        midiPitchSource?.disconnect()
        _midiConnected.value = false
        _connectedBluetoothDevice.value = null
        _midiConnectionError.value = null
    }

    fun clearMidiError() {
        _midiConnectionError.value = null
    }

    /** 蓝牙练琴：显示键盘（连接后在 onConnected 里加载 MIDI）；若已连接则立即触发加载 */
    fun startBluetoothPractice() {
        val success = _state.value as? SheetDetailUiState.Success ?: run {
            _snackbarMessage.value = "请先加载曲谱"
            return
        }
        if (success.midiUrl.isNullOrBlank()) {
            _snackbarMessage.value = "该曲谱暂无 MIDI，无法使用蓝牙 MIDI 练琴"
            return
        }
        if (!isMidiSupported) {
            _snackbarMessage.value = "当前设备不支持 MIDI"
            return
        }
        _showBluetoothPracticeKeyboard.value = true
        _bluetoothPracticeNotes.value = null
        if (_midiConnected.value) loadBluetoothPracticeNotesIfNeeded()
    }

    /** 连接成功后若正在蓝牙练琴则加载 MIDI 音符 */
    private fun loadBluetoothPracticeNotesIfNeeded() {
        if (!_showBluetoothPracticeKeyboard.value || !_midiConnected.value) return
        if (_bluetoothPracticeNotes.value != null) return
        val success = _state.value as? SheetDetailUiState.Success ?: return
        val midiUrl = success.midiUrl ?: return
        viewModelScope.launch {
            _bluetoothPracticeLoading.value = true
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
            _bluetoothPracticeLoading.value = false
            if (notes.isNotEmpty()) {
                _bluetoothPracticeNotes.value = notes
            } else {
                _snackbarMessage.value = "MIDI 解析无音符"
            }
        }
    }

    fun dismissBluetoothPracticeKeyboard() {
        disconnectMidi()
        stopBleMidiScan()
        _showBluetoothPracticeKeyboard.value = false
        _bluetoothPracticeNotes.value = null
        _connectedBluetoothDevice.value = null
    }

    // ---------- 声音识别练琴（麦克风识别，逻辑同跟弹） ----------

    private val audioPitchCapture = AudioPitchCapture()

    /** 声音识别练琴：是否显示底部键盘 */
    private val _showSoundPracticeKeyboard = MutableStateFlow(false)
    val showSoundPracticeKeyboard: StateFlow<Boolean> = _showSoundPracticeKeyboard.asStateFlow()

    /** 声音识别练琴：MIDI 解析出的按时间顺序的音符列表 */
    private val _soundPracticeNotes = MutableStateFlow<List<Note>?>(null)
    val soundPracticeNotes: StateFlow<List<Note>?> = _soundPracticeNotes.asStateFlow()

    /** 声音识别练琴：是否正在加载 MIDI */
    private val _soundPracticeLoading = MutableStateFlow(false)
    val soundPracticeLoading: StateFlow<Boolean> = _soundPracticeLoading.asStateFlow()

    val soundPracticeCurrentPitch: StateFlow<PitchResult?> = audioPitchCapture.currentPitch

    private val _soundPracticeRecording = MutableStateFlow(false)
    val soundPracticeRecording: StateFlow<Boolean> = _soundPracticeRecording.asStateFlow()

    /** 开始声音识别练琴：加载 MIDI 后弹出键盘，用麦克风识别比对 */
    fun startSoundPractice() {
        val success = _state.value as? SheetDetailUiState.Success ?: run {
            _snackbarMessage.value = "请先加载曲谱"
            return
        }
        val midiUrl = success.midiUrl
        if (midiUrl.isNullOrBlank()) {
            _snackbarMessage.value = "该曲谱暂无 MIDI，无法使用声音识别练琴"
            return
        }
        viewModelScope.launch {
            _soundPracticeLoading.value = true
            _soundPracticeNotes.value = null
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
            _soundPracticeLoading.value = false
            if (notes.isEmpty()) {
                _snackbarMessage.value = "MIDI 解析无音符"
            } else {
                _soundPracticeNotes.value = notes
                _showSoundPracticeKeyboard.value = true
            }
        }
    }

    /** 关闭声音识别练琴（停止麦克风并收起键盘） */
    fun dismissSoundPracticeKeyboard() {
        stopPitchCapture()
        _showSoundPracticeKeyboard.value = false
        _soundPracticeNotes.value = null
    }

    fun startPitchCapture() {
        if (_soundPracticeRecording.value) return
        _soundPracticeRecording.value = true
        viewModelScope.launch {
            val started = audioPitchCapture.startCapture()
            _soundPracticeRecording.value = false
        }
    }

    fun stopPitchCapture() {
        audioPitchCapture.stopCapture()
        _soundPracticeRecording.value = false
    }

    fun onSoundPracticePermissionDenied() {
        _snackbarMessage.value = "需要麦克风权限"
    }

    override fun onCleared() {
        super.onCleared()
        audioPitchCapture.stopCapture()
        bluetoothStateReceiver?.let {
            try {
                context.unregisterReceiver(it)
            } catch (_: Exception) { }
        }
        bluetoothStateReceiver = null
        stopBleMidiScan()
        midiPitchSource?.disconnect()
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
                    // 记录播放/练习（需登录，失败不影响当前页）
                    viewModelScope.launch {
                        sheetRepository.recordPlay(sheetId)
                    }
                }
                is ResponseState.NetworkError -> _state.value = SheetDetailUiState.Error(result.msg)
                is ResponseState.UnknownError -> _state.value = SheetDetailUiState.Error(
                    result.throwable?.message ?: "加载失败"
                )
            }
        }
    }
}
