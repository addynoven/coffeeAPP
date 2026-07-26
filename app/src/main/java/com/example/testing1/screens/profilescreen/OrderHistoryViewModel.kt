package com.example.testing1.screens.profilescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.repository.CoffeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderHistoryViewModel @Inject constructor(
    private val repository: CoffeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrderHistoryUiState())
    val uiState: StateFlow<OrderHistoryUiState> = _uiState

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            repository.getOrders().collect { orders ->
                _uiState.value = _uiState.value.copy(
                    orders = orders,
                    isLoading = false
                )
            }
        }
    }
}
