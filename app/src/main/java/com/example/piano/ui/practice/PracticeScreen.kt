package com.example.piano.ui.practice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.piano.core.audio.PitchResult
import com.example.piano.navigation.NavigationActions
import com.example.piano.ui.theme.PianoTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.core.content.ContextCompat

@Composable
fun PracticePage(navController: NavHostController) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0.45f) }
    val navActions = remember(navController) { NavigationActions(navController) }
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

    fun onStartPitchCaptureClick() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startPitchCapture()
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "AI 实时陪练",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "实时分析你的演奏，提供即时反馈",
            style = MaterialTheme.typography.bodyMedium,
            color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 跟弹纠错入口
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = PianoTheme.colors.secondaryContainer.copy(alpha = 0.5f)
            ),
            onClick = { navActions.navigateToPracticeFollowAlong() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = PianoTheme.colors.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "智能纠错 · 跟弹练习",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "按顺序点击琴键，实时纠错并查看正确率",
                        style = MaterialTheme.typography.bodySmall,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = PianoTheme.colors.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // 实时音高 · 音频采集 + 显示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                            text = "采集麦克风声音并显示检测到的音高（钢琴、人声等）",
                            style = MaterialTheme.typography.bodySmall,
                            color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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
        }

        // Current Piece Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = PianoTheme.colors.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "月光奏鸣曲 - 第一乐章",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "路德维希·凡·贝多芬",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "练习进度",
                            style = MaterialTheme.typography.bodySmall,
                            color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isPlaying) "暂停" else "开始练习")
                    }
                    OutlinedButton(
                        onClick = { progress = 0.45f }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // AI Feedback Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI 实时反馈",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                FeedbackItem(
                    icon = Icons.Default.Videocam,
                    title = "手势识别",
                    description = "正在分析手指位置",
                    status = "活跃",
                    statusColor = PianoTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeedbackItem(
                    icon = Icons.Default.Mic,
                    title = "音频分析",
                    description = "检测音准和节奏",
                    status = "活跃",
                    statusColor = PianoTheme.colors.secondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeedbackItem(
                    icon = Icons.Default.VolumeUp,
                    title = "节奏跟踪",
                    description = "监测演奏速度",
                    status = "待机",
                    statusColor = PianoTheme.colors.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        // Performance Metrics
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                modifier = Modifier.weight(1f),
                value = "92%",
                label = "准确率",
                color = PianoTheme.colors.primary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                value = "88%",
                label = "节奏",
                color = PianoTheme.colors.secondary
            )
            MetricCard(
                modifier = Modifier.weight(1f),
                value = "85%",
                label = "音准",
                color = PianoTheme.colors.onSurface
            )
        }

        // AI Suggestions
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "AI 建议",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                SuggestionItem(
                    emoji = "✓",
                    title = "手指位置准确",
                    description = "你的手指位置非常标准，继续保持！",
                    backgroundColor = PianoTheme.colors.primary.copy(alpha = 0.1f),
                    titleColor = PianoTheme.colors.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionItem(
                    emoji = "⚠",
                    title = "注意节奏",
                    description = "第12-16小节的节奏稍快，建议放慢速度练习",
                    backgroundColor = PianoTheme.colors.secondary.copy(alpha = 0.1f),
                    titleColor = PianoTheme.colors.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionItem(
                    emoji = "💡",
                    title = "练习建议",
                    description = "建议重点练习左手和弦部分，可以提高整体流畅度",
                    backgroundColor = PianoTheme.colors.surfaceVariant,
                    titleColor = PianoTheme.colors.onSurface
                )
            }
        }
    }
}

@Composable
fun FeedbackItem(
    icon: ImageVector,
    title: String,
    description: String,
    status: String,
    statusColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = PianoTheme.colors.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            AssistChip(
                onClick = { },
                label = { Text(status, style = MaterialTheme.typography.bodySmall) },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = statusColor.copy(alpha = 0.2f),
                    labelColor = statusColor
                )
            )
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SuggestionItem(
    emoji: String,
    title: String,
    description: String,
    backgroundColor: Color,
    titleColor: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "$emoji $title",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
