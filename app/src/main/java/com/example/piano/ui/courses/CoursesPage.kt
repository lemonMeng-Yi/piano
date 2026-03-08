package com.example.piano.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piano.ui.theme.PianoTheme

/** 课程 Tab：学钢琴 / 曲谱 */
private enum class CourseTab { LEARN_PIANO, MUSIC_LIBRARY }

private val TAB_TITLES = listOf("学钢琴", "曲谱库")

/** 单门课程数据（学钢琴用） */
private data class CourseItem(
    val title: String,
    val bullets: List<String>,
    val statusText: String,
    val inProgress: Boolean,
    val cardColor: Color,
    val accentColor: Color,
    val contentColor: Color,
    val videoUrl: String? = null,
    /** 有子课时的课程：点击「开始学习」进入详情页；需配合 courseId 使用 */
    val subItems: List<SubLesson>? = null,
    val courseId: String? = null
)

/** 子课时：标题 + 视频地址 */
private data class SubLesson(val title: String, val videoUrl: String?)

/** 曲库条目 */
private data class SongItem(
    val title: String,
    val artist: String,
    val level: String
)

@Composable
fun CoursesPage(
    onPlayVideo: (String) -> Unit = {},
    onOpenCourseDetail: (String) -> Unit = {}
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
            CourseTab.LEARN_PIANO -> LearnPianoContent(onPlayVideo = onPlayVideo, onOpenCourseDetail = onOpenCourseDetail)
            CourseTab.MUSIC_LIBRARY -> MusicLibraryContent()
        }
    }
}

@Composable
private fun LearnPianoContent(
    onPlayVideo: (String) -> Unit,
    onOpenCourseDetail: (String) -> Unit
) {
    val courses = remember {
        listOf(
            CourseItem(
                title = "认识钢琴和乐谱",
                bullets = listOf(
                    "认识钢琴",
                    "感受do、re、mi",
                    "认识乐谱"
                ),
                statusText = "进行中 0/8",
                inProgress = true,
                cardColor = Color(0xFFE91E8C).copy(alpha = 0.85f),
                accentColor = Color(0xFFFFC107),
                contentColor = Color.Black,
                videoUrl = "https://piano-course.oss-cn-beijing.aliyuncs.com/course/54eadcdcf136dd33b0fdbf0afbd24061.mp4",
                subItems = listOf(
                    SubLesson("认识钢琴", "https://piano-course.oss-cn-beijing.aliyuncs.com/course/54eadcdcf136dd33b0fdbf0afbd24061.mp4"),
                    SubLesson("感受do、re、mi", "https://piano-course.oss-cn-beijing.aliyuncs.com/course/54eadcdcf136dd33b0fdbf0afbd24061.mp4"),
                    SubLesson("认识乐谱", "https://piano-course.oss-cn-beijing.aliyuncs.com/course/54eadcdcf136dd33b0fdbf0afbd24061.mp4")
                ),
                courseId = "intro"
            ),
            CourseItem(
                title = "节奏入门",
                bullets = listOf(
                    "认识四分、二分、全音符及附点二分音符",
                    "认识左手低音 Do，并尝试双手合奏"
                ),
                statusText = "未开始",
                inProgress = false,
                cardColor = Color(0xFF7B1FA2),
                accentColor = Color.White,
                contentColor = Color.White
            ),
            CourseItem(
                title = "右手五指训练",
                bullets = listOf(
                    "在 Do 音手位上进行五指训练",
                    "认识同音连音线",
                    "认识四分、二分、全休止符"
                ),
                statusText = "未开始",
                inProgress = false,
                cardColor = Color(0xFF1976D2),
                accentColor = Color.White,
                contentColor = Color.White
            ),
            CourseItem(
                title = "双手演奏",
                bullets = listOf(
                    "学习 Re 音手位",
                    "双手配合基础练习"
                ),
                statusText = "未开始",
                inProgress = false,
                cardColor = Color(0xFF388E3C),
                accentColor = Color.White,
                contentColor = Color.White
            )
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        courses.forEach { course ->
            CourseCard(
                title = course.title,
                bullets = course.bullets,
                statusText = course.statusText,
                inProgress = course.inProgress,
                cardColor = course.cardColor,
                accentColor = course.accentColor,
                contentColor = course.contentColor,
                videoUrl = course.videoUrl,
                courseId = course.courseId,
                subItems = course.subItems,
                onOpenDetail = onOpenCourseDetail,
                onPlayVideo = onPlayVideo,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
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
    subItems: List<SubLesson>? = null,
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
                    if (subItems != null && courseId != null) {
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
    val songs = remember {
        listOf(
            SongItem("欢乐颂", "贝多芬", "入门"),
            SongItem("致爱丽丝", "贝多芬", "初级"),
            SongItem("小星星变奏曲", "莫扎特", "初级"),
            SongItem("月光奏鸣曲", "贝多芬", "中级"),
            SongItem("土耳其进行曲", "莫扎特", "中级")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "流行曲库",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PianoTheme.colors.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        songs.forEach { song ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = song.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = PianoTheme.colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    AssistChip(
                        onClick = { },
                        label = { Text(song.level) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = PianoTheme.colors.primary.copy(alpha = 0.2f),
                            labelColor = PianoTheme.colors.primary
                        )
                    )
                }
            }
        }
    }
}
