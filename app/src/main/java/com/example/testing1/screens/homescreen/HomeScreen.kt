package com.example.testing1.screens.homescreen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.testing1.R
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.models.CoffeeCategory
import com.example.testing1.screens.address.AddressSelectorBottomSheet
import com.example.testing1.screens.address.MapLocationPickerModal
import com.example.testing1.screens.homescreen.components.*
import com.example.testing1.screens.ui_components.EmptyStateContent
import com.example.testing1.screens.ui_components.MyBottomBar
import com.example.testing1.util.UiEvent

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
        onItemClick = onItemClick,
        onCartClick = onCartClick,
        onFavoriteClick = onFavoriteClick,
        onProfileClick = onProfileClick,
        onSearchTextChange = viewModel::onSearchTextChange,
        onClearSearch = viewModel::onClearSearch,
        onSearchClick = viewModel::onSearchClick,
        onSearchFocusChange = viewModel::onSearchFocusChange,
        onRecentSearchClick = viewModel::onRecentSearchClick,
        onCategorySelected = viewModel::onCategorySelected,
        onToggleFavorite = viewModel::toggleFavorite,
        onRefresh = { viewModel.onRefresh() },
        onSelectAddress = viewModel::onAddressSelected,
        onSaveMapAddress = viewModel::saveAddressFromMap
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onItemClick: (CoffeeEntity) -> Unit,
    onCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchTextChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onCategorySelected: (CoffeeCategory) -> Unit,
    onToggleFavorite: (CoffeeEntity) -> Unit,
    onRefresh: () -> Unit,
    onSelectAddress: (com.example.testing1.data.local.address.AddressEntity) -> Unit,
    onSaveMapAddress: (tag: String, fullAddress: String, lat: Double, lng: Double) -> Unit
) {
    var showAddressSheet by remember { mutableStateOf(false) }
    var showMapPicker by remember { mutableStateOf(false) }
    var selectedTemperatureFilter by remember { mutableStateOf<String?>(null) }

    val displayLocationText = uiState.selectedAddress?.let { "${it.tag}: ${it.fullAddress}" }
        ?: stringResource(R.string.default_location)

    if (showAddressSheet) {
        AddressSelectorBottomSheet(
            addresses = uiState.addresses,
            selectedAddressId = uiState.selectedAddress?.addressId,
            onAddressSelected = { address ->
                showAddressSheet = false
                onSelectAddress(address)
            },
            onOpenMapPicker = {
                showAddressSheet = false
                showMapPicker = true
            },
            onDismiss = { showAddressSheet = false }
        )
    }

    if (showMapPicker) {
        MapLocationPickerModal(
            initialLatitude = uiState.selectedAddress?.latitude,
            initialLongitude = uiState.selectedAddress?.longitude,
            initialTag = uiState.selectedAddress?.tag ?: "Home",
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
                // 1. Top Header & Promo Banner Container
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF1F120B),
                                            Color(0xFF2C1A14)
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
                                onLocationClick = { showAddressSheet = true }
                            )
                        }

                        if (!uiState.isSearchFocused) {
                            Banner(modifier = Modifier.align(Alignment.BottomCenter))
                        }
                    }
                }

                // 2. Burger King Style "OUR MENU" Category Section
                item {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "OUR MENU",
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    CategoryRow(
                        categories = uiState.categories,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = onCategorySelected
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. Burger King Style Hot / Cold Preference Filters (Image 2)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedTemperatureFilter == "HOT",
                            onClick = {
                                selectedTemperatureFilter = if (selectedTemperatureFilter == "HOT") null else "HOT"
                            },
                            label = { Text("☕ HOT", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE65100),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = selectedTemperatureFilter == "COLD",
                            onClick = {
                                selectedTemperatureFilter = if (selectedTemperatureFilter == "COLD") null else "COLD"
                            },
                            label = { Text("🧊 COLD", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0288D1),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = selectedTemperatureFilter == "VEGAN",
                            onClick = {
                                selectedTemperatureFilter = if (selectedTemperatureFilter == "VEGAN") null else "VEGAN"
                            },
                            label = { Text("🌿 VEGAN", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF388E3C),
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                // 4. Coffee Grid Section
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
                                .padding(vertical = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptyStateContent(
                                icon = Icons.Default.SearchOff,
                                title = "No coffee found",
                                description = "Try searching for a different coffee name or filter."
                            )
                        }
                    }
                } else {
                    item {
                        CoffeeGrid(
                            items = uiState.coffeeItems,
                            isLoading = false,
                            onItemClick = onItemClick,
                            onToggleFavorite = onToggleFavorite
                        )
                    }
                }

                // 5. Burger King Style "COFFEE DEALS OF THE DAY" Promo Section (Image 1 & Image 3)
                item {
                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = "COFFEE DEALS OF THE DAY",
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFF3E0)),
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(110.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Free Delivery",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color(0xFFE65100)
                                        )
                                        Text(
                                            text = "On all orders above $15",
                                            fontSize = 11.sp,
                                            color = Color(0xFF5D4037)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFE65100)
                                        ) {
                                            Text(
                                                text = "CODE: FREEFEES",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = Color(0xFFE65100),
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }

                        item {
                            ElevatedCard(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFEFEBE9)),
                                modifier = Modifier
                                    .width(280.dp)
                                    .height(110.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Buy 1 Get 1 Free",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF2C1A14)
                                        )
                                        Text(
                                            text = "Applicable on Cappuccinos",
                                            fontSize = 11.sp,
                                            color = Color(0xFF6D4C41)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF2C1A14)
                                        ) {
                                            Text(
                                                text = "CODE: BOGO",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.LocalOffer,
                                        contentDescription = null,
                                        tint = Color(0xFF2C1A14),
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
