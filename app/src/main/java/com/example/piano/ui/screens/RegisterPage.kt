package com.example.pianotutor.ui.screens

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
import com.example.piano.ui.theme.PianoBlue40
import com.example.piano.ui.theme.PianoBlueGrey40

@Composable
fun RegisterPage(
    navigationActions: NavigationActions,
    onRegisterClick: (String, String, String) -> Unit = { _, _, _ -> }
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo区域
            Spacer(modifier = Modifier.height(20.dp))
            
            // 标题
            Text(
                text = "小AI在线",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = PianoBlueGrey40,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "错音下线！",
                fontSize = 20.sp,
                color = PianoBlue40,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
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
                        tint = PianoBlue40
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PianoBlue40,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = PianoBlue40,
                    unfocusedLabelColor = Color(0xFF757575)
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
                        tint = PianoBlue40
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PianoBlue40,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = PianoBlue40,
                    unfocusedLabelColor = Color(0xFF757575)
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
                            tint = PianoBlue40
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 确认密码输入框
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("确认密码") },
                placeholder = { Text("请再次输入密码") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "确认密码",
                        tint = PianoBlue40
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PianoBlue40,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedLabelColor = PianoBlue40,
                    unfocusedLabelColor = Color(0xFF757575)
                ),
                visualTransformation = if (confirmPasswordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (confirmPasswordVisible) "隐藏密码" else "显示密码",
                            tint = PianoBlue40
                        )
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = confirmPassword.isNotBlank() && password != confirmPassword
            )
            
            // 密码不一致提示
            if (confirmPassword.isNotBlank() && password != confirmPassword) {
                Text(
                    text = "两次输入的密码不一致",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 注册按钮
            Button(
                onClick = { onRegisterClick(username, password, confirmPassword) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PianoBlue40,
                    contentColor = Color.White
                ),
                enabled = username.isNotBlank() && 
                         password.isNotBlank() && 
                         confirmPassword.isNotBlank() &&
                         password == confirmPassword
            ) {
                Text(
                    text = "注册",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 返回登录入口
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已有账号？",
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = { navigationActions.navigateUp() }
                ) {
                    Text(
                        text = "立即登录",
                        fontSize = 14.sp,
                        color = PianoBlue40,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
