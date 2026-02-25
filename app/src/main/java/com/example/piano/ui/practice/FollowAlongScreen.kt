package com.example.piano.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
    var lastWrongMidi by remember { mutableStateOf<Int?>(null) }
    var finished by remember { mutableStateOf(false) }

    LaunchedEffect(lastWrongMidi) {
        if (lastWrongMidi != null) {
            delay(400)
            lastWrongMidi = null
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
            currentIndex++
            if (currentIndex >= piece.notes.size) finished = true
        } else {
            lastWrongMidi = note.midi
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (!finished) {
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
                            text = "第 ${currentIndex + 1} / ${piece.notes.size} 个音 · 按顺序点击下方琴键",
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
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                PianoKeyboard(
                    modifier = Modifier.fillMaxWidth(),
                    highlightMidi = piece.notes.getOrNull(currentIndex)?.midi,
                    wrongMidi = lastWrongMidi,
                    onKeyPress = ::onKeyPress
                )
                if (records.isNotEmpty() && !records.last().isCorrect) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = records.last().errorMessage(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = PianoTheme.colors.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            } else {
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
                        val correctCount = records.count { it.isCorrect }
                        val total = records.size
                        val accuracy = if (total > 0) (correctCount * 100 / total) else 0
                        Text(
                            text = "正确率：$correctCount / $total（$accuracy%）",
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
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                PianoKeyboard(
                    modifier = Modifier.fillMaxWidth(),
                    highlightMidi = null,
                    wrongMidi = null,
                    onKeyPress = {}
                )
            }
        }
    }
}
