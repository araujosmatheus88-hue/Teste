package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DramaCard
import com.example.ui.theme.*
import com.example.viewmodel.DramaBoxViewModel
import com.example.viewmodel.ExploreSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: DramaBoxViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCatId by viewModel.selectedCategoryId.collectAsState()
    val currentSort by viewModel.exploreSort.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val filteredDramas by viewModel.filteredDramas.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(top = 8.dp)
            .testTag("explore_screen")
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = {
                Text(
                    text = "Buscar por nome, gênero, ator...",
                    color = TextSubtle,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = NeonPink
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar busca",
                            tint = TextMuted
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DeepSurfaceVariant,
                unfocusedContainerColor = DeepSurfaceVariant,
                focusedBorderColor = NeonPink,
                unfocusedBorderColor = DeepCardBorder,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = NeonPink
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("search_text_field")
        )

        // Sort Options Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = null,
                tint = Lavender,
                modifier = Modifier.size(18.dp)
            )

            Text(
                text = "Ordenar:",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = TextMuted,
                    fontWeight = FontWeight.Bold
                )
            )

            val sorts = listOf(
                "Populares" to ExploreSort.POPULAR,
                "Recentes" to ExploreSort.RECENT,
                "Destaques" to ExploreSort.FEATURED,
                "Mais Vistos" to ExploreSort.VIEWS
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(sorts) { (label, sort) ->
                    val isSelected = currentSort == sort
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) ElectricPurple else DeepCard)
                            .border(
                                1.dp,
                                if (isSelected) NeonPink else DeepCardBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.setSort(sort) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) TextWhite else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCatId == null,
                    onClick = { viewModel.selectCategory(null) },
                    label = { Text("Todas Categorias", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = DeepSurfaceVariant,
                        labelColor = TextMuted,
                        selectedContainerColor = NeonPink,
                        selectedLabelColor = TextWhite
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            items(categories) { cat ->
                val isSelected = selectedCatId == cat.id
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.selectCategory(cat.id) },
                    label = { Text(cat.name, fontSize = 12.sp) },
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
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        // Grid of Drama Cards
        if (filteredDramas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Nenhuma mininovela encontrada",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextWhite)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Tente buscar com outros termos ou selecione outra categoria.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 105.dp),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(filteredDramas) { drama ->
                    DramaCard(
                        drama = drama,
                        onClick = { viewModel.openDramaDetail(drama.id) },
                        cardWidth = 105
                    )
                }
            }
        }
    }
}
