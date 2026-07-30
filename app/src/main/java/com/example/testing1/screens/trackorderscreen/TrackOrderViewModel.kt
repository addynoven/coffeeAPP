package com.example.testing1.screens.trackorderscreen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.repository.CoffeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackOrderViewModel @Inject constructor(
    private val repository: CoffeeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(TrackOrderUiState())
    val uiState: StateFlow<TrackOrderUiState> = _uiState

    init {
        loadOrder()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            repository.getOrderById(orderId).collect { order ->
                _uiState.value = _uiState.value.copy(
                    orderWithItems = order,
                    isLoading = false
                )
            }
        }
    }
}
