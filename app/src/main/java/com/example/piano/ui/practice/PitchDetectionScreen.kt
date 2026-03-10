package com.example.piano.ui.practice

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.core.audio.PitchResult
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.theme.PianoTheme

/**
 * 琴音检测页：麦克风/蓝牙 MIDI 采集并实时显示音高，逻辑与练习界面的实时音高一致。
 */
@Composable
fun PitchDetectionScreen(
    onBack: () -> Unit,
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentPitch by viewModel.currentPitch.collectAsState()
    val currentMidiNotes by viewModel.currentMidiNotes.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()
    val permissionDenied by viewModel.permissionDenied.collectAsState()
    val useMidiSource by viewModel.useMidiSource.collectAsState()
    val midiConnected by viewModel.midiConnected.collectAsState()
    val scannedBleMidiDevices by viewModel.scannedBleMidiDevices.collectAsState()
    val isScanningBle by viewModel.isScanningBle.collectAsState()
    val midiConnectionError by viewModel.midiConnectionError.collectAsState()
    val isBluetoothEnabled by viewModel.bluetoothEnabled.collectAsState()
    val isMidiSupported = viewModel.isMidiSupported

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startPitchCapture()
        else {
            viewModel.onPermissionDenied()
            viewModel.stopPitchCapture()
        }
    }

    val scanPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) viewModel.startBleMidiScan()
    }

    val bluetoothEnableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
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
    }

    fun ensureScanPermissionAndStartScan() {
        if (!viewModel.isBluetoothEnabled()) {
            bluetoothEnableLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
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
    }

    fun onStartPitchCaptureClick() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startPitchCapture()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopPitchCapture()
        }
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(title = "琴音检测", onBack = onBack)
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
            Text(
                text = "实时采集并显示音高",
                style = MaterialTheme.typography.bodyMedium,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PianoTheme.colors.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = null,
                            tint = PianoTheme.colors.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "实时音高",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "麦克风或蓝牙 MIDI，支持多键同时弹奏解析",
                                style = MaterialTheme.typography.bodySmall,
                                color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "输入方式",
                        style = MaterialTheme.typography.labelMedium,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = !useMidiSource,
                            onClick = { viewModel.setUseMidiSource(false) },
                            label = { Text("麦克风") }
                        )
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        FilterChip(
                            selected = useMidiSource,
                            onClick = { if (isMidiSupported) viewModel.setUseMidiSource(true) else { } },
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
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            if (!isBluetoothEnabled) {
                                Text(
                                    text = "请先打开手机蓝牙",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PianoTheme.colors.error,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            if (midiConnected) {
                                Text(
                                    text = "已连接，弹奏将显示多音解析结果",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PianoTheme.colors.primary,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                Button(
                                    onClick = viewModel::disconnectMidi,
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    Text("断开 MIDI")
                                }
                            } else {
                                Button(
                                    onClick = ::ensureScanPermissionAndStartScan,
                                    modifier = Modifier.padding(top = 8.dp),
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
                                    TextButton(onClick = viewModel::clearMidiError) { Text("清除") }
                                }
                                if (scannedBleMidiDevices.isEmpty() && !isScanningBle) {
                                    Text(
                                        text = if (isBluetoothEnabled)
                                            "请打开电钢蓝牙后点击「扫描蓝牙 MIDI 设备」，再在列表中点击设备连接"
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
                                        TextButton(onClick = { viewModel.connectBluetoothMidi(device) }) {
                                            Text(viewModel.getBluetoothDeviceDisplayName(device))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PianoTheme.colors.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            when (val result = currentPitch) {
                                is PitchResult.Pitch -> {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = result.note.displayName(),
                                            style = MaterialTheme.typography.headlineMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PianoTheme.colors.primary
                                        )
                                        Text(
                                            text = String.format("%.1f Hz", result.frequencyHz),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                                is PitchResult.Listening -> {
                                    Text(
                                        text = "正在监听… 请对着麦克风演奏或发声",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                                null -> {
                                    Text(
                                        text = if (isRecording) "正在启动…" else "点击下方按钮开始采集",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                        if (permissionDenied) {
                            Text(
                                text = "需要麦克风权限才能采集音频",
                                style = MaterialTheme.typography.bodySmall,
                                color = PianoTheme.colors.error,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (isRecording) viewModel.stopPitchCapture()
                                else onStartPitchCaptureClick()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRecording) "停止采集" else "开始采集")
                        }
                    }

                    if (useMidiSource && midiConnected) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PianoTheme.colors.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            val notes = currentMidiNotes.sortedBy { it.midi }
                            if (notes.isEmpty()) {
                                Text(
                                    text = "等待 MIDI 输入… 可多键同时弹奏",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PianoTheme.colors.onSurface.copy(alpha = 0.7f)
                                )
                            } else {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "解析结果（${notes.size} 个音）",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PianoTheme.colors.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = notes.joinToString(" · ") { it.displayName() },
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PianoTheme.colors.primary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
