package com.example.piano.ui.courses.sheet

import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.components.NetworkErrorView
import com.example.piano.ui.theme.PianoTheme
import com.example.piano.domain.practice.Note
import com.example.piano.ui.practice.Full88PianoKeyboard
import com.example.piano.ui.practice.rememberPianoKeyboardBottomHeight
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

/** 五线谱/简谱切换图标：左「五」右「简」，双弧箭头表示循环切换。 */
@Composable
private fun NotationSwitchIcon(
    modifier: Modifier = Modifier,
    tint: Color = PianoTheme.colors.onSurface
) {
    Box(modifier = modifier.size(32.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeW = 2f.coerceAtLeast(w / 20)
            val path1 = Path().apply {
                moveTo(w * 0.25f, h * 0.35f)
                quadraticBezierTo(w * 0.5f, h * 0.1f, w * 0.75f, h * 0.65f)
            }
            val path2 = Path().apply {
                moveTo(w * 0.75f, h * 0.65f)
                quadraticBezierTo(w * 0.5f, h * 0.9f, w * 0.25f, h * 0.35f)
            }
            drawPath(path1, tint, style = Stroke(width = strokeW, cap = StrokeCap.Round))
            drawPath(path2, tint, style = Stroke(width = strokeW, cap = StrokeCap.Round))
        }
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "五",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = tint
            )
            Text(
                text = "简",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = tint
            )
        }
    }
}

