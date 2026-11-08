package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Drama
import com.example.model.Episode
import com.example.ui.theme.*
import com.example.viewmodel.DramaBoxViewModel

@Composable
fun DramaDetailScreen(
    dramaId: String,
    viewModel: DramaBoxViewModel,
    modifier: Modifier = Modifier
) {
    val drama by viewModel.selectedDrama.collectAsState()
    val allEpisodesMap by viewModel.episodes.collectAsState()
    val episodes = allEpisodesMap[dramaId] ?: emptyList()
    val favorites by viewModel.favorites.collectAsState()
    val isFavorite = favorites.contains(dramaId)

    if (drama == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DeepBlack),
            contentAlignment = Alignment.Center
        ) {
            Text("Mininovela não encontrada", color = TextWhite)
        }
        return
    }

    val currentDrama = drama!!

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("drama_detail_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Hero Backdrop with back button & favorite
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // Banner Image
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1794160023799),
                    contentDescription = currentDrama.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    DeepBlack.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    DeepBlack.copy(alpha = 0.9f),
                                    DeepBlack
                                )
                            )
                        )
                )

                // Top Bar with Back Button & Share/Fav
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.65f))
                            .testTag("detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleFavorite(currentDrama.id) },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DeepBlack.copy(alpha = 0.65f))
                            .testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "Favoritar",
                            tint = if (isFavorite) NeonPink else TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Title and Metadata
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Brush.horizontalGradient(listOf(NeonPink, ElectricPurple)))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = currentDrama.categoryName.uppercase(),
                                color = TextWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "${currentDrama.releaseYear} • ${currentDrama.language}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f".format(currentDrama.rating),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = currentDrama.title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp
                    ),
                    color = TextWhite
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentDrama.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextMuted,
                        lineHeight = 20.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Primary Action Button: "Assistir Ep. 1"
                Button(
                    onClick = {
                        val firstEp = episodes.firstOrNull()
                        if (firstEp != null) {
                            viewModel.playEpisode(currentDrama.id, firstEp.id)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPink,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("detail_play_ep1_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assistir Episódio 1",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Episodes Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Episódios (${episodes.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    )

                    Text(
                        text = "Temporada 1",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Lavender,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Episode List Items
        items(episodes) { episode ->
            EpisodeListItem(
                episode = episode,
                coverUrl = currentDrama.coverVerticalUrl,
                onClick = {
                    viewModel.playEpisode(currentDrama.id, episode.id)
                }
            )
        }
    }
}

@Composable
private fun EpisodeListItem(
    episode: Episode,
    coverUrl: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .border(1.dp, DeepCardBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("episode_item_${episode.episodeNumber}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Episode Thumbnail with Ep number
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(60.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DeepSurfaceVariant)
            ) {
                when (coverUrl) {
                    "img_drama_secret_heir" -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_drama_secret_heir_1794160037811),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    "img_drama_revenge_wife" -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_drama_revenge_wife_1794160048904),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1794160023799),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Play icon in center
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(DeepBlack.copy(alpha = 0.7f))
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = TextWhite,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Ep Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(NeonPink)
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "EP ${episode.episodeNumber}",
                        color = TextWhite,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = episode.description.ifBlank { "Episódio ${episode.episodeNumber} da mininovela" },
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                val minutes = episode.durationSeconds / 60
                val seconds = episode.durationSeconds % 60
                Text(
                    text = "%02d:%02d min".format(minutes, seconds),
                    style = MaterialTheme.typography.labelSmall.copy(color = Lavender, fontSize = 10.sp)
                )
            }
        }
    }
}
