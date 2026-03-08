package com.example.piano.ui.courses

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

/**
 * 课程视频全屏播放页
 * 横竖屏翻转时会保留播放进度与播放/暂停状态，从原位置继续播放。
 *
 * @param viewModel 由导航处通过 hiltViewModel(backStackEntry) 注入，用于保存/恢复进度
 * @param onBack 返回回调
 */
@Composable
fun CourseVideoScreen(
    viewModel: CourseVideoViewModel,
    onBack: () -> Unit
) {
    val videoUrl = viewModel.videoUrl
    if (videoUrl.isEmpty()) {
        onBack()
        return
    }
    val context = LocalContext.current
    val savedPositionMs by viewModel.currentPositionMs.collectAsState()
    val savedPlayWhenReady by viewModel.playWhenReady.collectAsState()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            seekTo(savedPositionMs)
            playWhenReady = savedPlayWhenReady
            repeatMode = Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.saveState(exoPlayer.currentPosition, exoPlayer.playWhenReady)
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                    controllerShowTimeoutMs = 3000
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White
            )
        }
    }
}
