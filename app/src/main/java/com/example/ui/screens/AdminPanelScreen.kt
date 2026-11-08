package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.DramaBoxViewModel
import com.example.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab {
    DASHBOARD,
    DRAMAS,
    EPISODES,
    CATEGORIES,
    USERS,
    VPS_SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    viewModel: DramaBoxViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val dramas by viewModel.dramas.collectAsState()
    val allEpisodesMap by viewModel.episodes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val users by viewModel.users.collectAsState()
    val vpsSettings by viewModel.vpsSettings.collectAsState()

    var activeTab by remember { mutableStateOf(AdminTab.DASHBOARD) }
    var selectedDramaForEpisodes by remember { mutableStateOf<Drama?>(null) }

    // Dialog states
    var showDramaDialog by remember { mutableStateOf<Drama?>(null) } // null = closed, if non-null or empty = editing
    var isNewDramaMode by remember { mutableStateOf(false) }

    var showEpisodeDialog by remember { mutableStateOf<Episode?>(null) }
    var isNewEpisodeMode by remember { mutableStateOf(false) }

    var showCategoryDialog by remember { mutableStateOf<Category?>(null) }
    var isNewCategoryMode by remember { mutableStateOf(false) }

    BackHandler {
        viewModel.navigateBack()
    }

    // Security check: Only Admin authorized
    if (currentUser?.role != UserRole.ADMIN) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DeepBlack)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCard),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.border(1.dp, Color(0xFFFF5252), RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Acesso Restrito",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Apenas administradores autorizados têm permissão para acessar o painel de controle.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.navigateBack() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
                    ) {
                        Text("Voltar ao Aplicativo", color = TextWhite)
                    }
                }
            }
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .testTag("admin_panel_screen")
    ) {
        // Admin Top Header
        Surface(color = DeepSurface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DeepSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DRAMA BOX",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                                color = NeonPink
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ADMIN",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextWhite
                            )
                        }
                        Text(
                            text = "Painel de Controle Oficial",
                            style = MaterialTheme.typography.labelSmall.copy(color = Lavender, fontSize = 10.sp)
                        )
                    }
                }

                // Super Admin Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricPurple.copy(alpha = 0.35f))
                        .border(1.dp, ElectricPurple, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Matheus Araújo",
                        color = AccentYellow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Navigation Tabs in Admin
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(DeepSurface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf(
                AdminTab.DASHBOARD to "Dashboard",
                AdminTab.DRAMAS to "Mininovelas",
                AdminTab.EPISODES to "Episódios",
                AdminTab.CATEGORIES to "Categorias",
                AdminTab.USERS to "Usuários",
                AdminTab.VPS_SETTINGS to "VPS / Servidor"
            )

            items(tabs) { (tab, title) ->
                val isSelected = activeTab == tab
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) NeonPink else DeepSurfaceVariant)
                        .clickable { activeTab = tab }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) TextWhite else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Body Content by Tab
        when (activeTab) {
            AdminTab.DASHBOARD -> {
                AdminDashboardTab(
                    dramas = dramas,
                    allEpisodesMap = allEpisodesMap,
                    categories = categories,
                    users = users,
                    onNavigateToTab = { activeTab = it }
                )
            }
            AdminTab.DRAMAS -> {
                AdminDramasTab(
                    dramas = dramas,
                    onAddNew = {
                        isNewDramaMode = true
                        showDramaDialog = Drama(
                            id = "drama_${System.currentTimeMillis()}",
                            title = "",
                            description = "",
                            coverVerticalUrl = "img_drama_secret_heir",
                            bannerHorizontalUrl = "img_hero_banner",
                            categoryId = categories.firstOrNull()?.id ?: "cat_romance",
                            categoryName = categories.firstOrNull()?.name ?: "Romance",
                            genres = listOf("Romance", "Bilionários"),
                            language = "Português",
                            releaseYear = 2024,
                            totalEpisodes = 1,
                            isPublished = true,
                            isFeatured = false,
                            isTrending = false,
                            isNewRelease = true
                        )
                    },
                    onEdit = { drama ->
                        isNewDramaMode = false
                        showDramaDialog = drama
                    },
                    onDelete = { dramaId ->
                        viewModel.repository.deleteDrama(dramaId)
                    },
                    onManageEpisodes = { drama ->
                        selectedDramaForEpisodes = drama
                        activeTab = AdminTab.EPISODES
                    }
                )
            }
            AdminTab.EPISODES -> {
                AdminEpisodesTab(
                    dramas = dramas,
                    allEpisodesMap = allEpisodesMap,
                    selectedDrama = selectedDramaForEpisodes ?: dramas.firstOrNull(),
                    onSelectDrama = { selectedDramaForEpisodes = it },
                    onAddNewEpisode = { drama ->
                        isNewEpisodeMode = true
                        val epList = allEpisodesMap[drama.id] ?: emptyList()
                        val nextEpNum = epList.size + 1
                        showEpisodeDialog = Episode(
                            id = "ep_${drama.id}_$nextEpNum",
                            dramaId = drama.id,
                            episodeNumber = nextEpNum,
                            title = "Episódio $nextEpNum",
                            description = "Descrição do episódio $nextEpNum",
                            videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                            durationSeconds = 120,
                            thumbnailUrl = drama.coverVerticalUrl,
                            isPublished = true
                        )
                    },
                    onEditEpisode = { ep ->
                        isNewEpisodeMode = false
                        showEpisodeDialog = ep
                    },
                    onDeleteEpisode = { dramaId, epId ->
                        viewModel.repository.deleteEpisode(dramaId, epId)
                    }
                )
            }
            AdminTab.CATEGORIES -> {
                AdminCategoriesTab(
                    categories = categories,
                    onAddNew = {
                        isNewCategoryMode = true
                        showCategoryDialog = Category(
                            id = "cat_${System.currentTimeMillis()}",
                            name = "",
                            slug = "",
                            colorHex = "#EC00D8",
                            sortOrder = categories.size + 1
                        )
                    },
                    onEdit = { cat ->
                        isNewCategoryMode = false
                        showCategoryDialog = cat
                    },
                    onDelete = { catId ->
                        viewModel.repository.deleteCategory(catId)
                    }
                )
            }
            AdminTab.USERS -> {
                AdminUsersTab(
                    users = users,
                    onToggleBlock = { userId ->
                        viewModel.repository.toggleUserBlocked(userId)
                    },
                    onToggleAdmin = { userId, makeAdmin ->
                        viewModel.repository.setAdminRole(userId, makeAdmin)
                    }
                )
            }
            AdminTab.VPS_SETTINGS -> {
                AdminVpsSettingsTab(
                    settings = vpsSettings,
                    onSaveSettings = { updated ->
                        viewModel.repository.updateVpsSettings(updated)
                    }
                )
            }
        }
    }

    // Modal: Drama Add/Edit
    showDramaDialog?.let { currentEditing ->
        DramaFormDialog(
            initialDrama = currentEditing,
            categories = categories,
            isNew = isNewDramaMode,
            onDismiss = { showDramaDialog = null },
            onSave = { saved ->
                if (isNewDramaMode) {
                    viewModel.repository.addDrama(saved)
                } else {
                    viewModel.repository.updateDrama(saved)
                }
                showDramaDialog = null
            }
        )
    }

    // Modal: Episode Add/Edit
    showEpisodeDialog?.let { currentEditing ->
        EpisodeFormDialog(
            initialEpisode = currentEditing,
            isNew = isNewEpisodeMode,
            onDismiss = { showEpisodeDialog = null },
            onSave = { saved ->
                if (isNewEpisodeMode) {
                    viewModel.repository.addEpisode(saved)
                } else {
                    viewModel.repository.updateEpisode(saved)
                }
                showEpisodeDialog = null
            }
        )
    }

    // Modal: Category Add/Edit
    showCategoryDialog?.let { currentEditing ->
        CategoryFormDialog(
            initialCategory = currentEditing,
            isNew = isNewCategoryMode,
            onDismiss = { showCategoryDialog = null },
            onSave = { saved ->
                if (isNewCategoryMode) {
                    viewModel.repository.addCategory(saved)
                } else {
                    viewModel.repository.updateCategory(saved)
                }
                showCategoryDialog = null
            }
        )
    }
}

