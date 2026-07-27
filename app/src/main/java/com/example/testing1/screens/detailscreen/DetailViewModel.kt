package com.example.testing1.screens.detailscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.repository.CoffeeRepository
import com.example.testing1.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: CoffeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun loadCoffee(coffeeId: Int) {
        viewModelScope.launch {
            val item = repository.getCoffeeById(coffeeId)
            _uiState.value = _uiState.value.copy(
                coffeeItem = item,
                isLoading = false
            )
        }
    }

    fun onSizeSelected(size: String) {
        _uiState.value = _uiState.value.copy(selectedSize = size)
    }

    fun toggleFavorite() {
        val currentItem = _uiState.value.coffeeItem ?: return
        viewModelScope.launch {
            repository.toggleFavorite(currentItem.id, !currentItem.isFavorite)
            // Refresh local state
            loadCoffee(currentItem.id)
        }
    }

    fun addToCart() {
        val currentItem = _uiState.value.coffeeItem ?: return
        viewModelScope.launch {
            repository.addToCart(
                coffeeId = currentItem.id,
                size = _uiState.value.selectedSize
            )
            _uiEvent.send(UiEvent.ShowSnackbar("${currentItem.name} added to cart! ☕"))
        }
    }
}
