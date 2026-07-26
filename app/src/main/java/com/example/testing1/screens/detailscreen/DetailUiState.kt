package com.example.testing1.screens.detailscreen

import com.example.testing1.data.local.coffee.CoffeeEntity

data class DetailUiState(
    val coffeeItem: CoffeeEntity? = null,
    val selectedSize: String = "M",
    val isLoading: Boolean = true
)
