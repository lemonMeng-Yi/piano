package com.example.piano.ui.profile

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.piano.ui.auth.viewmodel.AuthViewModel
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.components.SnackBarManager
import com.example.piano.ui.theme.PianoTheme
import java.io.File
import java.io.FileOutputStream

@Composable
fun ProfileEditPage(
    onBack: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val profile by authViewModel.profile.collectAsState()
    val profileLoading by authViewModel.profileLoading.collectAsState()
    val updateLoading by authViewModel.updateProfileLoading.collectAsState()
    val context = LocalContext.current

    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var localAvatarUri by remember { mutableStateOf<Uri?>(null) }
    var initialized by remember { mutableStateOf(false) }

    // 进入编辑页时拉取最新个人信息并填充表单
    LaunchedEffect(Unit) {
        authViewModel.loadProfile()
    }
    LaunchedEffect(profile) {
        if (profile != null && !initialized) {
            nickname = profile!!.displayName().ifBlank { profile!!.username ?: "" }
            email = profile?.email ?: ""
            phone = profile?.phone ?: ""
            initialized = true
        }
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        localAvatarUri = uri
        val tempFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        if (tempFile.exists()) {
            authViewModel.uploadAvatar(tempFile) { success, msg ->
                if (success) {
                    SnackBarManager.showSuccess("头像已更新")
                    localAvatarUri = null
                } else {
                    SnackBarManager.showError(msg ?: "头像上传失败")
                }
            }
        }
    }

    var captureFile by remember { mutableStateOf<File?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && captureFile != null) {
            captureFile?.let { file ->
                authViewModel.uploadAvatar(file) { ok, msg ->
                    if (ok) {
                        SnackBarManager.showSuccess("头像已更新")
                        localAvatarUri = Uri.fromFile(file)
                    } else {
                        SnackBarManager.showError(msg ?: "头像上传失败")
                    }
                }
            }
            captureFile = null
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            captureFile = File(context.cacheDir, "avatar_capture_${System.currentTimeMillis()}.jpg")
            captureFile?.let { file ->
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                takePictureLauncher.launch(uri)
            }
        } else {
            SnackBarManager.showError("需要相机权限才能拍摄")
        }
    }

    fun launchCamera() {
        when {
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED -> {
                captureFile = File(context.cacheDir, "avatar_capture_${System.currentTimeMillis()}.jpg")
                captureFile?.let { file ->
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    takePictureLauncher.launch(uri)
                }
            }
            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(title = "编辑资料", onBack = onBack)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            if (profileLoading && profile == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
            // 头像区域：点击头像选图，点击右侧图标拍摄
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PianoTheme.colors.primary,
                                    PianoTheme.colors.secondary
                                )
                            )
                        )
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        localAvatarUri != null -> AsyncImage(
                            model = localAvatarUri,
                            contentDescription = "头像预览",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                        !profile?.avatar.isNullOrBlank() -> AsyncImage(
                            model = profile?.avatar,
                            contentDescription = "头像",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                        else -> Text(
                            text = profile?.username?.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                FloatingActionButton(
                    onClick = { launchCamera() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = 8.dp),
                    containerColor = PianoTheme.colors.primaryContainer,
                    contentColor = PianoTheme.colors.onPrimaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "拍摄照片")
                }
            }
            Text(
                text = "点击头像从相册选择，点击右侧图标拍摄",
                style = MaterialTheme.typography.bodyMedium,
                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("昵称", style = MaterialTheme.typography.bodyLarge) },
                placeholder = { Text("请输入昵称", style = MaterialTheme.typography.bodyMedium) },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("邮箱", style = MaterialTheme.typography.bodyLarge) },
                placeholder = { Text("请输入邮箱", style = MaterialTheme.typography.bodyMedium) },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("手机号", style = MaterialTheme.typography.bodyLarge) },
                placeholder = { Text("请输入手机号", style = MaterialTheme.typography.bodyMedium) },
                textStyle = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    authViewModel.updateProfile(
                        nickname = nickname.takeIf { it.isNotBlank() },
                        email = email.takeIf { it.isNotBlank() },
                        phone = phone.takeIf { it.isNotBlank() },
                        avatar = null,
                        onResult = { success, msg ->
                            if (success) {
                                SnackBarManager.showSuccess("保存成功")
                                onBack()
                            } else {
                                SnackBarManager.showError(msg ?: "保存失败")
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !updateLoading
            ) {
                if (updateLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    if (updateLoading) "保存中..." else "保存",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            }
        }
    }
}
