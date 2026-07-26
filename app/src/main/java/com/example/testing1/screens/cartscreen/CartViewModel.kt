package com.example.testing1.screens.cartscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.data.repository.CoffeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CoffeeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(repository.getCartItems(), repository.getAddresses()) { items, addresses ->
                items to addresses
            }.collect { (items, addresses) ->
                val totalPrice = items.sumOf { it.coffee.price * it.cartItem.quantity }
                val currentSelected = _uiState.value.selectedAddress
                val newSelected = currentSelected ?: addresses.find { it.isDefault } ?: addresses.firstOrNull()
                
                _uiState.value = _uiState.value.copy(
                    cartItems = items,
                    addresses = addresses,
                    selectedAddress = newSelected,
                    totalPrice = totalPrice,
                    isLoading = false
                )
            }
        }
    }

    fun updateQuantity(cartEntity: CartEntity, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartEntity, newQuantity)
        }
    }

    fun onAddressSelected(address: AddressEntity) {
        _uiState.value = _uiState.value.copy(selectedAddress = address)
    }

    fun placeOrder() {
        val currentState = _uiState.value
        val address = currentState.selectedAddress ?: return
        if (currentState.cartItems.isEmpty()) return

        viewModelScope.launch {
            repository.placeOrder(address, currentState.totalPrice + currentState.deliveryFee)
            _uiState.value = _uiState.value.copy(isOrderPlaced = true)
        }
    }
    
    fun dismissOrderSuccess() {
        _uiState.value = _uiState.value.copy(isOrderPlaced = false)
    }
}
