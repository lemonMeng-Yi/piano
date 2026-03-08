package com.example.piano.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.ui.theme.PianoTheme

/** 课程 Tab：学钢琴 / 曲谱 */
private enum class CourseTab { LEARN_PIANO, MUSIC_LIBRARY }

private val TAB_TITLES = listOf("学钢琴", "曲谱库")

/** 卡片配色（按大模块顺序循环使用）；contentColor 统一为白色，保证字体一致 */
private val CARD_COLORS = listOf(
    Triple(Color(0xFFFF9800), Color.White, Color.White),
    Triple(Color(0xFF7B1FA2), Color.White, Color.White),
    Triple(Color(0xFF1976D2), Color.White, Color.White),
    Triple(Color(0xFF388E3C), Color.White, Color.White)
)

/** 曲谱库条目（参考钢琴谱库列表样式） */
private data class SheetMusicItem(
    val title: String,
    val tags: List<String>,
    val authorName: String,
    val likeCount: String
)

@Composable
fun CoursesPage(
    onPlayVideo: (String) -> Unit = {},
    onOpenCourseDetail: (String) -> Unit = {},
    viewModel: CoursesViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(CourseTab.LEARN_PIANO) }

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部：两个 Tab 居中排列，指示条宽度 50.dp
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                TAB_TITLES.forEachIndexed { index, title ->
                    val tab = CourseTab.entries[index]
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { selectedTab = tab }
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp,
                            fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == tab) PianoTheme.colors.primary else PianoTheme.colors.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val indicatorColor: Color = if (selectedTab == tab) PianoTheme.colors.primary else Color.Transparent
                        Box(
                            modifier = Modifier
                                .width(30.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(indicatorColor)
                        )
                    }
                }
            }
        }

        // 内容区：点击 Tab 切换
        when (selectedTab) {
            CourseTab.LEARN_PIANO -> LearnPianoContent(
                viewModel = viewModel,
                onPlayVideo = onPlayVideo,
                onOpenCourseDetail = onOpenCourseDetail
            )
            CourseTab.MUSIC_LIBRARY -> MusicLibraryContent()
        }
    }
}

@Composable
private fun LearnPianoContent(
    viewModel: CoursesViewModel,
    onPlayVideo: (String) -> Unit,
    onOpenCourseDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        when (val state = uiState) {
            is CoursesUiState.Loading -> {
                Spacer(modifier = Modifier.height(20.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PianoTheme.colors.primary)
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            is CoursesUiState.Error -> {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = state.message,
                    color = PianoTheme.colors.error,
                    modifier = Modifier.padding(16.dp)
                )
                TextButton(onClick = { viewModel.loadCategories() }) {
                    Text("重试", color = PianoTheme.colors.primary)
                }
            }
            is CoursesUiState.Success -> {
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

@Composable
private fun MusicLibraryContent() {
    val items = remember {
        listOf(
            SheetMusicItem(
                title = "蒲公英的约定-副歌",
                tags = listOf("副歌", "C大调", "可转简谱"),
                authorName = "香味少女",
                likeCount = "1.3w"
            ),
            SheetMusicItem(
                title = "明明就 (C调)-周杰伦",
                tags = listOf("C大调", "指法"),
                authorName = "LazyMmm",
                likeCount = "1.0w"
            ),
            SheetMusicItem(
                title = "天空之城 (C调)",
                tags = listOf("可转简谱", "指法"),
                authorName = "Hinngula",
                likeCount = "2149"
            ),
            SheetMusicItem(
                title = "听妈妈的话 (C调简单版)-周杰伦",
                tags = listOf("简单版", "C大调"),
                authorName = "不知道叫什么",
                likeCount = "4047"
            ),
            SheetMusicItem(
                title = "婚礼进行曲",
                tags = listOf("经典"),
                authorName = "钢琴谱库",
                likeCount = "454"
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        items.forEach { item ->
            SheetMusicListItem(
                item = item,
                onClick = { },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun SheetMusicListItem(
    item: SheetMusicItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧：曲谱缩略图占位 + 播放按钮
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PianoTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PianoTheme.colors.primary.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "播放",
                        tint = PianoTheme.colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            // 右侧：标题、标签、作者、点赞数
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PianoTheme.colors.onSurface,
                    maxLines = 2
                )
                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.tags.take(4).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PianoTheme.colors.onSurface.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PianoTheme.colors.onSurface.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(PianoTheme.colors.onSurface.copy(alpha = 0.15f))
                    )
                    Text(
                        text = item.authorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = item.likeCount,
                        style = MaterialTheme.typography.labelSmall,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
