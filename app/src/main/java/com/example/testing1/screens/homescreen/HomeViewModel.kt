package com.example.testing1.screens.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.repository.CoffeeRepository
import com.example.testing1.models.CoffeeCategory
import com.example.testing1.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    init {
        loadData()
        // Silent refresh on startup (background only)
        onRefresh(showUI = false)
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

    fun onRefresh(showUI: Boolean = true) {
        viewModelScope.launch {
            if (showUI) _uiState.value = _uiState.value.copy(isRefreshing = true)
            repository.refreshCoffee()
            if (showUI) _uiState.value = _uiState.value.copy(isRefreshing = false)
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

    fun onClearSearch() {
        _uiState.value = _uiState.value.copy(searchText = "")
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
            val newStatus = !coffee.isFavorite
            repository.toggleFavorite(coffee.id, newStatus)
            val message = if (newStatus) {
                "${coffee.name} added to favorites ❤️"
            } else {
                "${coffee.name} removed from favorites"
            }
            _uiEvent.send(UiEvent.ShowSnackbar(message))
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
            coffeeItems = filteredItems,
            isLoading = false
        )
    }
}
