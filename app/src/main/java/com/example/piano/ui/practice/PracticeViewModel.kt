package com.example.piano.ui.practice

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.media.midi.MidiManager
import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.audio.AudioPitchCapture
import com.example.piano.core.audio.PitchResult
import com.example.piano.core.midi.MidiPitchSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val audioPitchCapture = AudioPitchCapture()

    private val midiManager = context.getSystemService(Context.MIDI_SERVICE) as? MidiManager
    private val midiPitchSource = midiManager?.let { MidiPitchSource(it) }
    private val bluetoothAdapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    /** 应用内扫描到的蓝牙 MIDI 设备（不依赖系统已配对列表） */
    private val _scannedBleMidiDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val scannedBleMidiDevices: StateFlow<List<BluetoothDevice>> = _scannedBleMidiDevices.asStateFlow()

    private val _isScanningBle = MutableStateFlow(false)
    val isScanningBle: StateFlow<Boolean> = _isScanningBle.asStateFlow()

    private val _useMidiSource = MutableStateFlow(false)
    val useMidiSource: StateFlow<Boolean> = _useMidiSource.asStateFlow()

    private val _midiConnected = MutableStateFlow(false)
    val midiConnected: StateFlow<Boolean> = _midiConnected.asStateFlow()

    private val _midiConnectionError = MutableStateFlow<String?>(null)
    val midiConnectionError: StateFlow<String?> = _midiConnectionError.asStateFlow()

    val currentPitch: StateFlow<PitchResult?> = combine(
        _useMidiSource,
        _midiConnected,
        midiPitchSource?.currentPitch ?: flowOf(null),
        audioPitchCapture.currentPitch
    ) { useMidi, connected, midiPitch, audioPitch ->
        if (useMidi && connected) midiPitch else audioPitch
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = combine(
        _isRecording,
        _useMidiSource,
        _midiConnected
    ) { micRecording, useMidi, connected ->
        micRecording || (useMidi && connected)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()

    val isMidiSupported: Boolean = midiManager != null

    /** 蓝牙是否已打开（未开时需先请求用户打开再扫描） */
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun setUseMidiSource(use: Boolean) {
        _useMidiSource.value = use
        if (!use) {
            stopPitchCapture()
            stopBleMidiScan()
            midiPitchSource?.disconnect()
            _midiConnected.value = false
        }
    }

    /** 应用内扫描蓝牙 MIDI 设备（不依赖系统已配对），扫描到的设备点击即可连接 */
    @SuppressLint("MissingPermission")
    fun startBleMidiScan() {
        val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner
        if (scanner == null) {
            return
        }
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

    /** 通过扫描到的蓝牙 MIDI 设备连接，连接后即可接收琴音（逻辑与麦克风一致） */
    fun connectBluetoothMidi(device: BluetoothDevice) {
        midiPitchSource?.connectBluetooth(
            device,
            onConnected = {
                _midiConnected.value = true
                _midiConnectionError.value = null
            },
            onError = {
                _midiConnected.value = false
                _midiConnectionError.value = it
            }
        ) ?: run {
            _midiConnectionError.value = "设备不支持 MIDI"
        }
    }

    fun disconnectMidi() {
        midiPitchSource?.disconnect()
        _midiConnected.value = false
        _midiConnectionError.value = null
    }

    fun clearMidiError() {
        _midiConnectionError.value = null
    }

    fun startPitchCapture() {
        if (_useMidiSource.value) {
            if (_midiConnected.value) {
                _isRecording.value = true
            }
            return
        }
        if (_isRecording.value) return
        viewModelScope.launch {
            _permissionDenied.value = false
            _isRecording.value = true
            val started = audioPitchCapture.startCapture()
            _isRecording.value = false
            if (!started) _permissionDenied.value = true
        }
    }

    fun stopPitchCapture() {
        if (_useMidiSource.value) {
            _isRecording.value = false
            return
        }
        if (!_isRecording.value) return
        audioPitchCapture.stopCapture()
        _isRecording.value = false
    }

    fun clearPermissionDenied() {
        _permissionDenied.value = false
    }

    fun onPermissionDenied() {
        _permissionDenied.value = true
    }

    override fun onCleared() {
        super.onCleared()
        stopBleMidiScan()
        midiPitchSource?.disconnect()
        audioPitchCapture.stopCapture()
    }
}
