package com.example.piano.ui.auth.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piano.navigation.NavigationActions
import com.example.piano.ui.theme.PianoTheme

@Composable
fun LoginPage(
    navigationActions: NavigationActions,
    onLoginClick: (String, String) -> Unit = { _, _ -> }
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PianoTheme.colors.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo区域 - 使用应用图标
            Spacer(modifier = Modifier.height(20.dp))
            
            // 标题
            Text(
                text = "小AI在线",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PianoTheme.colors.textPrimary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "错音下线！",
                fontSize = 20.sp,
                color = PianoTheme.colors.textSecondary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // 用户名输入框
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("用户名") },
                placeholder = { Text("请输入用户名") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "用户名",
                        tint = PianoTheme.colors.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PianoTheme.colors.primary,
                    unfocusedBorderColor = PianoTheme.colors.border,
                    focusedLabelColor = PianoTheme.colors.primary,
                    unfocusedLabelColor = PianoTheme.colors.textSecondary,
                    focusedTextColor = PianoTheme.colors.textPrimary,
                    unfocusedTextColor = PianoTheme.colors.textPrimary,
                    cursorColor = PianoTheme.colors.primary
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 密码输入框
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("密码") },
                placeholder = { Text("请输入密码") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "密码",
                        tint = PianoTheme.colors.primary
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PianoTheme.colors.primary,
                    unfocusedBorderColor = PianoTheme.colors.border,
                    focusedLabelColor = PianoTheme.colors.primary,
                    unfocusedLabelColor = PianoTheme.colors.textSecondary,
                    focusedTextColor = PianoTheme.colors.textPrimary,
                    unfocusedTextColor = PianoTheme.colors.textPrimary,
                    cursorColor = PianoTheme.colors.primary
                ),
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                            tint = PianoTheme.colors.primary
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 登录按钮
            Button(
                onClick = { onLoginClick(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PianoTheme.colors.primary,
                    contentColor = PianoTheme.colors.onPrimary
                ),
                enabled = username.isNotBlank() && password.isNotBlank()
            ) {
                Text(
                    text = "登录",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 忘记密码和注册入口
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 忘记密码链接
                TextButton(
                    onClick = { navigationActions.navigateToForgotPassword() }
                ) {
                    Text(
                        text = "忘记密码？",
                        fontSize = 14.sp,
                        color = PianoTheme.colors.primary
                    )
                }
                
                // 注册入口
                TextButton(
                    onClick = { navigationActions.navigateToRegister() }
                ) {
                    Text(
                        text = "注册账号",
                        fontSize = 14.sp,
                        color = PianoTheme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
