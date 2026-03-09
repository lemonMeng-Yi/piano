package com.example.piano.ui.courses.sheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.piano.ui.theme.PianoTheme

/** 练琴方式枚举 */
enum class PracticeMethod {
    SOUND_RECOGNITION,  // 声音识别
    BLUETOOTH_MIDI,     // 蓝牙MIDI连接
    VIRTUAL_KEYBOARD    // 虚拟键盘
}

/**
 * 选择练琴方式弹窗：标题 + 关闭按钮 + 三个选项（声音识别、蓝牙MIDI连接、虚拟键盘）。
 * 样式参考设计图：白底圆角卡片，前两项图标为主色，第三项为灰色。
 */
@Composable
fun PracticeMethodDialog(
    onDismiss: () -> Unit,
    onSelectMethod: (PracticeMethod) -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "选择练琴方式",
                    style = MaterialTheme.typography.titleMedium,
                    color = PianoTheme.colors.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    textAlign = TextAlign.Center
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "关闭",
                        tint = PianoTheme.colors.onSurface
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
            ) {
                listOf(
                    Triple(PracticeMethod.SOUND_RECOGNITION, "声音识别", Icons.Outlined.Mic),
                    Triple(PracticeMethod.BLUETOOTH_MIDI, "蓝牙MIDI连接", Icons.Outlined.Bluetooth),
                    Triple(PracticeMethod.VIRTUAL_KEYBOARD, "虚拟键盘", Icons.Outlined.Piano)
                ).forEachIndexed { index, (method, label, icon) ->
                    val iconTint = if (index < 2) PianoTheme.colors.primary else PianoTheme.colors.onSurfaceVariant
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectMethod(method)
                                onDismiss()
                            }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            color = PianoTheme.colors.onSurface
                        )
                    }
                }
            }
        }
    }
}
