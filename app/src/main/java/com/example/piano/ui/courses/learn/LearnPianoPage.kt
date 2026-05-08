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
import androidx.compose.runtime.LaunchedEffect
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
    onPlayVideo: (Int, String) -> Unit,
    onOpenCourseDetail: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCategories()
    }

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
                Text(
                    text = "学钢琴",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = PianoTheme.colors.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "系统化学习，循序渐进",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                state.categories.forEachIndexed { index, category ->
                    val (cardColor, accentColor, contentColor) = CARD_COLORS[index % CARD_COLORS.size]
                    CourseCard(
                        title = category.title,
                        bullets = category.bullets,
                        statusText = category.statusText,
                        inProgress = category.inProgress,
                        isLocked = category.isLocked,
                        cardColor = cardColor,
                        accentColor = accentColor,
                        contentColor = contentColor,
                        categoryId = if (category.hasSubCourses) category.categoryId else null,
                        onOpenDetail = onOpenCourseDetail,
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
    isLocked: Boolean = false,
    cardColor: Color,
    accentColor: Color,
    contentColor: Color,
    categoryId: Int? = null,
    onOpenDetail: (Int) -> Unit = {},
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
                    if (isLocked) {
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
                    } else if (inProgress) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall,
                            color = contentColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    if (!isLocked && categoryId != null) {
                        onOpenDetail(categoryId)
                    }
                },
                enabled = !isLocked,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor.copy(alpha = 0.25f),
                    contentColor = contentColor,
                    disabledContainerColor = contentColor.copy(alpha = 0.12f),
                    disabledContentColor = contentColor.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLocked) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLocked) "已锁定" else "开始学习")
            }
        }
    }
}
