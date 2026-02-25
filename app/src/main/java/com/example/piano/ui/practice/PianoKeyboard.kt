package com.example.piano.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piano.domain.practice.Note
import com.example.piano.ui.theme.PianoTheme

/** 两 octave 白键：C4 D4 E4 F4 G4 A4 B4 C5 D5 E5 F5 G5 A5 B5 */
private val WHITE_MIDIS = listOf(60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77, 79, 81, 83)

/** 黑键 MIDI 及其在白键中的位置（第几个白键 + 0.65 的偏移） */
private val BLACK_KEYS = listOf(
    61 to 0.72f, 63 to 1.72f, 66 to 3.72f, 68 to 4.72f, 70 to 5.72f,
    73 to 7.72f, 75 to 8.72f, 78 to 10.72f, 80 to 11.72f, 82 to 12.72f
)

/** 白键音名 C D E F G A B */
private val WHITE_NAMES = arrayOf("C", "D", "E", "F", "G", "A", "B")

/** MIDI 在八度内的半音偏移 0,2,4,5,7,9,11 -> 简谱 1-7 */
private fun midiToScaleDegree(midi: Int): Int {
    val semitone = midi % 12
    return when (semitone) {
        0 -> 1   // C
        2 -> 2   // D
        4 -> 3   // E
        5 -> 4   // F
        7 -> 5   // G
        9 -> 6   // A
        11 -> 7  // B
        else -> 0 // 黑键
    }
}

/** 白键：音名 + 简谱 1-7 */
private fun whiteKeyLabel(midi: Int): Pair<String, String> {
    val degree = midiToScaleDegree(midi)
    val name = if (degree in 1..7) WHITE_NAMES[degree - 1] else "?"
    return name to "$degree"
}

/** 黑键：升号 + 简谱，如 #1 #2 #4 #5 #6 */
private fun blackKeyLabel(midi: Int): String {
    val semitone = midi % 12
    val degree = when (semitone) {
        1 -> 1   // C#
        3 -> 2   // D#
        6 -> 4   // F#
        8 -> 5   // G#
        10 -> 6  // A#
        else -> 0
    }
    return if (degree > 0) "#$degree" else ""
}

@Composable
fun PianoKeyboard(
    modifier: Modifier = Modifier,
    highlightMidi: Int? = null,
    wrongMidi: Int? = null,
    onKeyPress: (Note) -> Unit
) {
    val config = LocalConfiguration.current
    val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val totalWidth = maxWidth
        val maxH = maxHeight
        val keyHeight: Dp = if (isLandscape) {
            (maxH * 0.42f).coerceAtMost(140.dp).coerceAtLeast(72.dp)
        } else {
            150.dp
        }
        val blackHeight = keyHeight * (95f / 150f)
        val blackWidthRatio = 0.62f
        val n = WHITE_MIDIS.size
        val whiteW = totalWidth / n
        val blackW = whiteW * blackWidthRatio

        val keyLabelSp = if (isLandscape) 9.sp else 11.sp
        val degreeSp = if (isLandscape) 8.sp else 10.sp

        Row(Modifier.fillMaxWidth()) {
            WHITE_MIDIS.forEach { midi ->
                val highlight = highlightMidi == midi
                val wrong = wrongMidi == midi
                val (name, degree) = whiteKeyLabel(midi)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(keyHeight)
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(
                            when {
                                wrong -> PianoTheme.colors.error.copy(alpha = 0.5f)
                                highlight -> PianoTheme.colors.primary.copy(alpha = 0.45f)
                                else -> Color.White
                            }
                        )
                        .then(
                            if (highlight || wrong)
                                Modifier.border(2.dp, PianoTheme.colors.primary, RoundedCornerShape(6.dp))
                            else Modifier
                        )
                        .clickable { onKeyPress(Note(midi)) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = name,
                            fontSize = keyLabelSp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (highlight || wrong) PianoTheme.colors.onPrimary
                            else Color(0xFF333333)
                        )
                        Text(
                            text = degree,
                            fontSize = degreeSp,
                            color = if (highlight || wrong) PianoTheme.colors.onPrimary.copy(alpha = 0.9f)
                            else Color(0xFF666666)
                        )
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
            BLACK_KEYS.forEach { (midi, posInWhites) ->
                val highlight = highlightMidi == midi
                val wrong = wrongMidi == midi
                val left = whiteW * (posInWhites - blackWidthRatio / 2f)
                val label = blackKeyLabel(midi)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = left, y = 0.dp)
                        .width(blackW)
                        .height(blackHeight)
                        .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(
                            when {
                                wrong -> PianoTheme.colors.error
                                highlight -> PianoTheme.colors.primary
                                else -> Color(0xFF2d2d2d)
                            }
                        )
                        .clickable { onKeyPress(Note(midi)) },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    if (label.isNotEmpty()) {
                        Text(
                            text = label,
                            fontSize = (keyLabelSp.value - 1).sp,
                            fontWeight = FontWeight.Medium,
                            color = if (highlight || wrong) Color.White else Color(0xFFb0b0b0),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
