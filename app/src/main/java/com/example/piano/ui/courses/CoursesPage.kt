package com.example.piano.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.ui.courses.learn.CoursesViewModel
import com.example.piano.ui.courses.learn.LearnPianoContent
import com.example.piano.ui.courses.sheet.MusicLibraryContent
import com.example.piano.ui.theme.PianoTheme

/** 课程顶层 Tab：学钢琴 / 曲谱库 */
private enum class CourseTab { LEARN_PIANO, MUSIC_LIBRARY }

private val TAB_TITLES = listOf("学钢琴", "曲谱库")

@Composable
fun CoursesPage(
    onPlayVideo: (String) -> Unit = {},
    onOpenCourseDetail: (String) -> Unit = {},
    onOpenSheetDetail: (Long) -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: CoursesViewModel = hiltViewModel()
) {
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val selectedTab = CourseTab.entries[selectedTabIndex.coerceIn(0, CourseTab.entries.size - 1)]

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部：学钢琴 | 曲谱库
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
                        ) { viewModel.setSelectedTabIndex(index) }
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

        when (selectedTab) {
            CourseTab.LEARN_PIANO -> LearnPianoContent(
                viewModel = viewModel,
                onPlayVideo = onPlayVideo,
                onOpenCourseDetail = onOpenCourseDetail
            )
            CourseTab.MUSIC_LIBRARY -> MusicLibraryContent(
                onOpenSheetDetail = onOpenSheetDetail,
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}
