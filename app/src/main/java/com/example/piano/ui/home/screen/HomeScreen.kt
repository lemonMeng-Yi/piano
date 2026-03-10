package com.example.piano.ui.home.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.data.sheet.api.dto.SheetPlayRecordDTO
import com.example.piano.data.sheet.api.dto.SheetItemDTO
import com.example.piano.ui.home.HomeViewModel
import com.example.piano.ui.home.HotSheetsUiState
import com.example.piano.ui.home.RecentPlaysUiState
import com.example.piano.ui.theme.PianoTheme
import com.example.piano.ui.components.NetworkErrorView
import com.example.piano.R

@Composable
fun HomePage(
    onOpenSheetDetail: (Long) -> Unit = {},
    onNavigateToCourses: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    refreshTrigger: Int = 0,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val recentPlaysState by viewModel.recentPlaysState.collectAsState()
    val todayGoalMinutes by viewModel.todayPracticeGoalMinutes.collectAsState()
    var showGoalDialog by mutableStateOf(false)

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.loadRecentPlays()
            viewModel.loadHotSheets()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "欢迎回来！",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "继续你的钢琴学习之旅",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
            color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Today's Practice Card（显示最近一次练习曲目 + 今日目标时长可设置）
        val lastPlayedRecord = (recentPlaysState as? RecentPlaysUiState.Success)?.list?.firstOrNull()
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "今日练琴",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                        fontWeight = FontWeight.Bold
                    )
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { showGoalDialog = true }
                    )
                }
                
                Text(
                    text = if (lastPlayedRecord != null) "继续练习《${lastPlayedRecord.sheet.title}》" else "去选一首曲目开始练习",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Button(
                    onClick = {
                        if (lastPlayedRecord != null) onOpenSheetDetail(lastPlayedRecord.sheet.id)
                        else onNavigateToCourses()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (lastPlayedRecord != null) "开始练习" else "去选曲目",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp)
                    )
                }
            }
        }

        if (showGoalDialog) {
            TodayGoalDialog(
                currentMinutes = todayGoalMinutes,
                onDismiss = { showGoalDialog = false },
                onSelect = {
                    viewModel.setTodayPracticeGoal(it)
                    showGoalDialog = false
                }
            )
        }

        // Recent Practice
        Text(
            text = "最近练习",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        when (val state = recentPlaysState) {
            is RecentPlaysUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = PianoTheme.colors.primary
                    )
                }
            }
            is RecentPlaysUiState.NeedLogin -> {
                NetworkErrorView(
                    hintText = "登录后查看最近练习",
                    buttonText = "去登录",
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }
            is RecentPlaysUiState.Error -> {
                NetworkErrorView(
                    hintText = state.message,
                    buttonText = "重试",
                    onClick = { viewModel.loadRecentPlays() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                )
            }
            is RecentPlaysUiState.Success -> {
                if (state.list.isEmpty()) {
                    Text(
                        text = "暂无最近练习记录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    state.list.forEach { record ->
                        RecentPracticeItemCard(
                            record = record,
                            onClick = { onOpenSheetDetail(record.sheet.id) }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }

        // 热门曲谱（在最近练习下方）
        Text(
            text = "热门曲谱",
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 28.dp, bottom = 16.dp)
        )

        val hotSheetsState by viewModel.hotSheetsState.collectAsState()
        when (val hotState = hotSheetsState) {
            is HotSheetsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = PianoTheme.colors.primary
                    )
                }
            }
            is HotSheetsUiState.Error -> {
                Text(
                    text = hotState.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
            is HotSheetsUiState.Success -> {
                if (hotState.list.isEmpty()) {
                    Text(
                        text = "暂无曲谱",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    val hotList = hotState.list.take(6)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            hotList.getOrNull(0)?.let { sheet ->
                                HotSheetCard(
                                    sheet = sheet,
                                    onClick = { onOpenSheetDetail(sheet.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            hotList.getOrNull(1)?.let { sheet ->
                                HotSheetCard(
                                    sheet = sheet,
                                    onClick = { onOpenSheetDetail(sheet.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            hotList.getOrNull(2)?.let { sheet ->
                                HotSheetCard(
                                    sheet = sheet,
                                    onClick = { onOpenSheetDetail(sheet.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            hotList.getOrNull(3)?.let { sheet ->
                                HotSheetCard(
                                    sheet = sheet,
                                    onClick = { onOpenSheetDetail(sheet.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            hotList.getOrNull(4)?.let { sheet ->
                                HotSheetCard(
                                    sheet = sheet,
                                    onClick = { onOpenSheetDetail(sheet.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            hotList.getOrNull(5)?.let { sheet ->
                                HotSheetCard(
                                    sheet = sheet,
                                    onClick = { onOpenSheetDetail(sheet.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onNavigateToCourses,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PianoTheme.colors.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "更多曲谱",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 17.sp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HotSheetCard(
    sheet: SheetItemDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageHeight = 100.dp
    val primary = PianoTheme.colors.primary
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                primary.copy(alpha = 0.18f),
                                primary.copy(alpha = 0.08f)
                            )
                        )
                    )
            ) {
                // 角落装饰小音符（半透明）
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = primary.copy(alpha = 0.2f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(20.dp)
                )
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = primary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(16.dp)
                )
                // 居中主图标
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(40.dp)
                )
            }
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = sheet.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    color = PianoTheme.colors.onSurface
                )
                Text(
                    text = sheet.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                    maxLines = 1,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

private val TODAY_GOAL_OPTIONS = listOf(15, 25, 30, 45, 60)

@Composable
private fun TodayGoalDialog(
    currentMinutes: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("今日练习目标") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "选择今日需要练习的时长（分钟）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.8f)
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TODAY_GOAL_OPTIONS.take(3).forEach { minutes ->
                            FilterChip(
                                selected = currentMinutes == minutes,
                                onClick = { onSelect(minutes) },
                                label = { Text("${minutes}分钟") }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TODAY_GOAL_OPTIONS.drop(3).forEach { minutes ->
                            FilterChip(
                                selected = currentMinutes == minutes,
                                onClick = { onSelect(minutes) },
                                label = { Text("${minutes}分钟") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

/** 将 playedAt 转为具体相对时间：5秒前、3分钟前、2小时前、1天前、一周前（≥7天） */
private fun formatPlayedAt(playedAt: String): String {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = formatter.parse(playedAt) ?: return playedAt
        val nowMs = System.currentTimeMillis()
        val pastMs = date.time
        val diffMs = nowMs - pastMs
        if (diffMs < 0) return playedAt
        val diffSeconds = diffMs / 1000
        val diffMinutes = diffMs / (60 * 1000)
        val diffHours = diffMs / (60 * 60 * 1000)
        val diffDays = diffMs / (24 * 60 * 60 * 1000)
        when {
            diffSeconds < 60 -> "${diffSeconds}秒前"
            diffMinutes < 60 -> "${diffMinutes}分钟前"
            diffHours < 24 -> "${diffHours}小时前"
            diffDays < 7 -> "${diffDays}天前"
            else -> "一周前"
        }
    } catch (_: Exception) {
        playedAt
    }
}

@Composable
fun RecentPracticeItemCard(
    record: SheetPlayRecordDTO,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PianoTheme.colors.onSurface.copy(alpha = 0.08f))
            ) {
                val previewUrl = record.sheet.previewImageUrl
                if (!previewUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = previewUrl,
                        contentDescription = record.sheet.title,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = PianoTheme.colors.primary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.sheet.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = record.sheet.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Text(
                text = formatPlayedAt(record.playedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun PracticeItemCard(
    title: String,
    subtitle: String,
    accuracy: Int,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PianoTheme.colors.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = PianoTheme.colors.primary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "$accuracy%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PianoTheme.colors.primary
                )
                Text(
                    text = time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
