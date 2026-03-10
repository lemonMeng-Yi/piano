package com.example.piano.ui.courses.sheet

import android.content.pm.PackageManager
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
import com.example.piano.core.audio.PianoKeySound
import com.example.piano.core.audio.PitchResult
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
    val showSoundPracticeKeyboard by viewModel.showSoundPracticeKeyboard.collectAsState()
    val soundPracticeNotes by viewModel.soundPracticeNotes.collectAsState()
    val soundPracticeLoading by viewModel.soundPracticeLoading.collectAsState()
    val soundPracticeCurrentPitch by viewModel.soundPracticeCurrentPitch.collectAsState()
    val soundPracticeRecording by viewModel.soundPracticeRecording.collectAsState()
    val showBluetoothPracticeKeyboard by viewModel.showBluetoothPracticeKeyboard.collectAsState()
    val bluetoothPracticeNotes by viewModel.bluetoothPracticeNotes.collectAsState()
    val bluetoothPracticeLoading by viewModel.bluetoothPracticeLoading.collectAsState()
    val bluetoothPracticeCurrentPitch by viewModel.bluetoothPracticeCurrentPitch.collectAsState()
    val midiConnected by viewModel.midiConnected.collectAsState()
    val scannedBleMidiDevices by viewModel.scannedBleMidiDevices.collectAsState()
    val isScanningBle by viewModel.isScanningBle.collectAsState()
    val isBluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val midiConnectionError by viewModel.midiConnectionError.collectAsState()
    val isMidiSupported = viewModel.isMidiSupported
    val context = LocalContext.current

    /** 选择蓝牙 MIDI 时先弹出「蓝牙MIDI设备」弹窗，连接成功后再关闭弹窗并弹出键盘 */
    var showBluetoothMidiDialog by remember { mutableStateOf(false) }
    LaunchedEffect(showBluetoothMidiDialog, midiConnected) {
        if (showBluetoothMidiDialog && midiConnected) {
            showBluetoothMidiDialog = false
            viewModel.startBluetoothPractice()
        }
    }

    val connectedBluetoothDevice by viewModel.connectedBluetoothDevice.collectAsState()

    /** 蓝牙扫描权限与打开蓝牙（参照 FollowAlongScreen） */
    val scanPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) viewModel.startBleMidiScan()
    }
    fun doPermissionAndStartScan() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissions.isEmpty()) viewModel.startBleMidiScan()
        else scanPermissionLauncher.launch(permissions.toTypedArray())
    }
    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) doPermissionAndStartScan()
    }
    fun ensureScanPermissionAndStartScan() {
        if (!viewModel.isBluetoothEnabled()) {
            bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            return
        }
        doPermissionAndStartScan()
    }

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

    /** 声音识别：先弹权限弹窗，同意后再显示键盘 */
    var pendingSoundPermissionRequest by remember { mutableStateOf(false) }
    val soundPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startSoundPractice()
        else viewModel.onSoundPracticePermissionDenied()
    }
    LaunchedEffect(pendingSoundPermissionRequest) {
        if (!pendingSoundPermissionRequest) return@LaunchedEffect
        pendingSoundPermissionRequest = false
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startSoundPractice()
        } else {
            soundPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    /** 当前曲谱正在播放时弹出键盘（随 MIDI 高亮）；虚拟键盘练琴时显示练琴键盘而非随播 */
    val showPlaybackKeyboard = successState != null &&
        playingSheetId == viewModel.currentSheetId &&
        isPlaying

    /** 虚拟键盘练琴：底部键盘 + 对绿错红逻辑，弹完后弹窗显示结果 */
    val practiceNotes = virtualPracticeNotes
    val showPracticeKeyboard = showVirtualPracticeKeyboard && practiceNotes != null && practiceNotes.isNotEmpty()

    /** 声音识别练琴：底部键盘 + 麦克风识别比对，弹完后弹窗显示结果 */
    val soundNotes = soundPracticeNotes
    val showSoundPractice = showSoundPracticeKeyboard && soundNotes != null && soundNotes.isNotEmpty()

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

            if (showSoundPracticeKeyboard && soundPracticeLoading) {
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

            if (showBluetoothPracticeKeyboard && bluetoothPracticeLoading) {
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

            if (showSoundPractice) {
                val notes = soundNotes!!
                var currentIndex by remember(notes) { mutableStateOf(0) }
                val records = remember(notes) { mutableStateListOf<com.example.piano.domain.practice.CorrectionRecord>() }
                var wrongMidi by remember { mutableStateOf<Int?>(null) }
                var correctMidi by remember { mutableStateOf<Int?>(null) }
                var finished by remember { mutableStateOf(false) }
                var showResultDialog by remember { mutableStateOf(false) }
                var hasAdvancedForCurrentIndex by remember { mutableStateOf(false) }
                var lastRecordedWrongMidi by remember { mutableStateOf<Int?>(null) }
                var hasStartedCapture by remember { mutableStateOf(false) }

                LaunchedEffect(showSoundPractice) {
                    if (showSoundPractice && !hasStartedCapture) {
                        hasStartedCapture = true
                        viewModel.startPitchCapture()
                    }
                }

                LaunchedEffect(currentIndex) {
                    hasAdvancedForCurrentIndex = false
                    lastRecordedWrongMidi = null
                }

                LaunchedEffect(soundPracticeCurrentPitch, currentIndex, finished) {
                    if (finished) return@LaunchedEffect
                    val pitch = soundPracticeCurrentPitch as? PitchResult.Pitch ?: return@LaunchedEffect
                    val expected = notes.getOrNull(currentIndex) ?: return@LaunchedEffect
                    if (pitch.note.midi == expected.midi) {
                        if (hasAdvancedForCurrentIndex) return@LaunchedEffect
                        hasAdvancedForCurrentIndex = true
                        wrongMidi = null
                        correctMidi = expected.midi
                        records.add(
                            com.example.piano.domain.practice.CorrectionRecord(
                                index = currentIndex,
                                expected = expected,
                                actual = pitch.note,
                                isCorrect = true
                            )
                        )
                        currentIndex++
                        if (currentIndex >= notes.size) {
                            finished = true
                            showResultDialog = true
                        }
                    } else {
                        val prevNote = notes.getOrNull(currentIndex - 1)
                        if (currentIndex > 0 && pitch.note.midi == prevNote?.midi) return@LaunchedEffect
                        wrongMidi = pitch.note.midi
                        if (pitch.note.midi == lastRecordedWrongMidi) return@LaunchedEffect
                        lastRecordedWrongMidi = pitch.note.midi
                        records.add(
                            com.example.piano.domain.practice.CorrectionRecord(
                                index = currentIndex,
                                expected = expected,
                                actual = pitch.note,
                                isCorrect = false
                            )
                        )
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

                DisposableEffect(Unit) {
                    onDispose { viewModel.stopPitchCapture() }
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
                                showResultDialog = true
                            }
                        ) {
                            Text("提前结束", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "声音识别练琴",
                            style = MaterialTheme.typography.titleSmall,
                            color = PianoTheme.colors.onSurface
                        )
                        IconButton(onClick = { viewModel.dismissSoundPracticeKeyboard() }) {
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
                        onKeyPress = { }
                    )
                }

                if (showResultDialog) {
                    val total = notes.size
                    val playedCount = currentIndex
                    val wrongCount = records.filter { !it.isCorrect }.map { it.index }.toSet().size
                    val progressPercent = if (total > 0) playedCount * 100 / total else 0
                    val accuracyPercent = if (playedCount > 0) (playedCount - wrongCount) * 100 / playedCount else 100
                    Dialog(onDismissRequest = { }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(24.dp)) {
                                Text(
                                    text = "练习结果",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "进度：已弹 $playedCount / 总 $total 个音（$progressPercent%）",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PianoTheme.colors.onSurface,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                                if (playedCount > 0) {
                                    Text(
                                        text = "正确率（按已弹数量）：（$playedCount - $wrongCount 错）/ $playedCount = $accuracyPercent%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = PianoTheme.colors.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                } else {
                                    Text(
                                        text = "尚未弹奏，无正确率",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
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
                                            showResultDialog = false
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
                                            showResultDialog = false
                                            viewModel.dismissSoundPracticeKeyboard()
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

            if (showBluetoothPracticeKeyboard) {
                val btNotes = bluetoothPracticeNotes
                var btCurrentIndex by remember(btNotes) { mutableStateOf(0) }
                val btRecords = remember(btNotes) { mutableStateListOf<com.example.piano.domain.practice.CorrectionRecord>() }
                var btWrongMidi by remember { mutableStateOf<Int?>(null) }
                var btCorrectMidi by remember { mutableStateOf<Int?>(null) }
                var btFinished by remember { mutableStateOf(false) }
                var btShowResultDialog by remember { mutableStateOf(false) }
                var btHasAdvanced by remember { mutableStateOf(false) }
                var btLastRecordedWrong by remember { mutableStateOf<Int?>(null) }

                DisposableEffect(Unit) {
                    onDispose { viewModel.dismissBluetoothPracticeKeyboard() }
                }

                if (btNotes != null) {
                    LaunchedEffect(btCurrentIndex) {
                        btHasAdvanced = false
                        btLastRecordedWrong = null
                    }
                    LaunchedEffect(bluetoothPracticeCurrentPitch, btCurrentIndex, btFinished) {
                        if (btFinished) return@LaunchedEffect
                        val pitch = bluetoothPracticeCurrentPitch as? PitchResult.Pitch ?: return@LaunchedEffect
                        val expected = btNotes.getOrNull(btCurrentIndex) ?: return@LaunchedEffect
                        if (pitch.note.midi == expected.midi) {
                            if (btHasAdvanced) return@LaunchedEffect
                            btHasAdvanced = true
                            btWrongMidi = null
                            btCorrectMidi = expected.midi
                            btRecords.add(
                                com.example.piano.domain.practice.CorrectionRecord(
                                    index = btCurrentIndex,
                                    expected = expected,
                                    actual = pitch.note,
                                    isCorrect = true
                                )
                            )
                            btCurrentIndex++
                            if (btCurrentIndex >= btNotes.size) {
                                btFinished = true
                                btShowResultDialog = true
                            }
                        } else {
                            val prevNote = btNotes.getOrNull(btCurrentIndex - 1)
                            if (btCurrentIndex > 0 && pitch.note.midi == prevNote?.midi) return@LaunchedEffect
                            btWrongMidi = pitch.note.midi
                            if (pitch.note.midi == btLastRecordedWrong) return@LaunchedEffect
                            btLastRecordedWrong = pitch.note.midi
                            btRecords.add(
                                com.example.piano.domain.practice.CorrectionRecord(
                                    index = btCurrentIndex,
                                    expected = expected,
                                    actual = pitch.note,
                                    isCorrect = false
                                )
                            )
                        }
                    }
                    LaunchedEffect(btWrongMidi) {
                        if (btWrongMidi != null) {
                            delay(400)
                            btWrongMidi = null
                        }
                    }
                    LaunchedEffect(btCorrectMidi) {
                        if (btCorrectMidi != null) {
                            delay(350)
                            btCorrectMidi = null
                        }
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
                                btFinished = true
                                btShowResultDialog = true
                            }
                        ) {
                            Text("提前结束", style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "蓝牙MIDI练琴",
                            style = MaterialTheme.typography.titleSmall,
                            color = PianoTheme.colors.onSurface
                        )
                        IconButton(onClick = { viewModel.dismissBluetoothPracticeKeyboard() }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "收起键盘",
                                tint = PianoTheme.colors.onSurface
                            )
                        }
                    }
                    if (!midiConnected) {
                        Column(Modifier.padding(vertical = 8.dp)) {
                            if (!isMidiSupported) {
                                Text(
                                    text = "当前设备不支持 MIDI",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PianoTheme.colors.error
                                )
                            } else {
                                if (!isBluetoothEnabled) {
                                    Text(
                                        text = "请先打开手机蓝牙",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PianoTheme.colors.error
                                    )
                                }
                                Button(
                                    onClick = { ensureScanPermissionAndStartScan() },
                                    enabled = !isScanningBle
                                ) {
                                    Text(if (isScanningBle) "扫描中…" else "扫描蓝牙 MIDI 设备")
                                }
                                midiConnectionError?.let { err ->
                                    Text(
                                        text = err,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PianoTheme.colors.error,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    TextButton(onClick = { viewModel.clearMidiError() }) { Text("清除") }
                                }
                                if (scannedBleMidiDevices.isEmpty() && !isScanningBle && isBluetoothEnabled) {
                                    Text(
                                        text = "打开电钢蓝牙后点击「扫描蓝牙 MIDI 设备」，再在列表中点击设备连接",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                } else if (scannedBleMidiDevices.isNotEmpty()) {
                                    Text(
                                        text = "点击设备连接",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                    scannedBleMidiDevices.forEach { device ->
                                        TextButton(onClick = { viewModel.connectBluetoothMidi(device) }) {
                                            Text(viewModel.getBluetoothDeviceDisplayName(device))
                                        }
                                    }
                                }
                            }
                        }
                    } else if (btNotes == null) {
                        Text(
                            text = "已连接，正在加载 MIDI…",
                            style = MaterialTheme.typography.bodySmall,
                            color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    Full88PianoKeyboard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(keyboardHeightDp),
                        highlightMidi = btNotes?.getOrNull(btCurrentIndex)?.midi,
                        wrongMidi = btWrongMidi,
                        correctMidi = btCorrectMidi,
                        showOctaveLabels = true,
                        onKeyPress = { }
                    )
                }

                if (btNotes != null && btShowResultDialog) {
                    val total = btNotes.size
                    val playedCount = btCurrentIndex
                    val wrongCount = btRecords.filter { !it.isCorrect }.map { it.index }.toSet().size
                    val progressPercent = if (total > 0) playedCount * 100 / total else 0
                    val accuracyPercent = if (playedCount > 0) (playedCount - wrongCount) * 100 / playedCount else 100
                    Dialog(onDismissRequest = { }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(24.dp)) {
                                Text(
                                    text = "练习结果",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "进度：已弹 $playedCount / 总 $total 个音（$progressPercent%）",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PianoTheme.colors.onSurface,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                                if (playedCount > 0) {
                                    Text(
                                        text = "正确率（按已弹数量）：$accuracyPercent%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = PianoTheme.colors.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                } else {
                                    Text(
                                        text = "尚未弹奏，无正确率",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
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
                                            btShowResultDialog = false
                                            btCurrentIndex = 0
                                            btRecords.clear()
                                            btWrongMidi = null
                                            btCorrectMidi = null
                                            btFinished = false
                                        }
                                    ) {
                                        Text("再来一次")
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Button(
                                        onClick = {
                                            btShowResultDialog = false
                                            viewModel.dismissBluetoothPracticeKeyboard()
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

            if (showPracticeKeyboard) {
                val notes = practiceNotes!!
                val keySound = remember { PianoKeySound() }
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
                        playKeySound = { keySound.playNote(it) },
                        onKeyPress = ::onPracticeKeyPress
                    )
                }

                if (showPracticeResultDialog) {
                    val total = notes.size
                    val playedCount = currentIndex
                    val wrongCount = records.filter { !it.isCorrect }.map { it.index }.toSet().size
                    val progressPercent = if (total > 0) playedCount * 100 / total else 0
                    val accuracyPercent = if (playedCount > 0) (playedCount - wrongCount) * 100 / playedCount else 100
                    Dialog(onDismissRequest = { }) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(24.dp)) {
                                Text(
                                    text = "练习结果",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "进度：已弹 $playedCount / 总 $total 个音（$progressPercent%）",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PianoTheme.colors.onSurface,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                                if (playedCount > 0) {
                                    Text(
                                        text = "正确率（按已弹数量）：（$playedCount - $wrongCount 错）/ $playedCount = $accuracyPercent%",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = PianoTheme.colors.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                } else {
                                    Text(
                                        text = "尚未弹奏，无正确率",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
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

            if (showPlaybackKeyboard && !showPracticeKeyboard && !showSoundPractice) {
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
                    PracticeMethod.SOUND_RECOGNITION -> {
                        showPracticeMethodDialog = false
                        pendingSoundPermissionRequest = true
                    }
                    PracticeMethod.BLUETOOTH_MIDI -> {
                        showPracticeMethodDialog = false
                        showBluetoothMidiDialog = true
                    }
                }
            }
        )
    }

    if (showBluetoothMidiDialog) {
        BluetoothMidiDeviceDialog(
            onDismiss = {
                showBluetoothMidiDialog = false
                viewModel.stopBleMidiScan()
            },
            scannedDevices = scannedBleMidiDevices,
            isScanning = isScanningBle,
            isBluetoothEnabled = isBluetoothEnabled,
            connectedDevice = connectedBluetoothDevice,
            getDeviceDisplayName = viewModel::getBluetoothDeviceDisplayName,
            onScanClick = { ensureScanPermissionAndStartScan() },
            onDeviceClick = { viewModel.connectBluetoothMidi(it) }
        )
    }
}
