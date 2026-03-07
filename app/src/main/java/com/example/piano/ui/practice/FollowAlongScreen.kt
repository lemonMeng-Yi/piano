package com.example.piano.ui.practice

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.piano.core.audio.PitchResult
import com.example.piano.domain.practice.CorrectionRecord
import com.example.piano.domain.practice.Note
import com.example.piano.domain.practice.PracticePiece
import com.example.piano.ui.theme.PianoTheme
import kotlinx.coroutines.delay

/** 默认练习曲：C 大调音阶上下行 */
private val DEFAULT_PIECE = PracticePiece(
    id = "scale_c",
    title = "C 大调音阶",
    notes = listOf(60, 62, 64, 65, 67, 69, 71, 72, 71, 69, 67, 65, 64, 62, 60).map { Note(it) }
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowAlongScreen(
    onBack: () -> Unit
) {
    val piece = DEFAULT_PIECE
    var currentIndex by remember { mutableStateOf(0) }
    val records = remember { mutableStateListOf<CorrectionRecord>() }
    /** 仅在此处设为非 null：点击错键 或 麦克风判定为错音。弹对或 400ms 后清除。红键只跟这个走。 */
    var wrongMidi by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }
    var hasAdvancedForCurrentIndex by remember { mutableStateOf(false) }
    /** 本索引下已记录过的错音 MIDI（麦克风），避免同一错键重复记多条 */
    var lastRecordedWrongMidi by remember { mutableStateOf<Int?>(null) }

    val viewModel: PracticeViewModel = viewModel()
    val currentPitch by viewModel.currentPitch.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val permissionDenied by viewModel.permissionDenied.collectAsState()
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startPitchCapture()
        else { viewModel.onPermissionDenied(); viewModel.stopPitchCapture() }
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
            TopAppBar(
                title = { Text("跟弹纠错") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PianoTheme.colors.surface,
                    titleContentColor = PianoTheme.colors.onSurface
                )
            )
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
                    instructionCard(
                        piece = piece,
                        currentIndex = currentIndex,
                        records = records,
                        wrongMidi = wrongMidi,
                        isRecording = isRecording,
                        currentPitch = currentPitch,
                        permissionDenied = permissionDenied,
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
                text = "第 ${currentIndex + 1} / ${piece.notes.size} 个音 · 点击「开始弹奏」用钢琴弹，或直接点击琴键",
                style = MaterialTheme.typography.bodyMedium,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
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
