package com.example.piano.core.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.piano.domain.practice.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

/**
 * 实时麦克风采集 + 音高检测，结果通过 StateFlow 暴露给 UI。
 */
class AudioPitchCapture {

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_SAMPLES = 4096  // 约 93ms @ 44.1kHz
    }

    private val _currentPitch = MutableStateFlow<PitchResult?>(null)
    val currentPitch: StateFlow<PitchResult?> = _currentPitch.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private val buffer = ShortArray(BUFFER_SIZE_SAMPLES)

    fun isRecording(): Boolean = audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING

    suspend fun startCapture(): Boolean = withContext(Dispatchers.IO) {
        val minBufSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        val bufSize = maxOf(minBufSize, BUFFER_SIZE_SAMPLES * 2)
        val record = try {
            AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, bufSize)
        } catch (e: SecurityException) {
            null
        } ?: return@withContext false

        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return@withContext false
        }

        audioRecord = record
        record.startRecording()
        _currentPitch.value = PitchResult.Listening

        try {
            while (coroutineContext.isActive && record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0 && coroutineContext.isActive) {
                    val freq = PitchDetector.detectFrequency(buffer, SAMPLE_RATE)
                    _currentPitch.value = if (freq != null) {
                        val midi = PitchDetector.frequencyToMidi(freq)
                        PitchResult.Pitch(Note(midi), freq)
                    } else {
                        PitchResult.Listening
                    }
                }
            }
        } finally {
            if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                record.stop()
            }
            record.release()
            audioRecord = null
            if (coroutineContext.isActive) {
                _currentPitch.value = null
            }
        }
        true
    }

    fun stopCapture() {
        audioRecord?.apply {
            if (recordingState == AudioRecord.RECORDSTATE_RECORDING) stop()
            release()
        }
        audioRecord = null
        _currentPitch.value = null
    }
}

sealed class PitchResult {
    data object Listening : PitchResult()
    data class Pitch(val note: Note, val frequencyHz: Float) : PitchResult()
}
