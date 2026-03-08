package com.example.piano.core.midi

import android.bluetooth.BluetoothDevice
import android.media.midi.MidiDevice
import android.media.midi.MidiManager
import android.media.midi.MidiReceiver
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.piano.core.audio.PitchResult
import com.example.piano.domain.practice.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow

/**
 * 蓝牙/USB MIDI 输入：解析 Note On/Off，以与麦克风相同的 PitchResult 暴露给跟弹与实时音高。
 */
class MidiPitchSource(
    private val midiManager: MidiManager,
    private val handler: Handler = Handler(Looper.getMainLooper())
) {

    private val _currentPitch = MutableStateFlow<PitchResult?>(null)
    val currentPitch: StateFlow<PitchResult?> = _currentPitch.asStateFlow()

    private var device: MidiDevice? = null
    private val outputPorts = mutableListOf<android.media.midi.MidiOutputPort>()
    private val connectedReceivers = mutableListOf<MidiReceiver>()

    /** 通过蓝牙设备连接 BLE MIDI（如电钢），连接后即可接收琴音 */
    fun connectBluetooth(
        bluetoothDevice: BluetoothDevice,
        onConnected: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            handler.post { onError("需要 Android 6.0 及以上") }
            return
        }
        disconnect()
        midiManager.openBluetoothDevice(bluetoothDevice, { openedDevice ->
            if (openedDevice == null) {
                handler.post { onError("无法打开蓝牙 MIDI 设备") }
                return@openBluetoothDevice
            }
            device = openedDevice
            val onNote: (Int, Int, Boolean) -> Unit = { note, velocity, isOn ->
                handler.post {
                    _currentPitch.value = if (isOn && velocity > 0) {
                        PitchResult.Pitch(Note(note), midiNoteToFreq(note))
                    } else {
                        PitchResult.Listening
                    }
                }
            }
            val portCount = openedDevice.info.outputPortCount
            for (idx in 0 until portCount) {
                val port = openedDevice.openOutputPort(idx) ?: continue
                val receiver = MidiNoteReceiver(onNote)
                port.connect(receiver)
                outputPorts.add(port)
                connectedReceivers.add(receiver)
            }
            if (outputPorts.isEmpty()) {
                openedDevice.close()
                device = null
                handler.post { onError("无法打开输出端口") }
                return@openBluetoothDevice
            }
            handler.post { onConnected() }
        }, handler)
    }

    fun disconnect() {
        connectedReceivers.forEachIndexed { index, receiver ->
            try {
                outputPorts.getOrNull(index)?.disconnect(receiver)
            } catch (_: Exception) { }
        }
        connectedReceivers.clear()
        outputPorts.forEach { try { it.close() } catch (_: Exception) { } }
        outputPorts.clear()
        try {
            device?.close()
        } catch (_: Exception) { }
        device = null
        _currentPitch.value = null
    }

    fun isConnected(): Boolean = device != null

    private fun midiNoteToFreq(midi: Int): Float {
        if (midi <= 0) return 0f
        return (440f * 2f.pow((midi - 69) / 12f))
    }

    private class MidiNoteReceiver(
        private val onNote: (noteNumber: Int, velocity: Int, isOn: Boolean) -> Unit
    ) : MidiReceiver() {

        private var runningStatus = 0
        private val buffer = mutableListOf<Byte>()

        override fun onSend(msg: ByteArray, offset: Int, count: Int, timestamp: Long) {
            for (j in offset until offset + count) buffer.add(msg[j])
            while (buffer.isNotEmpty()) {
                var consumed: Int
                val b0 = buffer[0].toInt() and 0xFF
                if (b0 >= 0x80) {
                    runningStatus = b0
                    if (buffer.size < 3) break
                    when (runningStatus and 0xF0) {
                        0x80 -> {
                            onNote(buffer[1].toInt() and 0x7F, buffer[2].toInt() and 0x7F, false)
                            consumed = 3
                        }
                        0x90 -> {
                            val vel = buffer[2].toInt() and 0x7F
                            onNote(buffer[1].toInt() and 0x7F, vel, vel > 0)
                            consumed = 3
                        }
                        else -> consumed = 3
                    }
                } else {
                    if (buffer.size < 2) break
                    when (runningStatus and 0xF0) {
                        0x80 -> {
                            onNote(buffer[0].toInt() and 0x7F, buffer[1].toInt() and 0x7F, false)
                            consumed = 2
                        }
                        0x90 -> {
                            val vel = buffer[1].toInt() and 0x7F
                            onNote(buffer[0].toInt() and 0x7F, vel, vel > 0)
                            consumed = 2
                        }
                        else -> consumed = 2
                    }
                }
                repeat(consumed) { buffer.removeAt(0) }
            }
            if (buffer.size > 4) buffer.subList(0, buffer.size - 4).clear()
        }
    }
}
