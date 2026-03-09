package com.example.piano.ui.courses.sheet

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.components.NetworkErrorView
import com.example.piano.ui.theme.PianoTheme
import kotlinx.coroutines.delay

const val SHEET_ID_KEY = "sheetId"

/**
 * 曲谱详情入口：先隐藏状态栏并显示加载页，再进入曲谱详情（与跟弹页 FollowAlongEntry 一致）。
 */
@Composable
fun SheetDetailEntry(
    onBack: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var ready by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val decorView = window.decorView
        val insetsController = ViewCompat.getWindowInsetsController(decorView)
        insetsController?.hide(WindowInsetsCompat.Type.statusBars())
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    LaunchedEffect(Unit) {
        delay(550)
        ready = true
    }

    if (ready) {
        content()
    } else {
        SheetDetailLoadingScreen()
    }
}

/** 曲谱详情加载页：仅显示加载图标，用于进入详情前的系统栏隐藏等待。 */
@Composable
private fun SheetDetailLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PianoTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = PianoTheme.colors.primary
        )
    }
}

@Composable
fun SheetDetailScreen(
    onBack: () -> Unit,
    viewModel: SheetDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val favorited by viewModel.favorited.collectAsState()
    val useStaffNotation by viewModel.useStaffNotation.collectAsState()
    val playingSheetId by viewModel.playingSheetId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearSnackbarMessage()
        }
    }

    val title = when (state) {
        is SheetDetailUiState.Success -> (state as SheetDetailUiState.Success).title
        else -> "曲谱详情"
    }

    val successState = state as? SheetDetailUiState.Success
    val displaySheetUrl = when {
        successState == null -> null
        useStaffNotation -> successState.staffSheetDataUrl
        else -> successState.simplifiedSheetDataUrl ?: successState.staffSheetDataUrl
    }

    var showPracticeMethodDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = title,
                onBack = onBack,
                trailingContent = if (successState != null) {
                    {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showPracticeMethodDialog = true }) {
                                Text(
                                    text = "练琴",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PianoTheme.colors.primary
                                )
                            }
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (favorited) Icons.Filled.Star else Icons.Outlined.Star,
                                    contentDescription = if (favorited) "取消收藏" else "收藏",
                                    tint = if (favorited) PianoTheme.colors.primary else PianoTheme.colors.onSurface
                                )
                            }
                            Text(
                                text = if (useStaffNotation) "五线谱" else "简谱",
                                style = MaterialTheme.typography.labelMedium,
                                color = PianoTheme.colors.onSurface
                            )
                            IconButton(onClick = { viewModel.setUseStaffNotation(!useStaffNotation) }) {
                                Text(
                                    text = "切换",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PianoTheme.colors.primary
                                )
                            }
                            IconButton(onClick = { viewModel.togglePlayPause(successState.mp3Url) }) {
                                Icon(
                                    imageVector = if (playingSheetId == viewModel.currentSheetId && isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = if (isPlaying) "暂停" else "播放",
                                    tint = PianoTheme.colors.onSurface
                                )
                            }
                        }
                    }
                } else null
            )
        },
        containerColor = PianoTheme.colors.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val s = state) {
                is SheetDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PianoTheme.colors.primary)
                    }
                }
                is SheetDetailUiState.Error -> {
                    NetworkErrorView(
                        modifier = Modifier.fillMaxSize(),
                        hintText = s.message,
                        onClick = { viewModel.loadDetail() }
                    )
                }
                is SheetDetailUiState.Success -> {
                    val url = displaySheetUrl
                    if (!url.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = s.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无曲谱图片",
                                style = MaterialTheme.typography.bodyLarge,
                                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showPracticeMethodDialog) {
        PracticeMethodDialog(
            onDismiss = { showPracticeMethodDialog = false },
            onSelectMethod = { /* TODO: 根据练琴方式跳转或进入对应陪练页 */ }
        )
    }
}
