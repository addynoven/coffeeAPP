package com.example.testing1.screens.homescreen

import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.local.search.SearchHistoryEntity
import com.example.testing1.models.CoffeeCategory

import com.example.testing1.data.local.address.AddressEntity

data class HomeUiState(
    val categories: List<CoffeeCategory> = CoffeeCategory.entries,
    val selectedCategory: CoffeeCategory = CoffeeCategory.AllCoffee,
    val coffeeItems: List<CoffeeEntity> = emptyList(),
    val recentSearches: List<SearchHistoryEntity> = emptyList(),
    val addresses: List<AddressEntity> = emptyList(),
    val selectedAddress: AddressEntity? = null,
    val searchText: String = "",
    val isSearchFocused: Boolean = false,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)
