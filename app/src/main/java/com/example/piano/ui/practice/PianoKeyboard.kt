package com.example.piano.ui.practice

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piano.domain.practice.Note
import com.example.piano.ui.theme.PianoTheme

// ========== 88 键钢琴键盘（A0–C8） ==========

/** 88 键白键 MIDI：A0(21), B0(23), C1..B7, C8(108) 共 52 键 */
private fun full88WhiteMidis(): List<Int> {
    val list = mutableListOf(21, 23)
    for (oct in 1..7) {
        for (s in listOf(0, 2, 4, 5, 7, 9, 11)) {
            list.add((oct + 1) * 12 + s)
        }
    }
    list.add(108)
    return list
}

/**
 * 88 键黑键：(midi, 中心位置)。
 * 中心位置 = 竖线索引（竖线在相邻白键之间：第 1 条竖线=1.0，第 2 条=2.0…），黑键严格以该竖线左右对称。
 */
private fun full88BlackKeys(): List<Pair<Int, Float>> {
    val out = mutableListOf<Pair<Int, Float>>()
    out.add(22 to 1f)
    for (oct in 1..7) {
        val lineStart = 2f + (oct - 1) * 7
        val base = (oct + 1) * 12
        out.add(base + 1 to (lineStart + 1f))
        out.add(base + 3 to (lineStart + 2f))
        out.add(base + 6 to (lineStart + 4f))
        out.add(base + 8 to (lineStart + 5f))
        out.add(base + 10 to (lineStart + 6f))
    }
    return out
}

private fun full88OctaveLabel(midi: Int): String? =
    if (midi == 60) "中央C" else if (midi % 12 == 0) "C${(midi / 12) - 1}" else null

private val WhiteKeyBorderColor = Color.Black
private val BlackKeyFill = Color(0xFF1A1A1A)
private val BlackKeyTopHighlight = Color(0xFF404040)
private val CurrentKeyYellow = Color(0xFFFFC107)
private val CurrentKeyYellowDark = Color(0xFFE0A800)
private val CorrectKeyGreen = Color(0xFF4CAF50)
private val CorrectKeyGreenDark = Color(0xFF388E3C)
private val OctaveLabelGray = Color(0xFF757575)

private val borderWidth = 0.5.dp
private const val BLACK_HEIGHT_RATIO = 0.62f
private const val BLACK_WIDTH_RATIO = 0.65f

/** 底部琴键条推荐高度（与跟弹页一致）：屏高 22%，限制在 100.dp～150.dp，用于随播/跟弹等底部固定键盘。 */
@Composable
fun rememberPianoKeyboardBottomHeight(): androidx.compose.ui.unit.Dp {
    val config = LocalConfiguration.current
    return (config.screenHeightDp.dp * 0.22f).coerceIn(100.dp, 150.dp)
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun Full88PianoKeyboard(
    modifier: Modifier = Modifier,
    highlightMidi: Int? = null,
    /** 当前应高亮的多个 MIDI 键（如随 MIDI 播放），优先于 highlightMidi */
    activeMidiSet: Set<Int> = emptySet(),
    wrongMidi: Int? = null,
    /** 刚弹对的键，显示绿色，用于虚拟键盘练琴等反馈 */
    correctMidi: Int? = null,
    showOctaveLabels: Boolean = true,
    onKeyPress: (Note) -> Unit
) {
    val whiteMidis = remember { full88WhiteMidis() }
    val blackKeys = remember { full88BlackKeys() }

    BoxWithConstraints(modifier = modifier.fillMaxWidth().fillMaxSize()) {
        val totalWidth = maxWidth
        val blackHeight = maxHeight * BLACK_HEIGHT_RATIO
        val n = whiteMidis.size
        val whiteW = totalWidth / n
        val blackW = whiteW * BLACK_WIDTH_RATIO
        val keyShape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp)
        val blackShape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp)

        Row(Modifier.fillMaxWidth().width(maxWidth)) {
            whiteMidis.forEach { midi ->
                val highlight = midi in activeMidiSet || highlightMidi == midi
                val wrong = wrongMidi == midi
                val correct = correctMidi == midi
                val fillColor = when {
                    correct -> CorrectKeyGreen.copy(alpha = 0.92f)
                    wrong -> PianoTheme.colors.error.copy(alpha = 0.5f)
                    highlight -> CurrentKeyYellow.copy(alpha = 0.92f)
                    else -> Color.White
                }
                val label = if (showOctaveLabels) full88OctaveLabel(midi) else null
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(this@BoxWithConstraints.maxHeight)
                        .clip(keyShape)
                        .background(fillColor)
                        .then(
                            if (highlight || wrong || correct)
                                Modifier.border(2.dp, PianoTheme.colors.primary, keyShape)
                            else
                                Modifier.border(borderWidth, WhiteKeyBorderColor, keyShape)
                        )
                        .clickable { onKeyPress(Note(midi)) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (label != null) {
                        val labelColor = when {
                            wrong -> PianoTheme.colors.onError
                            correct -> Color.White
                            highlight -> Color(0xFF1A1A1A)
                            else -> OctaveLabelGray
                        }
                        val textStyle = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium
                        )
                        if (label == "中央C") {
                            Text(
                                text = "中\n央\nC",
                                style = textStyle.copy(lineHeight = 10.sp),
                                color = labelColor,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        } else {
                            Text(
                                text = label,
                                style = textStyle,
                                color = labelColor,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(blackHeight)
                .align(Alignment.TopStart)
        ) {
            blackKeys.forEach { (midi, centerLineIndex) ->
                val highlight = midi in activeMidiSet || highlightMidi == midi
                val wrong = wrongMidi == midi
                val correct = correctMidi == midi
                val centerX = whiteW * centerLineIndex
                val left = centerX - blackW / 2
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = left, y = 0.dp)
                        .width(blackW)
                        .height(blackHeight)
                        .clip(blackShape)
                        .clickable { onKeyPress(Note(midi)) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                when {
                                    correct -> CorrectKeyGreenDark
                                    wrong -> PianoTheme.colors.error
                                    highlight -> CurrentKeyYellowDark
                                    else -> BlackKeyFill
                                }
                            )
                            .then(
                                if (!highlight && !wrong && !correct)
                                    Modifier.border(1.dp, Color(0xFF0D0D0D), blackShape)
                                else Modifier
                            )
                    )
                    if (!highlight && !wrong && !correct) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .align(Alignment.TopCenter)
                                .background(BlackKeyTopHighlight)
                        )
                    }
                }
            }
        }
    }
}
