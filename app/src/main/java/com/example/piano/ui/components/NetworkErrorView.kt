package com.example.piano.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.piano.ui.theme.PianoTheme

/**
 * 加载失败 / 无网络时的可复用错误视图。
 * 居中显示图标、提示文案和重试按钮。
 *
 * @param onClick 点击重试按钮的回调
 * @param modifier 修饰符
 * @param buttonText 按钮文案，默认「重试」
 * @param hintText 提示文案，默认「网络异常，请检查网络后重试」
 */
@Composable
fun NetworkErrorView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonText: String = "重试",
    hintText: String = "网络异常，请检查网络后重试"
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.CloudOff,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = PianoTheme.colors.onSurface.copy(alpha = 0.4f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = hintText,
                style = MaterialTheme.typography.bodyMedium,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.padding(top = 0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PianoTheme.colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(buttonText)
            }
        }
    }
}
