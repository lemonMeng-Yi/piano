package com.example.piano.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piano.R
import com.example.piano.ui.theme.AppColors

/**
 * 应用统一的 Snackbar 宿主组件
 * 用于显示全局提示消息
 */
@Composable
fun AppSnackBarHost(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        SnackbarHost(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .wrapContentSize()
                .padding(bottom = 45.dp)
                .imePadding(),
            hostState = SnackBarManager.snackBarHostState,
            snackbar = { data ->
                CustomSnackbar(
                    message = data.visuals.message,
                    iconState = SnackBarManager.iconState
                )
            }
        )
    }
}

/**
 * 自定义 Snackbar 样式
 */
@Composable
private fun CustomSnackbar(
    message: String,
    iconState: SnackState
) {
    Card(
        modifier = Modifier
            .wrapContentSize()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .shadow(
                elevation = 14.dp,
                spotColor = colorResource(R.color.color_black_20),
                ambientColor = colorResource(R.color.color_black_20)
            ),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = AppColors.toastBackground()
        )
    ) {
        Row(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧图标
            if (iconState != SnackState.Idle) {
                val icon: ImageVector = when (iconState) {
                    SnackState.Error -> Icons.Default.Error
                    SnackState.Success -> Icons.Default.CheckCircle
                    SnackState.Idle -> Icons.Default.Error // 不会执行到这里，但需要满足 exhaustive
                }
                
                val iconColor = when (iconState) {
                    SnackState.Error -> AppColors.feedbackError()
                    SnackState.Success -> AppColors.feedbackSuccess()
                    SnackState.Idle -> AppColors.iconPrimary() // 不会执行到这里，但需要满足 exhaustive
                }
                
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = iconColor
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            
            // 消息文本
            AppText(
                text = message,
                color = AppColors.textPrimary(),
                fontSize = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
