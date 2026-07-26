package com.example.testing1.screens.favoritescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.repository.CoffeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val repository: CoffeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoriteUiState())
    val uiState: StateFlow<FavoriteUiState> = _uiState

    init {
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            repository.getFavoriteCoffee().collect { items ->
                _uiState.value = _uiState.value.copy(
                    favoriteItems = items,
                    isLoading = false
                )
            }
        }
    }

    fun removeFavorite(coffee: CoffeeEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(coffee.id, false)
        }
    }
}