@Composable
fun SheetDetailScreen(
    onBack: () -> Unit,
    onNavigateToVirtualPractice: (sheetId: Long) -> Unit = {},
    viewModel: SheetDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val favorited by viewModel.favorited.collectAsState()
    val useStaffNotation by viewModel.useStaffNotation.collectAsState()
    val playingSheetId by viewModel.playingSheetId.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val activeMidiKeys by viewModel.activeMidiKeys.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val showVirtualPracticeKeyboard by viewModel.showVirtualPracticeKeyboard.collectAsState()
    val virtualPracticeNotes by viewModel.virtualPracticeNotes.collectAsState()
    val virtualPracticeLoading by viewModel.virtualPracticeLoading.collectAsState()
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

    /** 当前曲谱正在播放时弹出键盘（随 MIDI 高亮）；虚拟键盘练琴时显示练琴键盘而非随播 */
    val showPlaybackKeyboard = successState != null &&
        playingSheetId == viewModel.currentSheetId &&
        isPlaying

    /** 虚拟键盘练琴：底部键盘 + 对绿错红逻辑，弹完后弹窗显示结果 */
    val practiceNotes = virtualPracticeNotes
    val showPracticeKeyboard = showVirtualPracticeKeyboard && practiceNotes != null && practiceNotes.isNotEmpty()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(PianoTheme.colors.surface)) {
                Spacer(modifier = Modifier.height(30.dp))
                BackTitleTopBar(
                    title = "",
                    onBack = onBack,
                    trailingContent = if (successState != null) {
                        {
                            Row(
                                modifier = Modifier.width(220.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = { showPracticeMethodDialog = true }) {
                                    Text(
                                        text = "练琴",
                                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 16.sp),
                                        color = PianoTheme.colors.primary
                                    )
                                }
                                IconButton(onClick = { viewModel.toggleFavorite() }) {
                                    Icon(
                                        imageVector = if (favorited) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = if (favorited) "取消收藏" else "收藏",
                                        tint = if (favorited) PianoTheme.colors.primary else PianoTheme.colors.onSurface,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.setUseStaffNotation(!useStaffNotation) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    NotationSwitchIcon(
                                        tint = if (useStaffNotation) PianoTheme.colors.primary else PianoTheme.colors.onSurface
                                    )
                                }
                                IconButton(onClick = { viewModel.togglePlayPause(successState.mp3Url) }) {
                                    Icon(
                                        imageVector = if (playingSheetId == viewModel.currentSheetId && isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                        contentDescription = if (isPlaying) "暂停" else "播放",
                                        tint = PianoTheme.colors.onSurface,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    } else null
                )
            }
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
                        var scale by remember { mutableStateOf(1f) }
                        var offset by remember { mutableStateOf(Offset.Zero) }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clipToBounds()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.2f, 5f)
                                        offset += pan
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .layout { measurable, constraints ->
                                        val loose = constraints.copy(maxHeight = Int.MAX_VALUE)
                                        val placeable = measurable.measure(loose)
                                        layout(placeable.width, placeable.height) {
                                            placeable.place(0, 0)
                                        }
                                    }
                                    .graphicsLayer(
                                        scaleX = scale,
                                        scaleY = scale,
                                        translationX = offset.x,
                                        translationY = offset.y
                                    )
                            ) {
                                AsyncImage(
                                    model = url,
                                    contentDescription = s.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentScale = ContentScale.FillWidth
                                )
                            }
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

            if (showVirtualPracticeKeyboard && virtualPracticeLoading) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(PianoTheme.colors.surface)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "正在加载 MIDI…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PianoTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = PianoTheme.colors.primary
                    )
                }
            }

            if (showPracticeKeyboard) {
                val notes = practiceNotes!!
                var currentIndex by remember(notes) { mutableStateOf(0) }
                val records = remember(notes) { mutableStateListOf<com.example.piano.domain.practice.CorrectionRecord>() }
                var wrongMidi by remember { mutableStateOf<Int?>(null) }
                var correctMidi by remember { mutableStateOf<Int?>(null) }
                var finished by remember { mutableStateOf(false) }
                var showPracticeResultDialog by remember { mutableStateOf(false) }

                fun onPracticeKeyPress(note: Note) {
                    if (finished) return
                    val expected = notes.getOrNull(currentIndex) ?: return
                    val correct = note.midi == expected.midi
                    records.add(
                        com.example.piano.domain.practice.CorrectionRecord(
                            index = currentIndex,
                            expected = expected,
                            actual = note,
                            isCorrect = correct
                        )
                    )
                    if (correct) {
                        wrongMidi = null
                        correctMidi = note.midi
                        currentIndex++
                        if (currentIndex >= notes.size) {
                            finished = true
                            showPracticeResultDialog = true
                        }
                    } else {
                        wrongMidi = note.midi
                    }
                }

                LaunchedEffect(wrongMidi) {
                    if (wrongMidi != null) {
                        delay(400)
                        wrongMidi = null
                    }
                }
                LaunchedEffect(correctMidi) {
                    if (correctMidi != null) {
                        delay(350)
                        correctMidi = null
                    }
                }

                val keyboardHeightDp = rememberPianoKeyboardBottomHeight()
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(PianoTheme.colors.surface)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                finished = true
                                showPracticeResultDialog = true
                            }
                        ) {
                            Text("提前结束", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "虚拟键盘练琴",
                            style = MaterialTheme.typography.titleSmall,
                            color = PianoTheme.colors.onSurface
                        )
                        IconButton(onClick = { viewModel.dismissVirtualPracticeKeyboard() }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "收起键盘",
                                tint = PianoTheme.colors.onSurface
                            )
                        }
                    }
                    Full88PianoKeyboard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyboardHeightDp),
                        highlightMidi = notes.getOrNull(currentIndex)?.midi,
                        wrongMidi = wrongMidi,
                        correctMidi = correctMidi,
                        showOctaveLabels = true,
                        onKeyPress = ::onPracticeKeyPress
                    )
                }

                if (showPracticeResultDialog) {
                    val total = notes.size
                    val wrongCount = records.filter { !it.isCorrect }.map { it.index }.toSet().size
                    val accuracy = if (total > 0) ((total - wrongCount) * 100 / total) else 100
                    Dialog(onDismissRequest = { }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(24.dp)) {
                                Text(
                                    text = "练习完成",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "正确率：（$total - $wrongCount 错）/ $total = $accuracy%",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PianoTheme.colors.primary,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                                if (wrongCount > 0) {
                                    Text(
                                        text = "错了 $wrongCount 个音",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 20.dp),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = {
                                            showPracticeResultDialog = false
                                            currentIndex = 0
                                            records.clear()
                                            wrongMidi = null
                                            correctMidi = null
                                            finished = false
                                        }
                                    ) {
                                        Text("再来一次")
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = {
                                            showPracticeResultDialog = false
                                            viewModel.dismissVirtualPracticeKeyboard()
                                        }
                                    ) {
                                        Text("关闭")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showPlaybackKeyboard && !showPracticeKeyboard) {
                val keyboardHeightDp = rememberPianoKeyboardBottomHeight()
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(PianoTheme.colors.surface)
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "随播键盘",
                            style = MaterialTheme.typography.titleSmall,
                            color = PianoTheme.colors.onSurface
                        )
                        IconButton(onClick = { viewModel.togglePlayPause(successState?.mp3Url) }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "收起键盘",
                                tint = PianoTheme.colors.onSurface
                            )
                        }
                    }
                    Full88PianoKeyboard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyboardHeightDp),
                        activeMidiSet = activeMidiKeys,
                        showOctaveLabels = true,
                        onKeyPress = { }
                    )
                }
            }
        }
    }

    if (showPracticeMethodDialog) {
        PracticeMethodDialog(
            onDismiss = { showPracticeMethodDialog = false },
            onSelectMethod = { method ->
                when (method) {
                    PracticeMethod.VIRTUAL_KEYBOARD -> {
                        showPracticeMethodDialog = false
                        viewModel.startVirtualPractice()
                    }
                    PracticeMethod.SOUND_RECOGNITION,
                    PracticeMethod.BLUETOOTH_MIDI -> {
                        // TODO: 跳转对应陪练页
                    }
                }
            }
        )
    }
}
