package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.model.User
import com.example.model.UserRole
import com.example.model.WatchProgress
import com.example.ui.theme.*
import com.example.viewmodel.Screen

@Composable
fun DramaBoxTopBar(
    currentUser: User?,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DeepBlack.copy(alpha = 0.95f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { /* Brand tap */ }
                    .padding(4.dp)
            ) {
                // Neon logo box
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(NeonPink, ElectricPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Drama Box Logo",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DRAMA",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = TextWhite
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "BOX",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = NeonPink
                        )
                    }
                    Text(
                        text = "MININOVELAS PREMIUM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            letterSpacing = 1.5.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Lavender
                    )
                }
            }

            // Right Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Admin Badge / Shortcut if user is Admin
                if (currentUser?.role == UserRole.ADMIN) {
                    FilledTonalButton(
                        onClick = onAdminClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ElectricPurple.copy(alpha = 0.35f),
                            contentColor = Lavender
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .border(1.dp, ElectricPurple, RoundedCornerShape(16.dp))
                            .testTag("admin_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin",
                            modifier = Modifier.size(16.dp),
                            tint = AccentYellow
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "PAINEL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )
                    }
                }

                // Search icon
                IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DeepSurfaceVariant)
                        .testTag("header_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar dramas",
                        tint = TextWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Avatar / Profile icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricPurple, NeonPink)
                            )
                        )
                        .border(1.5.dp, if (currentUser?.role == UserRole.ADMIN) AccentYellow else NeonPink, CircleShape)
                        .clickable { onProfileClick() }
                        .testTag("header_profile_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.name?.take(1)?.uppercase() ?: "D",
                        color = TextWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DramaBoxBottomBar(
    currentScreen: Screen,
    onTabSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DeepBlack,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val tabs = listOf(
                NavigationItem("Início", Screen.Home, Icons.Filled.Home, Icons.Outlined.Home, "tab_home"),
                NavigationItem("Explorar", Screen.Explore, Icons.Filled.Explore, Icons.Outlined.Explore, "tab_explore"),
                NavigationItem("Novidades", Screen.NewReleases, Icons.Filled.Whatshot, Icons.Outlined.Whatshot, "tab_new"),
                NavigationItem("Minha Lista", Screen.MyList, Icons.Filled.Bookmark, Icons.Outlined.BookmarkBorder, "tab_my_list"),
                NavigationItem("Perfil", Screen.Profile, Icons.Filled.Person, Icons.Outlined.Person, "tab_profile")
            )

            tabs.forEach { tab ->
                val isSelected = currentScreen == tab.screen
                val tint by animateColorAsState(
                    targetValue = if (isSelected) NeonPink else TextMuted,
                    label = "TabTint"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab.screen) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag(tab.tag)
                ) {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = tint
                    )
                }
            }
        }
    }
}

private data class NavigationItem(
    val title: String,
    val screen: Screen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
)

@Composable
fun SectionHeader(
    title: String,
    onActionClick: (() -> Unit)? = null,
    actionLabel: String = "Ver Todos",
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Neon accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(NeonPink, ElectricPurple)
                        )
                    )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = TextWhite
            )
        }

        if (onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = Lavender
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onActionClick() }
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun DramaCard(
    drama: Drama,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cardWidth: Int = 130
) {
    Column(
        modifier = modifier
            .width(cardWidth.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .testTag("drama_card_${drama.id}")
    ) {
        // 2:3 aspect ratio cover box
        Box(
            modifier = Modifier
                .width(cardWidth.dp)
                .height((cardWidth * 1.5).dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DeepCard)
                .border(1.dp, DeepCardBorder, RoundedCornerShape(10.dp))
        ) {
            // Cover Image: check if it's local generated drawable or external URL
            when (drama.coverVerticalUrl) {
                "img_drama_secret_heir" -> {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.img_drama_secret_heir_1794160037811),
                        contentDescription = drama.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                "img_drama_revenge_wife" -> {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.img_drama_revenge_wife_1794160048904),
                        contentDescription = drama.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                "img_hero_banner" -> {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.img_hero_banner_1794160023799),
                        contentDescription = drama.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                else -> {
                    if (drama.coverVerticalUrl.startsWith("http")) {
                        AsyncImage(
                            model = drama.coverVerticalUrl,
                            contentDescription = drama.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Fallback gradient cover
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(DeepSurfaceVariant, DeepBlack)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                tint = NeonPink,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }

            // Top Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (drama.isNewRelease) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.horizontalGradient(listOf(NeonPink, ElectricPurple)))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "NOVO",
                            color = TextWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else if (drama.isFeatured) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Brush.horizontalGradient(listOf(AccentYellow, AccentOrange)))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "TOP",
                            color = DeepBlack,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Rating badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DeepBlack.copy(alpha = 0.75f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AccentYellow,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "%.1f".format(drama.rating),
                            color = TextWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Bottom Gradient & Episode count
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, DeepBlack.copy(alpha = 0.95f))
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${drama.totalEpisodes} Eps",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Lavender
                    )

                    // Format views (e.g. 348K)
                    val viewsStr = if (drama.viewsCount >= 1000) "${drama.viewsCount / 1000}K" else "${drama.viewsCount}"
                    Text(
                        text = "$viewsStr views",
                        fontSize = 9.sp,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = drama.title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextWhite
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Category Tag
        Text(
            text = drama.categoryName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                color = TextMuted
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ContinueWatchingCard(
    progress: WatchProgress,
    onResumeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCard),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .width(240.dp)
            .border(1.dp, DeepCardBorder, RoundedCornerShape(12.dp))
            .clickable { onResumeClick() }
            .testTag("continue_card_${progress.dramaId}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(DeepSurfaceVariant)
            ) {
                // Backdrop
                when (progress.coverUrl) {
                    "img_drama_secret_heir" -> {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.img_drama_secret_heir_1794160037811),
                            contentDescription = progress.dramaTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    "img_drama_revenge_wife" -> {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.img_drama_revenge_wife_1794160048904),
                            contentDescription = progress.dramaTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        androidx.compose.foundation.Image(
                            painter = painterResource(id = R.drawable.img_hero_banner_1794160023799),
                            contentDescription = progress.dramaTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Dark vignette
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, DeepBlack.copy(alpha = 0.8f))
                            )
                        )
                )

                // Play icon button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(NeonPink.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Continuar",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Progress Bar at bottom of thumbnail
                LinearProgressIndicator(
                    progress = { progress.progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = NeonPink,
                    trackColor = DeepBlack.copy(alpha = 0.6f)
                )
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = progress.dramaTitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "EP ${progress.episodeNumber}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Lavender
                        )
                    )
                    Text(
                        text = "Continuar",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = NeonPink,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
