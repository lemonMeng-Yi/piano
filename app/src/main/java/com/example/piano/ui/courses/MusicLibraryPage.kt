package com.example.piano.ui.courses

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.piano.ui.theme.PianoTheme
import kotlinx.coroutines.launch

/** 曲谱库子 Tab：乐谱 / 收藏 */
private enum class SheetMusicTab { SHEET_MUSIC, FAVORITES }

private val SHEET_MUSIC_TAB_TITLES = listOf("乐谱", "收藏")

/** 曲谱库列表项数据（显示歌手名称） */
data class SheetMusicItem(
    val title: String,
    val tags: List<String>,
    val singerName: String,
    val likeCount: String
)

@Composable
fun MusicLibraryContent() {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

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
                        fontSize = 18.sp,
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
                SheetMusicTab.SHEET_MUSIC -> SheetMusicListContent()
                SheetMusicTab.FAVORITES -> FavoritesContent()
            }
        }
    }
}

@Composable
private fun SheetMusicListContent() {
    val items = remember {
        listOf(
            SheetMusicItem(
                title = "蒲公英的约定-副歌",
                tags = listOf("副歌", "C大调", "可转简谱"),
                singerName = "周杰伦",
                likeCount = "1.3w"
            ),
            SheetMusicItem(
                title = "明明就 (C调)-周杰伦",
                tags = listOf("C大调", "指法"),
                singerName = "周杰伦",
                likeCount = "1.0w"
            ),
            SheetMusicItem(
                title = "天空之城 (C调)",
                tags = listOf("可转简谱", "指法"),
                singerName = "久石让",
                likeCount = "2149"
            ),
            SheetMusicItem(
                title = "听妈妈的话 (C调简单版)-周杰伦",
                tags = listOf("简单版", "C大调"),
                singerName = "周杰伦",
                likeCount = "4047"
            ),
            SheetMusicItem(
                title = "婚礼进行曲",
                tags = listOf("经典"),
                singerName = "瓦格纳",
                likeCount = "454"
            )
        )
    }

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
                onClick = { },
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FavoritesContent() {
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
                color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
            )
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
            Box(
                modifier = Modifier
                    .size(width = 100.dp, height = 72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PianoTheme.colors.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PianoTheme.colors.primary.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "播放",
                        tint = PianoTheme.colors.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall,
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
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PianoTheme.colors.onSurface.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
                        style = MaterialTheme.typography.bodySmall,
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
                        style = MaterialTheme.typography.labelSmall,
                        color = PianoTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
