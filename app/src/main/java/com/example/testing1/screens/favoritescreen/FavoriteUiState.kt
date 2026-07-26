package com.example.testing1.screens.favoritescreen

import com.example.testing1.data.local.coffee.CoffeeEntity

data class FavoriteUiState(
    val favoriteItems: List<CoffeeEntity> = emptyList(),
    val isLoading: Boolean = true
)
