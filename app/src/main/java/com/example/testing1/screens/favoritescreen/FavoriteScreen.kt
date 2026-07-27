package com.example.testing1.screens.favoritescreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.screens.favoritescreen.components.FavoriteItemCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import com.example.testing1.screens.ui_components.EmptyStateContent
import com.example.testing1.screens.ui_components.MyBottomBar

@Preview(showBackground = true)
@Composable
fun FavoriteScreenPreview() {
    FavoriteScreen(
        uiState = FavoriteUiState(
            favoriteItems = listOf(
                CoffeeEntity(
                    id = 1,
                    name = "Espresso",
                    description = "Strong",
                    category = com.example.testing1.models.CoffeeCategory.Espresso,
                    price = 3.5,
                    imageUrl = ""
                )
            ),
            isLoading = false
        ),
        onHomeClick = {},
        onCartClick = {},
        onFavoriteClick = {},
        onProfileClick = {},
        onRemoveFavorite = {}
    )
}

@Composable
fun FavoriteRoute(
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    FavoriteScreen(
        uiState = uiState,
        onHomeClick = onHomeClick,
        onCartClick = onCartClick,
        onFavoriteClick = onFavoriteClick,
        onProfileClick = onProfileClick,
        onRemoveFavorite = viewModel::removeFavorite
    )
}

@Composable
fun FavoriteScreen(
    uiState: FavoriteUiState,
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRemoveFavorite: (CoffeeEntity) -> Unit
) {
    Scaffold(
        bottomBar = {
            MyBottomBar(
                selectedTabId = "favorite",
                onTabSelected = { tabId ->
                    when (tabId) {
                        "home" -> onHomeClick()
                        "cart" -> onCartClick()
                        "profile" -> onProfileClick()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            if (!uiState.isLoading && uiState.favoriteItems.isEmpty()) {
                EmptyStateContent(
                    icon = Icons.Default.FavoriteBorder,
                    title = "Your wishlist is empty",
                    description = "You haven't saved any coffee yet. Tap the heart icon to save your favorites!",
                    actionLabel = "Discover Coffee",
                    onActionClick = onHomeClick
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Wishlist",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.favoriteItems) { item ->
                        FavoriteItemCard(
                            item = item,
                            onRemoveClick = { onRemoveFavorite(item) }
                        )
                    }
                }
            }
        }
    }
}
