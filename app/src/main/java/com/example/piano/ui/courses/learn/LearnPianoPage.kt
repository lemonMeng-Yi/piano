package com.example.piano.ui.courses.learn

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.piano.ui.components.NetworkErrorView
import com.example.piano.ui.theme.PianoTheme

/** 卡片配色（按大模块顺序循环使用）；contentColor 统一为白色 */
private val CARD_COLORS = listOf(
    Triple(Color(0xFFFF9800), Color.White, Color.White),
    Triple(Color(0xFF7B1FA2), Color.White, Color.White),
    Triple(Color(0xFF1976D2), Color.White, Color.White),
    Triple(Color(0xFF388E3C), Color.White, Color.White)
)

@Composable
fun LearnPianoContent(
    viewModel: CoursesViewModel,
    onPlayVideo: (String) -> Unit,
    onOpenCourseDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    when (val state = uiState) {
        is CoursesUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PianoTheme.colors.primary)
            }
        }
        is CoursesUiState.Error -> {
            NetworkErrorView(
                onClick = { viewModel.loadCategories() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                hintText = state.message
            )
        }
        is CoursesUiState.Success -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                state.categories.forEachIndexed { index, category ->
                    val (cardColor, accentColor, contentColor) = CARD_COLORS[index % CARD_COLORS.size]
                    CourseCard(
                        title = category.title,
                        bullets = category.bullets,
                        statusText = category.statusText,
                        inProgress = category.inProgress,
                        cardColor = cardColor,
                        accentColor = accentColor,
                        contentColor = contentColor,
                        videoUrl = null,
                        courseId = if (category.hasSubCourses) category.categoryId.toString() else null,
                        onOpenDetail = onOpenCourseDetail,
                        onPlayVideo = onPlayVideo,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun CourseCard(
    title: String,
    bullets: List<String>,
    statusText: String,
    inProgress: Boolean,
    cardColor: Color,
    accentColor: Color,
    contentColor: Color,
    videoUrl: String? = null,
    courseId: String? = null,
    onOpenDetail: (String) -> Unit = {},
    onPlayVideo: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    bullets.forEach { bullet ->
                        Text(
                            text = "• $bullet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.9f),
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (inProgress) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = accentColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.bodySmall,
                                color = accentColor
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (courseId != null) {
                        onOpenDetail(courseId)
                    } else {
                        videoUrl?.let { url -> onPlayVideo(url) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor.copy(alpha = 0.25f),
                    contentColor = contentColor
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("开始学习")
            }
        }
    }
}
