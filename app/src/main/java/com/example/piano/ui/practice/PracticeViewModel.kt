package com.example.piano.ui.practice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.piano.core.audio.AudioPitchCapture
import com.example.piano.core.audio.PitchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PracticeViewModel @Inject constructor() : ViewModel() {

    private val audioPitchCapture = AudioPitchCapture()

    val currentPitch: StateFlow<PitchResult?> = audioPitchCapture.currentPitch

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _permissionDenied = MutableStateFlow(false)
    val permissionDenied: StateFlow<Boolean> = _permissionDenied.asStateFlow()

    fun startPitchCapture() {
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
}
