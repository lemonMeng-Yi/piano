package com.example.piano.ui.courses.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.piano.ui.components.BackTitleTopBar
import com.example.piano.ui.components.NetworkErrorView
import com.example.piano.ui.theme.PianoTheme

const val SHEET_ID_KEY = "sheetId"

@Composable
fun SheetDetailScreen(
    onBack: () -> Unit,
    viewModel: SheetDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val title = when (state) {
        is SheetDetailUiState.Success -> (state as SheetDetailUiState.Success).title
        else -> "曲谱详情"
    }

    Scaffold(
        topBar = {
            BackTitleTopBar(title = title, onBack = onBack)
        },
        containerColor = PianoTheme.colors.surface
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val s = state) {
                is SheetDetailUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PianoTheme.colors.primary)
                    }
                }
                is SheetDetailUiState.Error -> {
                    NetworkErrorView(
                        modifier = Modifier.fillMaxSize(),
                        hintText = s.message,
                        onClick = { viewModel.loadDetail() }
                    )
                }
                is SheetDetailUiState.Success -> {
                    val url = s.sheetDataUrl
                    if (!url.isNullOrEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            AsyncImage(
                                model = url,
                                contentDescription = s.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentScale = ContentScale.Fit
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "暂无曲谱图片",
                                style = MaterialTheme.typography.bodyLarge,
                                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}
