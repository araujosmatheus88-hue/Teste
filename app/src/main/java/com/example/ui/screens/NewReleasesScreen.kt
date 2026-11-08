package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.R
import com.example.model.Drama
import com.example.ui.theme.*
import com.example.viewmodel.DramaBoxViewModel

@Composable
fun NewReleasesScreen(
    viewModel: DramaBoxViewModel,
    modifier: Modifier = Modifier
) {
    val dramas by viewModel.dramas.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val newReleases = dramas.filter { it.isPublished }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("new_releases_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Lançamentos &",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Novidades",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = NeonPink
                )
            }
            Text(
                text = "Mininovelas recém-adicionadas à plataforma Drama Box",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(newReleases) { drama ->
                NewReleaseCard(
                    drama = drama,
                    isFavorite = favorites.contains(drama.id),
                    onDramaClick = { viewModel.openDramaDetail(drama.id) },
                    onPlayClick = {
                        val firstEp = viewModel.episodes.value[drama.id]?.firstOrNull()
                        if (firstEp != null) {
                            viewModel.playEpisode(drama.id, firstEp.id)
                        } else {
                            viewModel.openDramaDetail(drama.id)
                        }
                    },
                    onFavoriteClick = { viewModel.toggleFavorite(drama.id) }
                )
            }
        }
    }
}

@Composable
private fun NewReleaseCard(
    drama: Drama,
    isFavorite: Boolean,
    onDramaClick: () -> Unit,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCard),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DeepCardBorder, RoundedCornerShape(14.dp))
            .clickable { onDramaClick() }
            .testTag("new_card_${drama.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Poster 2:3
            Box(
                modifier = Modifier
                    .width(95.dp)
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DeepSurfaceVariant)
            ) {
                when (drama.coverVerticalUrl) {
                    "img_drama_secret_heir" -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_drama_secret_heir_1794160037811),
                            contentDescription = drama.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    "img_drama_revenge_wife" -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_drama_revenge_wife_1794160048904),
                            contentDescription = drama.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1794160023799),
                            contentDescription = drama.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Badge Novidade
                Box(
                    modifier = Modifier
                        .padding(5.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.horizontalGradient(listOf(NeonPink, ElectricPurple)))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NOVO",
                        color = TextWhite,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Info & Description
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = drama.categoryName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Lavender,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentYellow,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "%.1f".format(drama.rating),
                                color = TextWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = drama.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = drama.description,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMuted,
                            lineHeight = 16.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${drama.totalEpisodes} Episódios • ${drama.language}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSubtle,
                            fontSize = 11.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPink,
                            contentColor = TextWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Assistir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onFavoriteClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DeepSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favoritar",
                            tint = if (isFavorite) NeonPink else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
