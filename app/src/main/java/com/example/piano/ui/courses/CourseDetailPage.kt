package com.example.piano.ui.courses

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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piano.ui.theme.PianoTheme

/** 详情页内单个课时：标题 + 视频地址 */
private data class LessonItem(val title: String, val videoUrl: String)

/** 按 courseId 返回 (页面标题, 课时列表)，无则 null */
private fun getCourseDetailContent(courseId: String): Pair<String, List<LessonItem>>? {
    val videoUrl = "https://piano-course.oss-cn-beijing.aliyuncs.com/course/54eadcdcf136dd33b0fdbf0afbd24061.mp4"
    return when (courseId) {
        "intro" -> "认识钢琴和乐谱" to listOf(
            LessonItem("认识钢琴", videoUrl),
            LessonItem("感受do、re、mi", videoUrl),
            LessonItem("认识乐谱", videoUrl)
        )
        else -> null
    }
}

@Composable
private fun LessonCard(
    index: Int,
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = PianoTheme.colors.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    .background(PianoTheme.colors.primary.copy(alpha = 0.8f))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PianoTheme.colors.onSurface.copy(alpha = 0.08f)),
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
                color = PianoTheme.colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PianoTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
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

/**
 * 课程详情页：展示该课程下的子课时卡片，点击卡片播放视频
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailPage(
    courseId: String,
    onBack: () -> Unit,
    onPlayVideo: (String) -> Unit
) {
    val content = getCourseDetailContent(courseId)
    if (content == null) {
        onBack()
        return
    }
    val (title, lessons) = content

    Scaffold(
        topBar = {
            Surface(color = PianoTheme.colors.surface) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "返回",
                                tint = PianoTheme.colors.onSurface
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PianoTheme.colors.onSurface
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "选择课时",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            lessons.forEachIndexed { index, lesson ->
                LessonCard(
                    index = index + 1,
                    title = lesson.title,
                    onClick = { onPlayVideo(lesson.videoUrl) }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
