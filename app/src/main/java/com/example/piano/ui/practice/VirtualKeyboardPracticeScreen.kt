package com.example.piano.ui.practice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.domain.practice.CorrectionRecord
import com.example.piano.domain.practice.Note
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.theme.PianoTheme
import kotlinx.coroutines.delay

/**
 * 虚拟键盘练琴页：弹出键盘，用户按 MIDI 解析出的顺序逐键点击，对则绿、错则红，弹对才能进行下一个键。
 */
@Composable
fun VirtualKeyboardPracticeScreen(
    onBack: () -> Unit,
    viewModel: VirtualKeyboardPracticeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            BackTitleTopBar(title = "虚拟键盘练琴", onBack = onBack)
        },
        containerColor = PianoTheme.colors.background
    ) { paddingValues ->
        when (val state = uiState) {
            is VirtualKeyboardPracticeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PianoTheme.colors.primary)
                }
            }
            is VirtualKeyboardPracticeUiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = PianoTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }) {
                        Text("重试")
                    }
                }
            }
            is VirtualKeyboardPracticeUiState.Success -> {
                VirtualKeyboardPracticeContent(
                    title = state.title,
                    notes = state.notes,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun VirtualKeyboardPracticeContent(
    title: String,
    notes: List<Note>,
    modifier: Modifier = Modifier
) {
    var currentIndex by remember { mutableStateOf(0) }
    val records = remember { mutableStateListOf<CorrectionRecord>() }
    var wrongMidi by remember { mutableStateOf<Int?>(null) }
    var correctMidi by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }

    fun onKeyPress(note: Note) {
        if (finished) return
        val expected = notes.getOrNull(currentIndex) ?: return
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
            correctMidi = note.midi
            currentIndex++
            if (currentIndex >= notes.size) finished = true
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

    val config = LocalConfiguration.current
    val screenHeightDp = config.screenHeightDp.dp
    val keyboardHeightDp = (screenHeightDp * 0.22f).coerceIn(100.dp, 150.dp)

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            if (!finished) {
                virtualKeyboardInstructionCard(
                    title = title,
                    currentIndex = currentIndex,
                    totalCount = notes.size,
                    expectedNote = notes.getOrNull(currentIndex)
                )
                Spacer(modifier = Modifier.height(8.dp))
                expectedVsActualListVirtual(notes = notes, records = records)
            } else {
                virtualKeyboardFinishedCard(
                    title = title,
                    notesCount = notes.size,
                    records = records,
                    onRestart = {
                        currentIndex = 0
                        records.clear()
                        wrongMidi = null
                        correctMidi = null
                        finished = false
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                expectedVsActualListVirtual(notes = notes, records = records)
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
            highlightMidi = notes.getOrNull(currentIndex)?.midi,
            wrongMidi = wrongMidi,
            correctMidi = correctMidi,
            showOctaveLabels = true,
            onKeyPress = ::onKeyPress
        )
    }
}

@Composable
private fun virtualKeyboardInstructionCard(
    title: String,
    currentIndex: Int,
    totalCount: Int,
    expectedNote: Note?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "第 ${currentIndex + 1} / $totalCount 个音 · 点击下方琴键按顺序弹奏，弹对后自动下一个",
                style = MaterialTheme.typography.bodyMedium,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
            expectedNote?.let { note ->
                Text(
                    text = "当前应为：${note.displayName()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = PianoTheme.colors.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun virtualKeyboardFinishedCard(
    title: String,
    notesCount: Int,
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
            val wrongCount = records.filter { !it.isCorrect }.map { it.index }.toSet().size
            val accuracy = if (notesCount > 0) ((notesCount - wrongCount) * 100 / notesCount) else 100
            Text(
                text = "正确率：（$notesCount - $wrongCount 错）/ $notesCount = $accuracy%",
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
                errors.forEach { r ->
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

@Composable
private fun expectedVsActualListVirtual(
    notes: List<Note>,
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
            notes.forEachIndexed { index, expected ->
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
                        modifier = Modifier.padding(end = 8.dp),
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
