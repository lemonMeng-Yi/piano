package com.example.piano.ui.courses.learn

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.piano.core.network.util.ResponseState

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

    val detail by viewModel.courseDetail.collectAsState()
    val completeResult by viewModel.completeResult.collectAsState()
    var showCompletedHint by remember { mutableStateOf(false) }

    LaunchedEffect(completeResult) {
        if (completeResult is ResponseState.Success) {
            showCompletedHint = true
            kotlinx.coroutines.delay(2000)
            showCompletedHint = false
        }
    }

    // 视频播放结束时：未完成则自动标记完成，已完成则什么都不做
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_ENDED) {
                    if (viewModel.courseDetail.value?.isCompleted != 1) {
                        viewModel.markComplete()
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose { exoPlayer.removeListener(listener) }
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

        if (showCompletedHint) {
            Text(
                text = "已完成",
                color = Color(0xFF4CAF50),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}
