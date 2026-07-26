package com.example.testing1.screens.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.testing1.R
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.local.search.SearchHistoryEntity
import com.example.testing1.screens.homescreen.components.Banner
import com.example.testing1.screens.homescreen.components.CategoryRow
import com.example.testing1.screens.homescreen.components.CoffeeGrid
import com.example.testing1.screens.homescreen.components.HeaderSection
import com.example.testing1.screens.ui_components.MyBottomBar

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        uiState = HomeUiState(),
        onSearchTextChange = {},
        onSearchClick = {},
        onCategorySelected = {},
        onToggleFavorite = {},
        onItemClick = {},
        onCartClick = {},
        onFavoriteClick = {},
        onProfileClick = {},
        onSearchFocusChange = {},
        onRecentSearchClick = {}
    )
}

@Composable
fun HomeRoute(
    onItemClick: (CoffeeEntity) -> Unit,
    onCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreen(
        uiState = uiState,
        onSearchTextChange = viewModel::onSearchTextChange,
        onSearchClick = viewModel::onSearchClick,
        onCategorySelected = viewModel::onCategorySelected,
        onToggleFavorite = viewModel::toggleFavorite,
        onItemClick = onItemClick,
        onCartClick = onCartClick,
        onFavoriteClick = onFavoriteClick,
        onProfileClick = onProfileClick,
        onSearchFocusChange = viewModel::onSearchFocusChange,
        onRecentSearchClick = viewModel::onRecentSearchClick
    )
}

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearchTextChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    onCategorySelected: (com.example.testing1.models.CoffeeCategory) -> Unit,
    onToggleFavorite: (CoffeeEntity) -> Unit,
    onItemClick: (CoffeeEntity) -> Unit,
    onCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    onRecentSearchClick: (String) -> Unit
) {
    Scaffold(
        bottomBar = {
            MyBottomBar(
                selectedTab = "Home",
                onTabSelected = { tab ->
                    if (tab == "Cart") onCartClick()
                    if (tab == "Favorite") onFavoriteClick()
                    if (tab == "Profile") onProfileClick()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF131313),
                                    Color(0xFF313131)
                                )
                            )
                        )
                )

                Column {
                    HeaderSection(
                        uiState.searchText,
                        onSearchTextChange,
                        onSearchClick,
                        onSearchFocusChange
                    )
                    
                    if (uiState.isSearchFocused && uiState.recentSearches.isNotEmpty()) {
                        RecentSearchesSection(
                            searches = uiState.recentSearches,
                            onSearchClick = onRecentSearchClick
                        )
                    }
                }

                if (!uiState.isSearchFocused) {
                    Banner(modifier = Modifier.align(Alignment.BottomCenter))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            CategoryRow(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = onCategorySelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            CoffeeGrid(
                items = uiState.coffeeItems,
                onItemClick = onItemClick,
                onToggleFavorite = onToggleFavorite
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RecentSearchesSection(
    searches: List<SearchHistoryEntity>,
    onSearchClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A)),
        shape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = stringResource(R.string.recent_searches_title),
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            searches.forEach { search ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSearchClick(search.query) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = search.query, color = Color.White, fontSize = 14.sp)
                    }
                    Text(
                        text = stringResource(R.string.search_results_count, search.resultCount),
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