@Composable
private fun AdminDashboardTab(
    dramas: List<Drama>,
    allEpisodesMap: Map<String, List<Episode>>,
    categories: List<Category>,
    users: List<User>,
    onNavigateToTab: (AdminTab) -> Unit
) {
    val totalEpisodes = allEpisodesMap.values.sumOf { it.size }
    val totalViews = dramas.sumOf { it.viewsCount }
    val publishedCount = dramas.count { it.isPublished }
    val draftCount = dramas.count { !it.isPublished }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Visão Geral do Sistema",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
            Text(
                text = "Estatísticas em tempo real da plataforma Drama Box",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
            )
        }

        // Metrics Grid (2x3)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Mininovelas",
                    value = "${dramas.size}",
                    subtitle = "$publishedCount publicadas",
                    icon = Icons.Default.Movie,
                    accentColor = NeonPink,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Episódios",
                    value = "$totalEpisodes",
                    subtitle = "Hospedados na VPS",
                    icon = Icons.Default.VideoLibrary,
                    accentColor = ElectricPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminMetricCard(
                    title = "Usuários",
                    value = "${users.size}",
                    subtitle = "${users.count { it.role == UserRole.ADMIN }} admins",
                    icon = Icons.Default.People,
                    accentColor = AccentYellow,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Categorias",
                    value = "${categories.size}",
                    subtitle = "Gêneros ativos",
                    icon = Icons.Default.Category,
                    accentColor = Lavender,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val viewsFormatted = if (totalViews >= 1000000) "%.1fM".format(totalViews / 1000000f)
                else if (totalViews >= 1000) "${totalViews / 1000}K" else "$totalViews"

                AdminMetricCard(
                    title = "Visualizações",
                    value = viewsFormatted,
                    subtitle = "Reproduções totais",
                    icon = Icons.Default.Visibility,
                    accentColor = AccentOrange,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Rascunhos",
                    value = "$draftCount",
                    subtitle = "Em preparação",
                    icon = Icons.Default.EditNote,
                    accentColor = TextMuted,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Ações Rápidas",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onNavigateToTab(AdminTab.DRAMAS) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mininovelas", fontSize = 12.sp)
                }

                Button(
                    onClick = { onNavigateToTab(AdminTab.EPISODES) },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Episódios", fontSize = 12.sp)
                }

                Button(
                    onClick = { onNavigateToTab(AdminTab.VPS_SETTINGS) },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepSurfaceVariant),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("VPS", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun AdminMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DeepCard),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, DeepCardBorder, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black), color = TextWhite)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = accentColor))
        }
    }
}

