package com.example.testing1.screens.homescreen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.repository.CoffeeRepository
import com.example.testing1.models.CoffeeCategory
import com.example.testing1.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.combine

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CoffeeRepository
) : ViewModel() {

    private var allCoffee = emptyList<CoffeeEntity>()
    private var hasSyncedAtLeastOnce = false
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
            Log.d("HomeViewModel", "Starting coffee data collection...")
            combine(
                repository.getAllCoffee(),
                repository.getFavoriteCoffees()
            ) { coffees, favorites ->
                val favIds = favorites.map { it.id }.toSet()
                coffees.map { coffee ->
                    coffee.copy(isFavorite = favIds.contains(coffee.id))
                }
            }.collect { coffees ->
                Log.d("HomeViewModel", "Received ${coffees.size} coffee items with updated favorite state")
                allCoffee = coffees
                updateUi()
            }
        }
        viewModelScope.launch {
            repository.syncStatus.collect { status ->
                Log.d("HomeViewModel", "Sync Status Changed: Connected=${status.connected}, InitialSynced=${status.hasSynced}")
                hasSyncedAtLeastOnce = status.hasSynced ?: false
                updateUi()
            }
        }
        viewModelScope.launch {
            repository.getRecentSearches().collect { searches ->
                _uiState.value = _uiState.value.copy(recentSearches = searches)
            }
        }
        viewModelScope.launch {
            repository.getAddresses().collect { addressList ->
                val currentSelected = _uiState.value.selectedAddress
                val defaultOrFirst = currentSelected ?: addressList.find { it.isDefault } ?: addressList.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    addresses = addressList,
                    selectedAddress = defaultOrFirst
                )
            }
        }
    }

    fun onAddressSelected(address: AddressEntity) {
        _uiState.value = _uiState.value.copy(selectedAddress = address)
        viewModelScope.launch {
            repository.setAsDefaultAddress(address.addressId)
        }
    }

    fun saveAddressFromMap(tag: String, fullAddress: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val newAddress = AddressEntity(
                addressId = UUID.randomUUID().toString(),
                userId = repository.currentUserId,
                tag = tag,
                fullAddress = fullAddress,
                isDefault = true,
                latitude = lat,
                longitude = lng
            )
            repository.addAddress(newAddress)
            _uiState.value = _uiState.value.copy(selectedAddress = newAddress)
            _uiEvent.send(UiEvent.ShowSnackbar("Address '$tag' saved successfully 📍"))
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

    private fun updateUi() {
        val currentState = _uiState.value
        val filteredItems = allCoffee.filter { item ->
            val matchesSearch =
                item.name.contains(currentState.searchText, ignoreCase = true)
            val matchesCategory =
                currentState.selectedCategory == CoffeeCategory.AllCoffee ||
                        item.category == currentState.selectedCategory

            matchesSearch && matchesCategory
        }
        
        Log.d("HomeViewModel", "updateUi() - Total: ${allCoffee.size}, Filtered: ${filteredItems.size}, Category: ${currentState.selectedCategory}, isLoading: ${allCoffee.isEmpty() && !hasSyncedAtLeastOnce}")
        if (allCoffee.isNotEmpty() && filteredItems.isEmpty()) {
            allCoffee.forEach { 
                Log.d("HomeViewModel", "Filtering Mismatch - Item: ${it.name}, Category: ${it.category}")
            }
        }
        
        _uiState.value = _uiState.value.copy(
            coffeeItems = filteredItems,
            isLoading = allCoffee.isEmpty() && !hasSyncedAtLeastOnce
        )
    }

    private fun filterCoffee() {
        updateUi()
    }
}
