package com.example.piano.core.audio

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 曲谱列表 MP3 播放：使用 ExoPlayer 播放 mp3_url，支持播放/暂停/切换曲目。
 */
@Singleton
class SheetAudioPlaybackManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var positionUpdateJob: Job? = null

    private var _player: ExoPlayer? = null
    private val player: ExoPlayer
        get() {
            if (_player == null) {
                _player = ExoPlayer.Builder(context).build().apply {
                    repeatMode = Player.REPEAT_MODE_OFF
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_ENDED) {
                                _playingSheetId.value = null
                                _isPlaying.value = false
                                stopPositionUpdates()
                                _playbackPositionMs.value = 0L
                            }
                        }
                    })
                }
            }
            return _player!!
        }

    private val _playingSheetId = MutableStateFlow<Long?>(null)
    val playingSheetId: StateFlow<Long?> = _playingSheetId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /** 当前播放位置（毫秒），用于与 MIDI 事件同步高亮键盘 */
    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = scope.launch {
            while (isActive) {
                _player?.currentPosition?.let { _playbackPositionMs.value = it }
                delay(50)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    /** 有音频 URL 时点击：播放 / 暂停 / 切换曲目 */
    fun toggle(sheetId: Long, mp3Url: String?, onNoAudio: () -> Unit) {
        if (mp3Url.isNullOrBlank()) {
            onNoAudio()
            return
        }
        val p = player
        when {
            _playingSheetId.value == sheetId && _isPlaying.value -> {
                p.pause()
                _isPlaying.value = false
                stopPositionUpdates()
            }
            _playingSheetId.value == sheetId && !_isPlaying.value -> {
                p.play()
                _isPlaying.value = true
                startPositionUpdates()
            }
            else -> {
                p.stop()
                p.clearMediaItems()
                p.setMediaItem(MediaItem.fromUri(Uri.parse(mp3Url)))
                p.prepare()
                p.play()
                _playingSheetId.value = sheetId
                _isPlaying.value = true
                startPositionUpdates()
            }
        }
    }

    fun pause() {
        _player?.pause()
        _isPlaying.value = false
        stopPositionUpdates()
    }

    fun stop() {
        _player?.stop()
        _player?.clearMediaItems()
        _playingSheetId.value = null
        _isPlaying.value = false
        stopPositionUpdates()
        _playbackPositionMs.value = 0L
    }

    fun release() {
        stopPositionUpdates()
        _player?.release()
        _player = null
        _playingSheetId.value = null
        _isPlaying.value = false
        _playbackPositionMs.value = 0L
    }
}
