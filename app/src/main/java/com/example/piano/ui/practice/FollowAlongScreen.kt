package com.example.piano.ui.practice

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.core.audio.PitchResult
import com.example.piano.domain.practice.CoursePieces
import com.example.piano.domain.practice.CorrectionRecord
import com.example.piano.domain.practice.Note
import com.example.piano.domain.practice.PracticePiece
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.theme.PianoTheme
import kotlinx.coroutines.delay

/** 跟弹页入口：先显示加载页，等渲染稳定后再显示跟弹内容。
 * @param pieceId 课程曲目 id，见 [CoursePieces]，null 则用默认 C 大调音阶 */
@Composable
fun FollowAlongEntry(
    pieceId: String?,
    onBack: () -> Unit
) {
    val piece = remember(pieceId) { CoursePieces.getPiece(pieceId) }
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
        FollowAlongScreen(piece = piece, onBack = onBack)
    } else {
        FollowAlongLoadingScreen()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowAlongScreen(
    piece: PracticePiece,
    onBack: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    val records = remember { mutableStateListOf<CorrectionRecord>() }
    /** 仅在此处设为非 null：点击错键 或 麦克风判定为错音。弹对或 400ms 后清除。红键只跟这个走。 */
    var wrongMidi by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }
    var hasAdvancedForCurrentIndex by remember { mutableStateOf(false) }
    /** 本索引下已记录过的错音 MIDI（麦克风），避免同一错键重复记多条 */
    var lastRecordedWrongMidi by remember { mutableStateOf<Int?>(null) }

    val viewModel: PracticeViewModel = hiltViewModel()
    val currentPitch by viewModel.currentPitch.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val permissionDenied by viewModel.permissionDenied.collectAsState()
    val useMidiSource by viewModel.useMidiSource.collectAsState()
    val midiConnected by viewModel.midiConnected.collectAsState()
    val scannedBleMidiDevices by viewModel.scannedBleMidiDevices.collectAsState()
    val isScanningBle by viewModel.isScanningBle.collectAsState()
    val midiConnectionError by viewModel.midiConnectionError.collectAsState()
    val isBluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val isMidiSupported = viewModel.isMidiSupported
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startPitchCapture()
        else { viewModel.onPermissionDenied(); viewModel.stopPitchCapture() }
    }

    // 扫描蓝牙 MIDI 所需权限（BLUETOOTH_SCAN + 定位，用于发现设备）
    val scanPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) viewModel.startBleMidiScan()
    }

    /** 请求扫描/连接权限后开始扫描（不检查蓝牙是否已开） */
    fun doPermissionAndStartScan() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissions.isEmpty()) {
            viewModel.startBleMidiScan()
        } else {
            scanPermissionLauncher.launch(permissions.toTypedArray())
        }
    }

    // 请求打开蓝牙（未开时先弹出系统对话框，打开后再请求权限并扫描）
    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            doPermissionAndStartScan()
        }
    }

    /** 若蓝牙未开先请求打开，否则请求权限并开始扫描 */
    fun ensureScanPermissionAndStartScan() {
        if (!viewModel.isBluetoothEnabled()) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            bluetoothEnableLauncher.launch(intent)
            return
        }
        doPermissionAndStartScan()
    }

    fun startPlaying() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startPitchCapture()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    LaunchedEffect(currentIndex) {
        hasAdvancedForCurrentIndex = false
        lastRecordedWrongMidi = null
    }

    // 识别音高与【当前应弹】对比：一致则显示下一个要弹的键，不一致则提示错误并红色显示弹错的键（与点击一致）
    LaunchedEffect(currentPitch, currentIndex, finished) {
        if (finished) return@LaunchedEffect
        val pitch = currentPitch as? PitchResult.Pitch ?: return@LaunchedEffect
        // 必须用 effect 内的 currentIndex 取「当前应弹」，避免用到已更新后的「下一个」
        val expected = piece.notes.getOrNull(currentIndex) ?: return@LaunchedEffect
        if (pitch.note.midi == expected.midi) {
            // 一致：记对，前进，显示下一个要弹的键；立刻清除红键
            if (hasAdvancedForCurrentIndex) return@LaunchedEffect
            hasAdvancedForCurrentIndex = true
            wrongMidi = null
            records.add(
                CorrectionRecord(
                    index = currentIndex,
                    expected = expected,
                    actual = pitch.note,
                    isCorrect = true
                )
            )
            currentIndex++
            if (currentIndex >= piece.notes.size) finished = true
        } else {
            // 刚弹对后仍按住键时，currentIndex 已前进，不要把「上一音」误判为错
            val prevNote = piece.notes.getOrNull(currentIndex - 1)
            if (currentIndex > 0 && pitch.note.midi == prevNote?.midi) return@LaunchedEffect
            // 不一致：记错、提示错误、红键只在这里设为弹错的那个键
            wrongMidi = pitch.note.midi
            if (pitch.note.midi == lastRecordedWrongMidi) return@LaunchedEffect
            lastRecordedWrongMidi = pitch.note.midi
            records.add(
                CorrectionRecord(
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

    fun onKeyPress(note: Note) {
        if (finished) return
        val expected = piece.notes.getOrNull(currentIndex) ?: return
        val correct = note.midi == expected.midi
        records.add(
            CorrectionRecord(
                index = currentIndex,
                expected = expected,
                actual = note,
                isCorrect = correct
            )
        )
        if (correct) {
            wrongMidi = null
            currentIndex++
            if (currentIndex >= piece.notes.size) finished = true
        } else {
            wrongMidi = note.midi
        }
    }

    val activity = context as? Activity

    DisposableEffect(Unit) {
        val window = activity?.window ?: return@DisposableEffect onDispose {}
        val decorView = window.decorView
        val insetsController = ViewCompat.getWindowInsetsController(decorView)
        insetsController?.hide(WindowInsetsCompat.Type.statusBars())
        insetsController?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        onDispose {
            insetsController?.show(WindowInsetsCompat.Type.statusBars())
            viewModel.stopPitchCapture()
        }
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(title = "跟弹纠错", onBack = onBack)
        },
        containerColor = PianoTheme.colors.background
    ) { paddingValues ->
        // 整体上移：上方说明可滚动，下方琴键固定高度（不随错误提示变化）
        val config = LocalConfiguration.current
        val screenHeightDp = config.screenHeightDp.dp
        val keyboardHeightDp = (screenHeightDp * 0.22f).coerceIn(100.dp, 150.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                if (!finished) {
                    LaunchedEffect(useMidiSource) {
                        if (useMidiSource) ensureScanPermissionAndStartScan()
                    }
                    instructionCard(
                        piece = piece,
                        currentIndex = currentIndex,
                        records = records,
                        wrongMidi = wrongMidi,
                        isRecording = isRecording,
                        currentPitch = currentPitch,
                        permissionDenied = permissionDenied,
                        useMidiSource = useMidiSource,
                        midiConnected = midiConnected,
                        scannedBleMidiDevices = scannedBleMidiDevices,
                        isScanningBle = isScanningBle,
                        isBluetoothEnabled = isBluetoothEnabled,
                        midiConnectionError = midiConnectionError,
                        isMidiSupported = isMidiSupported,
                        getBluetoothDeviceDisplayName = viewModel::getBluetoothDeviceDisplayName,
                        onUseMidiSource = viewModel::setUseMidiSource,
                        onScanBleMidi = ::ensureScanPermissionAndStartScan,
                        onConnectBluetoothMidi = viewModel::connectBluetoothMidi,
                        onDisconnectMidi = viewModel::disconnectMidi,
                        onClearMidiError = viewModel::clearMidiError,
                        onStartPlaying = ::startPlaying,
                        onStopPlaying = { viewModel.stopPitchCapture() }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    expectedVsActualList(piece = piece, records = records)
                } else {
                    finishedCard(
                        piece = piece,
                        records = records,
                        onRestart = {
                            currentIndex = 0
                            records.clear()
                            wrongMidi = null
                            finished = false
                            hasAdvancedForCurrentIndex = false
                            lastRecordedWrongMidi = null
                            viewModel.stopPitchCapture()
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    expectedVsActualList(piece = piece, records = records)
                }
                if (!finished && records.isNotEmpty() && !records.last().isCorrect) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = records.last().errorMessage(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PianoTheme.colors.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            Full88PianoKeyboard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(keyboardHeightDp),
                highlightMidi = piece.notes.getOrNull(currentIndex)?.midi,
                wrongMidi = wrongMidi,
                showOctaveLabels = true,
                onKeyPress = ::onKeyPress
            )
        }
    }
}

@Composable
private fun expectedVsActualList(
    piece: PracticePiece,
    records: List<CorrectionRecord>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface.copy(alpha = 0.8f))
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "过程",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            piece.notes.forEachIndexed { index, expected ->
                val lastForIndex = records.filter { it.index == index }.lastOrNull()
                val actualText = when {
                    lastForIndex == null -> "—"
                    lastForIndex.isCorrect -> "✓ ${lastForIndex.actual.displayName()}"
                    else -> "${lastForIndex.actual.displayName()} ✗"
                }
                val isWrong = lastForIndex != null && !lastForIndex.isCorrect
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}. ${expected.displayName()}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.widthIn(min = 56.dp),
                        fontWeight = if (lastForIndex != null && lastForIndex.isCorrect) FontWeight.Medium else FontWeight.Normal
                    )
                    Text(
                        text = "→",
                        style = MaterialTheme.typography.bodySmall,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                    Text(
                        text = actualText,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            lastForIndex == null -> PianoTheme.colors.onSurface.copy(alpha = 0.5f)
                            isWrong -> PianoTheme.colors.error
                            else -> PianoTheme.colors.primary
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun instructionCard(
    piece: PracticePiece,
    currentIndex: Int,
    records: List<CorrectionRecord>,
    wrongMidi: Int?,
    isRecording: Boolean,
    currentPitch: PitchResult?,
    permissionDenied: Boolean,
    useMidiSource: Boolean,
    midiConnected: Boolean,
    scannedBleMidiDevices: List<BluetoothDevice>,
    isScanningBle: Boolean,
    isBluetoothEnabled: Boolean,
    midiConnectionError: String?,
    isMidiSupported: Boolean,
    getBluetoothDeviceDisplayName: (BluetoothDevice) -> String,
    onUseMidiSource: (Boolean) -> Unit,
    onScanBleMidi: () -> Unit,
    onConnectBluetoothMidi: (BluetoothDevice) -> Unit,
    onDisconnectMidi: () -> Unit,
    onClearMidiError: () -> Unit,
    onStartPlaying: () -> Unit,
    onStopPlaying: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = piece.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "第 ${currentIndex + 1} / ${piece.notes.size} 个音 · 选输入方式后弹奏或点击琴键",
                style = MaterialTheme.typography.bodyMedium,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            // 输入方式：麦克风 / 蓝牙 MIDI
            Text(
                text = "输入方式",
                style = MaterialTheme.typography.labelMedium,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 10.dp)
            )
            Row(modifier = Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = !useMidiSource,
                    onClick = { onUseMidiSource(false) },
                    label = { Text("麦克风") }
                )
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                FilterChip(
                    selected = useMidiSource,
                    onClick = { if (isMidiSupported) onUseMidiSource(true) else { } },
                    label = { Text("蓝牙 MIDI") },
                    enabled = isMidiSupported
                )
            }
            if (useMidiSource) {
                if (!isMidiSupported) {
                    Text(
                        text = "当前设备不支持 MIDI",
                        style = MaterialTheme.typography.bodySmall,
                        color = PianoTheme.colors.error,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    if (!isBluetoothEnabled) {
                        Text(
                            text = "蓝牙已关闭",
                            style = MaterialTheme.typography.bodySmall,
                            color = PianoTheme.colors.error,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    if (midiConnected) {
                        Text(
                            text = "已连接，直接弹奏",
                            style = MaterialTheme.typography.bodySmall,
                            color = PianoTheme.colors.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Button(onClick = onDisconnectMidi, modifier = Modifier.padding(top = 6.dp)) {
                            Text("断开 MIDI")
                        }
                    } else {
                        Button(
                            onClick = onScanBleMidi,
                            modifier = Modifier.padding(top = 6.dp),
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
                            TextButton(onClick = onClearMidiError) { Text("清除") }
                        }
                        if (scannedBleMidiDevices.isEmpty() && !isScanningBle) {
                            Text(
                                text = if (isBluetoothEnabled)
                                    "请打开电钢的蓝牙后点击「扫描蓝牙 MIDI 设备」，再在列表中点击设备连接"
                                else
                                    "请先打开手机蓝牙后再点击「扫描蓝牙 MIDI 设备」",
                                style = MaterialTheme.typography.bodySmall,
                                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        } else if (scannedBleMidiDevices.isNotEmpty()) {
                            Text(
                                text = "蓝牙 MIDI 设备（点击连接）",
                                style = MaterialTheme.typography.labelSmall,
                                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            scannedBleMidiDevices.forEach { device ->
                                TextButton(onClick = { onConnectBluetoothMidi(device) }) {
                                    Text(getBluetoothDeviceDisplayName(device))
                                }
                            }
                        }
                    }
                }
            } else {
                val next = piece.notes.getOrNull(currentIndex)
                if (next != null) {
                    Text(
                        text = "当前应为：${next.displayName()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = PianoTheme.colors.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                when (val pitch = currentPitch) {
                    is PitchResult.Pitch -> {
                        Text(
                            text = "当前弹奏：${pitch.note.displayName()}",
                            style = MaterialTheme.typography.titleSmall,
                            color = PianoTheme.colors.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    is PitchResult.Listening -> {
                        Text(
                            text = "正在听…",
                            style = MaterialTheme.typography.bodySmall,
                            color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    null -> { }
                }
                if (permissionDenied) {
                    Text(
                        text = "需要麦克风权限才能识别琴声",
                        style = MaterialTheme.typography.bodySmall,
                        color = PianoTheme.colors.error,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = if (isRecording) onStopPlaying else onStartPlaying
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(if (isRecording) "停止弹奏" else "开始弹奏")
                }
            }
            if (useMidiSource && midiConnected) {
                val next = piece.notes.getOrNull(currentIndex)
                if (next != null) {
                    Text(
                        text = "当前应为：${next.displayName()}",
                        style = MaterialTheme.typography.titleSmall,
                        color = PianoTheme.colors.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                when (val pitch = currentPitch) {
                    is PitchResult.Pitch -> {
                        Text(
                            text = "当前弹奏：${pitch.note.displayName()}",
                            style = MaterialTheme.typography.titleSmall,
                            color = PianoTheme.colors.primary,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    is PitchResult.Listening -> {
                        Text(
                            text = "等待 MIDI 输入…",
                            style = MaterialTheme.typography.bodySmall,
                            color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    null -> { }
                }
            }
        }
    }
}

@Composable
private fun finishedCard(
    piece: PracticePiece,
    records: List<CorrectionRecord>,
    onRestart: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "练习完成",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            val total = piece.notes.size
            val wrongCount = records.filter { !it.isCorrect }.map { it.index }.toSet().size
            val accuracy = if (total > 0) ((total - wrongCount) * 100 / total) else 100
            Text(
                text = "正确率：（$total - $wrongCount 错）/ $total = $accuracy%",
                style = MaterialTheme.typography.bodyLarge,
                color = PianoTheme.colors.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
            val errors = records.filter { !it.isCorrect }
            if (errors.isNotEmpty()) {
                Text(
                    text = "错音记录",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                errors.forEach { r: CorrectionRecord ->
                    Text(
                        text = "第 ${r.index + 1} 个音：${r.errorMessage()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRestart) {
                Text("重新弹奏")
            }
        }
    }
}
