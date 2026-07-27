package com.example.testing1.screens.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.testing1.util.UiEvent

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        uiState = HomeUiState(),
        onSearchTextChange = {},
        onClearSearch = {},
        onSearchClick = {},
        onCategorySelected = {},
        onToggleFavorite = {},
        onRefresh = {},
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    HomeScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onSearchTextChange = viewModel::onSearchTextChange,
        onClearSearch = viewModel::onClearSearch,
        onSearchClick = viewModel::onSearchClick,
        onCategorySelected = viewModel::onCategorySelected,
        onToggleFavorite = viewModel::toggleFavorite,
        onRefresh = viewModel::onRefresh,
        onItemClick = onItemClick,
        onCartClick = onCartClick,
        onFavoriteClick = onFavoriteClick,
        onProfileClick = onProfileClick,
        onSearchFocusChange = viewModel::onSearchFocusChange,
        onRecentSearchClick = viewModel::onRecentSearchClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onSearchTextChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSearchClick: () -> Unit,
    onCategorySelected: (com.example.testing1.models.CoffeeCategory) -> Unit,
    onToggleFavorite: (CoffeeEntity) -> Unit,
    onRefresh: () -> Unit,
    onItemClick: (CoffeeEntity) -> Unit,
    onCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    onRecentSearchClick: (String) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            MyBottomBar(
                selectedTabId = "home",
                onTabSelected = { tabId ->
                    when (tabId) {
                        "cart" -> onCartClick()
                        "favorite" -> onFavoriteClick()
                        "profile" -> onProfileClick()
                    }
                }
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                            onClearSearch,
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
                    isLoading = uiState.isLoading,
                    onItemClick = onItemClick,
                    onToggleFavorite = { coffee ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite(coffee)
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
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
