package com.example.piano.ui.courses.sheet

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Piano
import androidx.compose.material3.Button
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

/**
 * 蓝牙 MIDI 设备弹窗：标题「蓝牙MIDI设备」+ 关闭 + 扫描按钮 + 设备列表（名称、MAC、未连接/已连接），点击设备连接，连接成功后由调用方关闭弹窗并弹出键盘。
 */
@Composable
fun BluetoothMidiDeviceDialog(
    onDismiss: () -> Unit,
    scannedDevices: List<BluetoothDevice>,
    isScanning: Boolean,
    isBluetoothEnabled: Boolean,
    connectedDevice: BluetoothDevice?,
    getDeviceDisplayName: (BluetoothDevice) -> String,
    onScanClick: () -> Unit,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "蓝牙MIDI设备",
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
                if (!isBluetoothEnabled) {
                    Text(
                        text = "请先打开手机蓝牙",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PianoTheme.colors.error,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                Button(
                    onClick = onScanClick,
                    modifier = Modifier.padding(vertical = 8.dp),
                    enabled = isBluetoothEnabled && !isScanning
                ) {
                    Text(if (isScanning) "扫描中…" else "扫描蓝牙 MIDI 设备")
                }
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    scannedDevices.forEach { device ->
                        val isConnected = device.address == connectedDevice?.address
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isConnected) { onDeviceClick(device) }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = getDeviceDisplayName(device),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PianoTheme.colors.onSurface
                                )
                                Text(
                                    text = device.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PianoTheme.colors.onSurfaceVariant
                                )
                            }
                            Text(
                                text = if (isConnected) "已连接" else "未连接",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isConnected) PianoTheme.colors.primary else PianoTheme.colors.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
