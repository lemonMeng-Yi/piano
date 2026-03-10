package com.example.piano.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.theme.PianoTheme

/**
 * 权限设置：已开启点击跳转系统设置可关闭；未开启点击弹窗申请，若已选「不再询问」则跳转设置。
 * 包含：相机、照片、麦克风、位置。从设置返回时自动刷新状态。
 */
@Composable
fun PermissionSettingsPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = activity as? LifecycleOwner
    val refreshState = remember { mutableStateOf(0) }
    val requestedPermissions = remember { mutableStateOf(setOf<String>()) }

    fun isGranted(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    if (lifecycleOwner != null) {
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) refreshState.value++
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
        }
    }

    val storagePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { refreshState.value++ }

    fun openAppSettings() {
        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        })
    }

    fun requestPermission(permission: String?) {
        if (permission.isNullOrBlank() || activity == null) {
            openAppSettings()
            return
        }
        val canShowDialog = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
        val alreadyRequested = permission in requestedPermissions.value
        if (!alreadyRequested || canShowDialog) {
            requestedPermissions.value = requestedPermissions.value + permission
            permissionLauncher.launch(permission)
        } else {
            openAppSettings()
        }
    }

    val permissions = listOf(
        PermissionItem(Icons.Default.CameraAlt, "相机", "用于拍摄头像", Manifest.permission.CAMERA),
        PermissionItem(Icons.Default.PhotoLibrary, "照片", "用于选择头像图片", storagePermission),
        PermissionItem(Icons.Default.Mic, "麦克风", "用于跟弹时识别琴声", Manifest.permission.RECORD_AUDIO),
        PermissionItem(Icons.Default.LocationOn, "位置", "用于定位相关功能", Manifest.permission.ACCESS_FINE_LOCATION)
    )

    Scaffold(topBar = { BackTitleTopBar(title = "权限设置", onBack = onBack) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = "管理应用所需权限，未授予时相关功能可能无法使用。",
                style = MaterialTheme.typography.bodyLarge,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            key(refreshState.value) {
                Card {
                    permissions.forEachIndexed { index, item ->
                        val granted = isGranted(item.permission)
                        PermissionSettingRow(
                            icon = item.icon,
                            label = item.label,
                            description = item.description,
                            granted = granted,
                            onClick = {
                                if (granted) openAppSettings() else requestPermission(item.permission)
                            }
                        )
                        if (index < permissions.size - 1) {
                            HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
                        }
                    }
                }
                Text(
                    text = "已开启：点击进入系统设置可关闭；未开启：点击弹出申请弹窗。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

private data class PermissionItem(
    val icon: ImageVector,
    val label: String,
    val description: String,
    val permission: String
)

@Composable
private fun PermissionSettingRow(
    icon: ImageVector,
    label: String,
    description: String,
    granted: Boolean,
    onClick: () -> Unit
) {
    val tint = PianoTheme.colors.onSurface
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = tint.copy(alpha = 0.6f),
                modifier = Modifier.padding(end = 14.dp).size(26.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = tint
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = tint.copy(alpha = 0.6f)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (granted) "已开启" else "未开启",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (granted) PianoTheme.colors.primary else tint.copy(alpha = 0.5f),
                    modifier = Modifier.padding(end = 4.dp)
                )
                if (granted) {
                    Text(
                        text = "点击可关闭",
                        style = MaterialTheme.typography.bodySmall,
                        color = tint.copy(alpha = 0.5f)
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = tint.copy(alpha = 0.4f)
            )
        }
    }
}
