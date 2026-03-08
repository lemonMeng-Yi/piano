package com.example.piano.ui.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.piano.ui.theme.PianoTheme

/**
 * 跟弹页加载中：仅显示加载图标，无文案。
 * 用于进入跟弹前的横屏稳定等待。
 */
@Composable
fun FollowAlongLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PianoTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = PianoTheme.colors.primary
        )
    }
}
