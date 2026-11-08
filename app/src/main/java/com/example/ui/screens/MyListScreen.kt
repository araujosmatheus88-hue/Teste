package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WatchProgress
import com.example.ui.components.DramaCard
import com.example.ui.theme.*
import com.example.viewmodel.DramaBoxViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyListScreen(
    viewModel: DramaBoxViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val favoritesIds by viewModel.favorites.collectAsState()
    val allDramas by viewModel.dramas.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()

    val favoriteDramas = allDramas.filter { favoritesIds.contains(it.id) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("my_list_screen")
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DeepBlack,
            contentColor = TextWhite,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = NeonPink,
                    height = 3.dp
                )
            },
            divider = {
                HorizontalDivider(color = DeepCardBorder, thickness = 1.dp)
            }
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = null,
                            tint = if (selectedTabIndex == 0) NeonPink else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Favoritos (${favoriteDramas.size})",
                            fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTabIndex == 0) TextWhite else TextMuted
                        )
                    }
                }
            )

            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = if (selectedTabIndex == 1) NeonPink else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Histórico (${watchHistory.size})",
                            fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTabIndex == 1) TextWhite else TextMuted
                        )
                    }
                }
            )
        }

        when (selectedTabIndex) {
            0 -> {
                // Favorites Grid
                if (favoriteDramas.isEmpty()) {
                    EmptyListPlaceholder(
                        title = "Sua lista de favoritos está vazia",
                        subtitle = "Adicione mininovelas aos favoritos clicando no ícone de marcador para assistir depois."
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 105.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(favoriteDramas) { drama ->
                            DramaCard(
                                drama = drama,
                                onClick = { viewModel.openDramaDetail(drama.id) },
                                cardWidth = 105
                            )
                        }
                    }
                }
            }
            1 -> {
                // Watch History List
                if (watchHistory.isEmpty()) {
                    EmptyListPlaceholder(
                        title = "Nenhum histórico de reprodução",
                        subtitle = "Comece a assistir episódios para ver seu progresso salvo automaticamente aqui."
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 90.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(watchHistory) { item ->
                            HistoryItemCard(
                                item = item,
                                onResumeClick = {
                                    viewModel.playEpisode(item.dramaId, item.episodeId)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: WatchProgress,
    onResumeClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onResumeClick() }
            .testTag("history_item_${item.dramaId}_${item.episodeId}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.dramaTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.episodeTitle,
                        style = MaterialTheme.typography.bodySmall.copy(color = Lavender)
                    )
                }

                FilledIconButton(
                    onClick = onResumeClick,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = NeonPink,
                        contentColor = TextWhite
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Continuar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { item.progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = NeonPink,
                trackColor = DeepSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val currentMin = item.currentPositionMs / 60000
                val currentSec = (item.currentPositionMs % 60000) / 1000
                val totalMin = item.totalDurationMs / 60000
                val totalSec = (item.totalDurationMs % 60000) / 1000
                Text(
                    text = "%02d:%02d / %02d:%02d".format(currentMin, currentSec, totalMin, totalSec),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextSubtle)
                )

                val dateStr = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(item.lastPlayedAt))
                Text(
                    text = "Visto em $dateStr",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = TextSubtle)
                )
            }
        }
    }
}

@Composable
private fun EmptyListPlaceholder(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                lineHeight = 18.sp
            )
        }
    }
}
