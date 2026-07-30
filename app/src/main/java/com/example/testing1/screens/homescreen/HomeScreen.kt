package com.example.testing1.screens.homescreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
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
        onRecentSearchClick = viewModel::onRecentSearchClick,
        onAddressSelected = viewModel::onAddressSelected,
        onSaveMapAddress = viewModel::saveAddressFromMap
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
    onRecentSearchClick: (String) -> Unit,
    onAddressSelected: (com.example.testing1.data.local.address.AddressEntity) -> Unit = {},
    onSaveMapAddress: (tag: String, fullAddress: String, lat: Double, lng: Double) -> Unit = { _, _, _, _ -> }
) {
    val haptic = LocalHapticFeedback.current
    var showAddressSheet by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }

    val displayLocationText = uiState.selectedAddress?.let {
        "${it.tag} • ${it.fullAddress}"
    } ?: stringResource(R.string.default_location)

    if (showAddressSheet) {
        com.example.testing1.screens.address.AddressSelectorBottomSheet(
            addresses = uiState.addresses,
            selectedAddressId = uiState.selectedAddress?.addressId,
            onAddressSelected = onAddressSelected,
            onOpenMapPicker = {
                showAddressSheet = false
                showMapPicker = true
            },
            onDismiss = { showAddressSheet = false }
        )
    }

    if (showMapPicker) {
        com.example.testing1.screens.address.MapLocationPickerModal(
            initialLatitude = uiState.selectedAddress?.latitude,
            initialLongitude = uiState.selectedAddress?.longitude,
            onDismiss = { showMapPicker = false },
            onAddressSaved = { tag, fullAddress, lat, lng ->
                showMapPicker = false
                onSaveMapAddress(tag, fullAddress, lat, lng)
            }
        )
    }

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
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
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
                                searchText = uiState.searchText,
                                onSearchTextChange = onSearchTextChange,
                                onClearSearch = onClearSearch,
                                onSearchClick = onSearchClick,
                                onSearchFocusChange = onSearchFocusChange,
                                selectedLocationText = displayLocationText,
                                userPoints = 500,
                                cartItemCount = 0,
                                onLocationClick = { showAddressSheet = true },
                                onCartClick = onCartClick
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
                }

                item {
                    if (!uiState.isSearchFocused) {
                        Spacer(modifier = Modifier.height(12.dp))
                        com.example.testing1.screens.homescreen.components.RewardsPunchCardBanner(
                            purchasedCount = 3,
                            targetCount = 6
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    CategoryRow(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = onCategorySelected
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.isLoading) {
                    item {
                        CoffeeGrid(
                            items = emptyList(),
                            isLoading = true,
                            onItemClick = {},
                            onToggleFavorite = {}
                        )
                    }
                } else if (uiState.coffeeItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No coffee found for this category",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    val chunkedItems = uiState.coffeeItems.chunked(2)
                    items(chunkedItems) { rowItems ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            rowItems.forEach { item ->
                                com.example.testing1.screens.homescreen.components.CoffeeCard(
                                    item = item,
                                    modifier = Modifier.weight(1f),
                                    onClick = { onItemClick(item) },
                                    onToggleFavorite = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onToggleFavorite(item)
                                    }
                                )
                            }
                            if (rowItems.size < 2) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
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
