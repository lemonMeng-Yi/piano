package com.example.piano.ui.courses

import androidx.compose.runtime.Composable
import com.example.piano.ui.courses.sheet.MusicLibraryContent

/**
 * 陪练模块页面：仅展示曲谱库（乐谱 / 收藏两个 Tab）
 */
@Composable
fun AccompanimentPage(
    onOpenSheetDetail: (Long) -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {
    MusicLibraryContent(
        onOpenSheetDetail = onOpenSheetDetail,
        onNavigateToLogin = onNavigateToLogin
    )
}
