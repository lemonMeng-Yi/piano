package com.example.piano.ui.profile.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.piano.ui.auth.viewmodel.AuthViewModel
import com.example.piano.ui.components.SnackBarManager

@Composable
fun ProfilePage(
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var darkMode by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Profile Header Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "张",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "张小明",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "piano_lover@example.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { },
                            label = { Text("中级学员") },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                labelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        AssistChip(
                            onClick = { },
                            label = { Text("Lv. 12") }
                        )
                    }
                }
            }
        }

        // Stats Overview
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ProfileStatCard(
                modifier = Modifier.weight(1f),
                value = "24",
                label = "练习天数",
                valueColor = MaterialTheme.colorScheme.primary
            )
            ProfileStatCard(
                modifier = Modifier.weight(1f),
                value = "12",
                label = "完成曲目",
                valueColor = MaterialTheme.colorScheme.secondary
            )
            ProfileStatCard(
                modifier = Modifier.weight(1f),
                value = "8",
                label = "获得徽章",
                valueColor = MaterialTheme.colorScheme.onSurface
            )
        }

        // Settings Section
        Text(
            text = "设置",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.Settings,
                    label = "账户设置",
                    onClick = { },
                    showDivider = true
                )
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    label = "通知设置",
                    badge = "3",
                    onClick = { },
                    showDivider = true
                )
                SettingsItem(
                    icon = if (darkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                    label = "深色模式",
                    hasToggle = true,
                    toggleValue = darkMode,
                    onToggle = { darkMode = !darkMode },
                    showDivider = false
                )
            }
        }

        // Support Section
        Text(
            text = "支持",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column {
                SettingsItem(
                    icon = Icons.Default.HelpOutline,
                    label = "帮助中心",
                    onClick = { },
                    showDivider = true
                )
                SettingsItem(
                    icon = Icons.Default.ExitToApp,
                    label = "退出登录",
                    onClick = {
                        // 调用退出登录，成功后直接跳转到登录页
                        authViewModel.logout { success, errorMessage ->
                            if (success) {
                                SnackBarManager.showSuccess("退出登录成功")
                                // 直接调用 onLogout，更新登录状态并跳转
                                onLogout()
                            } else {
                                SnackBarManager.showError(errorMessage ?: "退出登录失败")
                            }
                        }
                    },
                    iconTint = MaterialTheme.colorScheme.error,
                    showDivider = false
                )
            }
        }

        // App Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "AI Piano Practice v1.0.0",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "© 2025 智能钢琴陪练平台",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ProfileStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    label: String,
    badge: String? = null,
    hasToggle: Boolean = false,
    toggleValue: Boolean = false,
    onToggle: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    iconTint: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
    showDivider: Boolean
) {
    Column {
        Surface(
            onClick = {
                if (hasToggle) {
                    onToggle?.invoke()
                } else {
                    onClick?.invoke()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                if (badge != null) {
                    AssistChip(
                        onClick = { },
                        label = { Text(badge) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            labelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                if (hasToggle) {
                    Switch(
                        checked = toggleValue,
                        onCheckedChange = { onToggle?.invoke() }
                    )
                } else {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 52.dp)
            )
        }
    }
}