@Composable
private fun AdminDramasTab(
    dramas: List<Drama>,
    onAddNew: () -> Unit,
    onEdit: (Drama) -> Unit,
    onDelete: (String) -> Unit,
    onManageEpisodes: (Drama) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gerenciar Mininovelas (${dramas.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
                Text(
                    text = "Adicione títulos, banners, sinopses e status",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }

            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nova", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(dramas) { drama ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepCard),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DeepCardBorder, RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = drama.title,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TextWhite,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(if (drama.isPublished) ElectricPurple else DeepSurfaceVariant)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (drama.isPublished) "Publicado" else "Rascunho",
                                            color = if (drama.isPublished) TextWhite else TextMuted,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "${drama.categoryName} • ${drama.totalEpisodes} Episódios • ${drama.viewsCount} visualizações",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Lavender)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { onEdit(drama) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextWhite, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDelete(drama.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action: Gerenciar Episódios
                        OutlinedButton(
                            onClick = { onManageEpisodes(drama) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPink),
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = Brush.horizontalGradient(listOf(NeonPink, ElectricPurple))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp)
                        ) {
                            Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gerenciar Episódios da Série", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminEpisodesTab(
    dramas: List<Drama>,
    allEpisodesMap: Map<String, List<Episode>>,
    selectedDrama: Drama?,
    onSelectDrama: (Drama) -> Unit,
    onAddNewEpisode: (Drama) -> Unit,
    onEditEpisode: (Episode) -> Unit,
    onDeleteEpisode: (String, String) -> Unit
) {
    val currentDrama = selectedDrama ?: dramas.firstOrNull()
    val episodes = currentDrama?.let { allEpisodesMap[it.id] } ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Drama Selector Dropdown / Chips
        Text(
            text = "Selecione a Mininovela:",
            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(dramas) { d ->
                val isSelected = d.id == currentDrama?.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ElectricPurple else DeepCard)
                        .border(1.dp, if (isSelected) NeonPink else DeepCardBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectDrama(d) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = d.title,
                        color = if (isSelected) TextWhite else TextMuted,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (currentDrama != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Episódios de \"${currentDrama.title}\"",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${episodes.size} episódios cadastrados com links VPS",
                        style = MaterialTheme.typography.labelSmall.copy(color = Lavender)
                    )
                }

                Button(
                    onClick = { onAddNewEpisode(currentDrama) },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Episódio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(episodes) { ep ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepCard),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DeepCardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "EP ${ep.episodeNumber}: ${ep.title}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhite
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "URL: ${ep.videoUrl}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = TextSubtle,
                                        fontSize = 10.sp
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Duração: ${ep.durationSeconds}s • ${if (ep.isPublished) "Publicado" else "Rascunho"}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Lavender, fontSize = 10.sp)
                                )
                            }

                            Row {
                                IconButton(onClick = { onEditEpisode(ep) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextWhite, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDeleteEpisode(currentDrama.id, ep.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminCategoriesTab(
    categories: List<Category>,
    onAddNew: () -> Unit,
    onEdit: (Category) -> Unit,
    onDelete: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Categorias (${categories.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextWhite
                )
                Text(
                    text = "Gêneros e temas do catálogo",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                )
            }

            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Nova", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(categories) { cat ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepCard),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DeepCardBorder, RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(NeonPink)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextWhite
                                )
                                Text(
                                    text = "Slug: ${cat.slug} • Ordem: ${cat.sortOrder}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSubtle)
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = { onEdit(cat) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = TextWhite, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDelete(cat.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    users: List<User>,
    onToggleBlock: (String) -> Unit,
    onToggleAdmin: (String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Gerenciar Usuários (${users.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TextWhite
        )
        Text(
            text = "Visualize contas, status e permissões administrativas",
            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(users) { user ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepCard),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, DeepCardBorder, RoundedCornerShape(10.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextWhite
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    if (user.role == UserRole.ADMIN) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(ElectricPurple)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ADMIN", color = TextWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                                )

                                val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(user.createdAt))
                                Text(
                                    text = "Cadastro: $dateStr • Status: ${if (user.isBlocked) "Bloqueado" else "Ativo"}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (user.isBlocked) Color(0xFFFF5252) else Lavender,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilledTonalButton(
                                    onClick = { onToggleBlock(user.id) },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = if (user.isBlocked) Color(0xFF2E7D32) else Color(0xFFB71C1C),
                                        contentColor = TextWhite
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(if (user.isBlocked) "Desbloquear" else "Bloquear", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminVpsSettingsTab(
    settings: AppVpsSettings,
    onSaveSettings: (AppVpsSettings) -> Unit
) {
    var vpsUrl by remember { mutableStateOf(settings.vpsBaseUrl) }
    var cdnEnabled by remember { mutableStateOf(settings.cdnEnabled) }
    var hlsEnabled by remember { mutableStateOf(settings.defaultHlsEnabled) }
    var testResult by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Integração VPS & Servidor de Vídeos",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
            Text(
                text = "Configure a infraestrutura onde seus arquivos MP4 e HLS (.m3u8) estão hospedados.",
                style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                lineHeight = 16.sp
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepCard),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DeepCardBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Endereço Base da VPS",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Lavender
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = vpsUrl,
                        onValueChange = { vpsUrl = it },
                        label = { Text("URL Base da VPS (ex: https://meuservidor.com/videos/)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = DeepCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Modo HLS Padrão (.m3u8)", style = MaterialTheme.typography.bodyMedium, color = TextWhite)
                            Text("Reprodução adaptativa de alta eficiência", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Switch(
                            checked = hlsEnabled,
                            onCheckedChange = { hlsEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = NeonPink)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Aceleração CDN", style = MaterialTheme.typography.bodyMedium, color = TextWhite)
                            Text("Distribuição global de baixa latência", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                        }
                        Switch(
                            checked = cdnEnabled,
                            onCheckedChange = { cdnEnabled = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = ElectricPurple)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            onSaveSettings(
                                settings.copy(
                                    vpsBaseUrl = vpsUrl,
                                    cdnEnabled = cdnEnabled,
                                    defaultHlsEnabled = hlsEnabled
                                )
                            )
                            testResult = "Configurações da VPS salvas com sucesso no banco de dados!"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Salvar Configurações da VPS", fontWeight = FontWeight.Bold, color = TextWhite)
                    }

                    if (testResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = testResult!!,
                            style = MaterialTheme.typography.bodySmall.copy(color = AccentYellow)
                        )
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepSurfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Instruções Nginx / HLS para sua VPS:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = AccentYellow
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "1. Mantenha os arquivos .m3u8 e .ts em diretórios públicos acessíveis por HTTPS.\n2. Configure o cabeçalho 'Access-Control-Allow-Origin: *' no bloco de configuração do Nginx.\n3. O Drama Box consumirá os fluxos com o player nativo ExoPlayer HLS.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

// Dialog: Add/Edit Drama
@Composable
private fun DramaFormDialog(
    initialDrama: Drama,
    categories: List<Category>,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Drama) -> Unit
) {
    var title by remember { mutableStateOf(initialDrama.title) }
    var description by remember { mutableStateOf(initialDrama.description) }
    var coverUrl by remember { mutableStateOf(initialDrama.coverVerticalUrl) }
    var selectedCatId by remember { mutableStateOf(initialDrama.categoryId) }
    var isPublished by remember { mutableStateOf(initialDrama.isPublished) }
    var isFeatured by remember { mutableStateOf(initialDrama.isFeatured) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSurface,
        title = {
            Text(
                text = if (isNew) "Adicionar Nova Mininovela" else "Editar Mininovela",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título da Mininovela") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = DeepCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Sinopse / Descrição") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = DeepCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = coverUrl,
                        onValueChange = { coverUrl = it },
                        label = { Text("URL da Capa Vertical (2:3)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = DeepCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Categoria:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(categories) { c ->
                            val isSel = c.id == selectedCatId
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCatId = c.id },
                                label = { Text(c.name, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = ElectricPurple,
                                    selectedLabelColor = TextWhite
                                )
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Publicar Imediatamente", color = TextWhite, fontSize = 13.sp)
                        Switch(
                            checked = isPublished,
                            onCheckedChange = { isPublished = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = NeonPink)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Destacar na Página Inicial", color = TextWhite, fontSize = 13.sp)
                        Switch(
                            checked = isFeatured,
                            onCheckedChange = { isFeatured = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = AccentYellow)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val cat = categories.find { it.id == selectedCatId }
                        onSave(
                            initialDrama.copy(
                                title = title,
                                description = description,
                                coverVerticalUrl = coverUrl,
                                categoryId = selectedCatId,
                                categoryName = cat?.name ?: "Geral",
                                isPublished = isPublished,
                                isFeatured = isFeatured
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) {
                Text("Salvar", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}

// Dialog: Add/Edit Episode
@Composable
private fun EpisodeFormDialog(
    initialEpisode: Episode,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Episode) -> Unit
) {
    var title by remember { mutableStateOf(initialEpisode.title) }
    var episodeNumberStr by remember { mutableStateOf(initialEpisode.episodeNumber.toString()) }
    var videoUrl by remember { mutableStateOf(initialEpisode.videoUrl) }
    var durationStr by remember { mutableStateOf(initialEpisode.durationSeconds.toString()) }
    var isPublished by remember { mutableStateOf(initialEpisode.isPublished) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSurface,
        title = {
            Text(
                text = if (isNew) "Adicionar Episódio da VPS" else "Editar Episódio",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título do Episódio") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = DeepCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = episodeNumberStr,
                        onValueChange = { episodeNumberStr = it },
                        label = { Text("Número do Ep") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = DeepCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { durationStr = it },
                        label = { Text("Duração (segundos)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = DeepCardBorder,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("URL do Vídeo (VPS HLS .m3u8 ou MP4)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = DeepCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Publicar Episódio", color = TextWhite, fontSize = 13.sp)
                    Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = TextWhite, checkedTrackColor = NeonPink)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && videoUrl.isNotBlank()) {
                        val num = episodeNumberStr.toIntOrNull() ?: 1
                        val dur = durationStr.toIntOrNull() ?: 120
                        onSave(
                            initialEpisode.copy(
                                title = title,
                                episodeNumber = num,
                                videoUrl = videoUrl,
                                durationSeconds = dur,
                                isPublished = isPublished
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) {
                Text("Salvar Episódio", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}

// Dialog: Add/Edit Category
@Composable
private fun CategoryFormDialog(
    initialCategory: Category,
    isNew: Boolean,
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf(initialCategory.name) }
    var slug by remember { mutableStateOf(initialCategory.slug) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepSurface,
        title = {
            Text(
                text = if (isNew) "Nova Categoria" else "Editar Categoria",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (isNew) slug = it.lowercase().replace(" ", "-")
                    },
                    label = { Text("Nome da Categoria") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = DeepCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = slug,
                    onValueChange = { slug = it },
                    label = { Text("Slug identificador") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = DeepCardBorder,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(initialCategory.copy(name = name, slug = slug))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) {
                Text("Salvar", color = TextWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextMuted)
            }
        }
    )
}
