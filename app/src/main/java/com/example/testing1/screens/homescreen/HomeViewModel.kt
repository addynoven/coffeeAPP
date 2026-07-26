package com.example.testing1.screens.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.repository.CoffeeRepository
import com.example.testing1.models.CoffeeCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CoffeeRepository
) : ViewModel() {

    private var allCoffee = emptyList<CoffeeEntity>()
    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState>
        get() = _uiState

    init {
        loadData()
        refreshData()
    }

    private fun loadData() {
        viewModelScope.launch {
            repository.getAllCoffee().collect { coffees ->
                allCoffee = coffees
                filterCoffee()
            }
        }
        viewModelScope.launch {
            repository.getRecentSearches().collect { searches ->
                _uiState.value = _uiState.value.copy(recentSearches = searches)
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            repository.refreshCoffee()
        }
    }

    fun onCategorySelected(category: CoffeeCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterCoffee()
    }

    fun onSearchTextChange(text: String) {
        _uiState.value = _uiState.value.copy(searchText = text)
        filterCoffee()
    }

    fun onSearchFocusChange(isFocused: Boolean) {
        _uiState.value = _uiState.value.copy(isSearchFocused = isFocused)
    }

    fun onSearchClick() {
        val query = _uiState.value.searchText
        if (query.isNotBlank()) {
            viewModelScope.launch {
                repository.saveSearch(query, _uiState.value.coffeeItems.size)
            }
        }
        _uiState.value = _uiState.value.copy(isSearchFocused = false)
        filterCoffee()
    }

    fun onRecentSearchClick(query: String) {
        _uiState.value = _uiState.value.copy(searchText = query, isSearchFocused = false)
        filterCoffee()
    }

    fun toggleFavorite(coffee: CoffeeEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(coffee.id, !coffee.isFavorite)
        }
    }

    private fun filterCoffee() {
        val currentState = _uiState.value
        val filteredItems = allCoffee.filter { item ->
            val matchesSearch =
                item.name.contains(currentState.searchText, ignoreCase = true)
            val matchesCategory =
                currentState.selectedCategory == CoffeeCategory.AllCoffee ||
                        item.category == currentState.selectedCategory

            matchesSearch && matchesCategory
        }
        _uiState.value = _uiState.value.copy(
            coffeeItems = filteredItems
        )
    }
}
