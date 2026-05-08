package com.example.piano.ui.courses.learn

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.theme.PianoTheme

@Composable
private fun LessonCard(
    index: Int,
    title: String,
    isCompleted: Boolean = false,
    isLocked: Boolean = false,
    onClick: () -> Unit
) {
    val alpha = if (isLocked) 0.4f else 1f
    Card(
        onClick = if (isLocked) ({}) else onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PianoTheme.colors.surfaceVariant.copy(alpha = alpha)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLocked) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 16.dp, top = 16.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isCompleted) Color(0xFF4CAF50) else PianoTheme.colors.primary.copy(alpha = 0.8f * alpha)
                    )
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) Color(0xFF4CAF50).copy(alpha = 0.15f)
                        else PianoTheme.colors.onSurface.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$index",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.7f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = PianoTheme.colors.onSurface.copy(alpha = alpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) Color(0xFF4CAF50)
                        else if (isLocked) PianoTheme.colors.onSurface.copy(alpha = 0.12f)
                        else PianoTheme.colors.primary
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "已完成",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (isLocked) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "已锁定",
                        tint = PianoTheme.colors.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = PianoTheme.colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * 课程详情页：展示该课程下的子课时卡片，点击卡片播放视频
 */
@Composable
fun CourseDetailPage(
    onBack: () -> Unit,
    onPlayVideo: (courseId: Int, videoUrl: String) -> Unit,
    viewModel: CourseDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDetail()
    }

    LaunchedEffect(uiState) {
        if (uiState is CourseDetailUiState.Error) {
            val msg = (uiState as CourseDetailUiState.Error).message
            if (msg == "未找到该课程") onBack()
        }
    }

    val title = when (val s = uiState) {
        is CourseDetailUiState.Success -> s.categoryName
        else -> ""
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = title.ifEmpty { "课程详情" },
                onBack = onBack
            )
        },
        containerColor = PianoTheme.colors.surface
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            when (val state = uiState) {
                is CourseDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PianoTheme.colors.primary)
                    }
                }
                is CourseDetailUiState.Error -> {
                    Text(
                        text = state.message,
                        color = PianoTheme.colors.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    TextButton(onClick = { viewModel.loadDetail() }) {
                        Text("重试", color = PianoTheme.colors.primary)
                    }
                }
                is CourseDetailUiState.Success -> {
                    Text(
                        text = "选择课时",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    state.lessons.forEachIndexed { index, lesson ->
                        LessonCard(
                            index = index + 1,
                            title = lesson.title,
                            isCompleted = lesson.isCompleted,
                            isLocked = lesson.isLocked,
                            onClick = {
                                onPlayVideo(lesson.courseId, lesson.videoUrl)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
