package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.Drama
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.DramaCard
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*
import com.example.viewmodel.DramaBoxViewModel
import com.example.viewmodel.Screen

@Composable
fun HomeScreen(
    viewModel: DramaBoxViewModel,
    modifier: Modifier = Modifier
) {
    val dramas by viewModel.dramas.collectAsState()
    val watchHistory by viewModel.watchHistory.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val featuredDrama = dramas.firstOrNull { it.isFeatured } ?: dramas.firstOrNull()
    val continueWatching = watchHistory.filterNot { it.isCompleted }.take(5)
    val trendingDramas = dramas.filter { it.isTrending }
    val newDramas = dramas.filter { it.isNewRelease }
    val popularDramas = dramas.sortedByDescending { it.viewsCount }
    val recommendedDramas = dramas.sortedByDescending { it.rating }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Hero Featured Banner
        if (featuredDrama != null) {
            item {
                FeaturedHeroBanner(
                    drama = featuredDrama,
                    isFavorite = favorites.contains(featuredDrama.id),
                    onPlayClick = {
                        val firstEp = viewModel.episodes.value[featuredDrama.id]?.firstOrNull()
                        if (firstEp != null) {
                            viewModel.playEpisode(featuredDrama.id, firstEp.id)
                        } else {
                            viewModel.openDramaDetail(featuredDrama.id)
                        }
                    },
                    onFavoriteClick = { viewModel.toggleFavorite(featuredDrama.id) },
                    onBannerClick = { viewModel.openDramaDetail(featuredDrama.id) }
                )
            }
        }

        // 2. Categories Horizontal Bar
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = viewModel.selectedCategoryId.collectAsState().value == cat.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.selectCategory(cat.id)
                            viewModel.navigateTo(Screen.Explore)
                        },
                        label = {
                            Text(
                                text = cat.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = DeepSurfaceVariant,
                            labelColor = TextMuted,
                            selectedContainerColor = ElectricPurple,
                            selectedLabelColor = TextWhite
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = DeepCardBorder,
                            selectedBorderColor = NeonPink
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // 3. Continuar Assistindo (Continue Watching)
        if (continueWatching.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Continuar Assistindo",
                    onActionClick = { viewModel.navigateTo(Screen.MyList) }
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(continueWatching) { item ->
                        ContinueWatchingCard(
                            progress = item,
                            onResumeClick = {
                                viewModel.playEpisode(item.dramaId, item.episodeId)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 4. Mininovelas em Alta (Trending)
        if (trendingDramas.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Mininovelas em Alta 🔥",
                    onActionClick = { viewModel.navigateTo(Screen.Explore) }
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trendingDramas) { drama ->
                        DramaCard(
                            drama = drama,
                            onClick = { viewModel.openDramaDetail(drama.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 5. Novidades (New Releases)
        if (newDramas.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Novidades da Semana ⚡",
                    onActionClick = { viewModel.navigateTo(Screen.NewReleases) }
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(newDramas) { drama ->
                        DramaCard(
                            drama = drama,
                            onClick = { viewModel.openDramaDetail(drama.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 6. Mais Populares (Most Popular)
        if (popularDramas.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Mais Populares 👑",
                    onActionClick = { viewModel.navigateTo(Screen.Explore) }
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(popularDramas) { drama ->
                        DramaCard(
                            drama = drama,
                            onClick = { viewModel.openDramaDetail(drama.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // 7. Recomendados para Você (Recommended)
        if (recommendedDramas.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Recomendados para Você ✨",
                    onActionClick = { viewModel.navigateTo(Screen.Explore) }
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(recommendedDramas) { drama ->
                        DramaCard(
                            drama = drama,
                            onClick = { viewModel.openDramaDetail(drama.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@Composable
private fun FeaturedHeroBanner(
    drama: Drama,
    isFavorite: Boolean,
    onPlayClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onBannerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp)
            .clickable { onBannerClick() }
            .testTag("featured_hero_banner")
    ) {
        // Background Hero Artwork
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner_1794160023799),
            contentDescription = drama.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Gradient fade into Deep Black
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            DeepBlack.copy(alpha = 0.4f),
                            DeepBlack.copy(alpha = 0.95f),
                            DeepBlack
                        )
                    )
                )
        )

        // Content Overlay at bottom of Hero
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Category & Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.horizontalGradient(listOf(NeonPink, ElectricPurple)))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "EM DESTAQUE",
                        color = TextWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "${drama.categoryName} • ${drama.totalEpisodes} Episódios",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Lavender,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = drama.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 24.sp
                ),
                color = TextWhite
            )

            // Short Description
            Text(
                text = drama.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
            )

            // Actions: Assistir Agora + Minha Lista
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPink,
                        contentColor = TextWhite
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("hero_play_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Assistir Agora",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                OutlinedButton(
                    onClick = onFavoriteClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = DeepSurface.copy(alpha = 0.7f),
                        contentColor = if (isFavorite) NeonPink else TextWhite
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.horizontalGradient(
                            if (isFavorite) listOf(NeonPink, ElectricPurple) else listOf(DeepCardBorder, DeepCardBorder)
                        )
                    ),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(42.dp)
                        .testTag("hero_fav_button")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Bookmark else Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isFavorite) "Na Lista" else "Minha Lista",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
