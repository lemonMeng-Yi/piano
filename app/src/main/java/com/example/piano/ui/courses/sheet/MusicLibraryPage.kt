package com.example.piano.ui.courses.sheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.piano.data.sheet.api.dto.SheetItemDTO
import com.example.piano.ui.components.NetworkErrorView
import com.example.piano.ui.theme.PianoTheme
import kotlinx.coroutines.launch

/** 曲谱库子 Tab：乐谱 / 收藏 */
private enum class SheetMusicTab { SHEET_MUSIC, FAVORITES }

private val SHEET_MUSIC_TAB_TITLES = listOf("乐谱", "收藏")

/** 曲谱预览图为空时使用的默认图片 URL */
private const val DEFAULT_SHEET_PREVIEW_URL =
    "https://piano-course.oss-cn-beijing.aliyuncs.com/sheets/f6f87a8fce65319b776005f7a15f0640.jpg?Expires=1773036495&OSSAccessKeyId=TMP.3Kntks83q4iwimPswy37Up6V1LPxdshVoazghRbp1ku8BX9kDgs6GixHLhzvdnVc6jqGRLf8LQsMizbym7M8YyVZFaJxQk&Signature=AuKX1N9lInSOo%2B9tD5A0lAuVtVk%3D"

/** 曲谱库列表项 UI 数据 */
data class SheetMusicItem(
    val id: Long,
    val title: String,
    val tags: List<String>,
    val singerName: String,
    val likeCount: String,
    val favorited: Boolean = false,
    val previewImageUrl: String? = null
)

private fun formatFavoriteCount(n: Int): String = when {
    n >= 10000 -> "%.1fw".format(n / 10000.0)
    n >= 1000 -> "%.1fk".format(n / 1000.0)
    else -> "$n"
}

private fun SheetItemDTO.toSheetMusicItem(): SheetMusicItem {
    val tagList = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    return SheetMusicItem(
        id = id,
        title = title,
        tags = tagList,
        singerName = artist,
        likeCount = formatFavoriteCount(favoriteCount),
        favorited = favorited,
        previewImageUrl = previewImageUrl
    )
}

@Composable
fun MusicLibraryContent(
    onOpenSheetDetail: (Long) -> Unit = {},
    viewModel: SheetViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val listState by viewModel.listState.collectAsState()
    val favoritesState by viewModel.favoritesState.collectAsState()

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == SheetMusicTab.FAVORITES.ordinal) {
            viewModel.loadFavorites()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 曲谱库内子 Tab：乐谱 | 收藏（字体调大）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SHEET_MUSIC_TAB_TITLES.forEachIndexed { index, title ->
                val tab = SheetMusicTab.entries[index]
                val selected = pagerState.currentPage == index
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 20.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) PianoTheme.colors.primary else PianoTheme.colors.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                if (selected) PianoTheme.colors.primary
                                else Color.Transparent
                            )
                    )
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            when (SheetMusicTab.entries[page]) {
                SheetMusicTab.SHEET_MUSIC -> SheetMusicListContent(
                    listState = listState,
                    onRetry = { viewModel.loadList() },
                    onOpenSheetDetail = onOpenSheetDetail
                )
                SheetMusicTab.FAVORITES -> FavoritesContent(
                    favoritesState = favoritesState,
                    onRetry = { viewModel.loadFavorites() },
                    onOpenSheetDetail = onOpenSheetDetail
                )
            }
        }
    }
}

@Composable
private fun SheetMusicListContent(
    listState: SheetListUiState,
    onRetry: () -> Unit,
    onOpenSheetDetail: (Long) -> Unit
) {
    when (listState) {
        is SheetListUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PianoTheme.colors.primary)
            }
        }
        is SheetListUiState.Error -> {
            NetworkErrorView(
                modifier = Modifier.fillMaxSize(),
                hintText = listState.message,
                onClick = onRetry
            )
        }
        is SheetListUiState.Success -> {
            val items = listState.list.map { it.toSheetMusicItem() }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                items.forEach { item ->
                    SheetMusicListItem(
                        item = item,
                        onClick = { onOpenSheetDetail(item.id) },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun FavoritesContent(
    favoritesState: SheetFavoritesUiState,
    onRetry: () -> Unit,
    onOpenSheetDetail: (Long) -> Unit
) {
    when (favoritesState) {
        is SheetFavoritesUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PianoTheme.colors.primary)
            }
        }
        is SheetFavoritesUiState.NeedLogin -> {
            NetworkErrorView(
                modifier = Modifier.fillMaxSize(),
                hintText = "请先登录",
                buttonText = "去登录",
                onClick = onRetry
            )
        }
        is SheetFavoritesUiState.Error -> {
            NetworkErrorView(
                modifier = Modifier.fillMaxSize(),
                hintText = favoritesState.message,
                onClick = onRetry
            )
        }
        is SheetFavoritesUiState.Success -> {
            val items = favoritesState.list.map { it.toSheetMusicItem() }
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = PianoTheme.colors.onSurface.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "暂无收藏",
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = 16.sp,
                            color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                ) {
                    Spacer(modifier = Modifier.height(8.dp))
                    items.forEach { item ->
                        SheetMusicListItem(
                            item = item,
                            onClick = { onOpenSheetDetail(item.id) },
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun SheetMusicListItem(
    item: SheetMusicItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = PianoTheme.colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.previewImageUrl ?: DEFAULT_SHEET_PREVIEW_URL,
                contentDescription = item.title,
                modifier = Modifier
                    .size(width = 100.dp, height = 72.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = PianoTheme.colors.onSurface,
                    maxLines = 2
                )
                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        item.tags.take(4).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = PianoTheme.colors.onSurface.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = tag,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontSize = 13.sp,
                                    color = PianoTheme.colors.onSurface.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.singerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 14.sp,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = item.likeCount,
                        style = MaterialTheme.typography.labelMedium,
                        fontSize = 13.sp,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
