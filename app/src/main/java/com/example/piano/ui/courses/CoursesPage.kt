package com.example.piano.ui.courses

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.ui.courses.learn.CoursesViewModel
import com.example.piano.ui.courses.learn.LearnPianoContent

/**
 * 课程模块页面：仅展示学钢琴内容
 */
@Composable
fun CoursesPage(
    onPlayVideo: (String) -> Unit = {},
    onOpenCourseDetail: (String) -> Unit = {},
    viewModel: CoursesViewModel = hiltViewModel()
) {
    LearnPianoContent(
        viewModel = viewModel,
        onPlayVideo = onPlayVideo,
        onOpenCourseDetail = onOpenCourseDetail
    )
}
