package com.example.testing1.screens.cartscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.data.repository.CoffeeRepository
import com.example.testing1.data.repository.DiscountRepository
import com.example.testing1.models.Discount
import com.example.testing1.util.PricingEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CoffeeRepository,
    private val discountRepository: DiscountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState

    private val _selectedDiscount = MutableStateFlow<Discount?>(null)

    init {
        loadData()
        refreshDiscounts()
    }

    private fun refreshDiscounts() {
        viewModelScope.launch {
            discountRepository.refreshDiscounts()
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                repository.getCartItems(), 
                repository.getAddresses(),
                discountRepository.getAllDiscounts(),
                _selectedDiscount
            ) { items, addresses, discounts, selectedDiscount ->
                val currentSelectedAddress = _uiState.value.selectedAddress
                val newSelectedAddress = currentSelectedAddress ?: addresses.find { it.isDefault } ?: addresses.firstOrNull()
                
                val pricing = PricingEngine.calculatePrice(items, selectedDiscount)

                _uiState.value = _uiState.value.copy(
                    cartItems = items,
                    addresses = addresses,
                    availableDiscounts = discounts,
                    selectedDiscount = selectedDiscount,
                    selectedAddress = newSelectedAddress,
                    subtotal = pricing.subtotal,
                    discountAmount = pricing.discountAmount,
                    totalPrice = pricing.grandTotal,
                    deliveryFee = pricing.deliveryFee,
                    isLoading = false
                )
            }.collect {}
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

    fun onDiscountSelected(discount: Discount?) {
        _selectedDiscount.value = discount
    }

    fun placeOrder() {
        val currentState = _uiState.value
        val address = currentState.selectedAddress ?: return
        if (currentState.cartItems.isEmpty()) return

        viewModelScope.launch {
            repository.placeOrder(address, currentState.selectedDiscount?.code)
            _uiState.value = _uiState.value.copy(isOrderPlaced = true)
        }
    }
    
    fun dismissOrderSuccess() {
        _uiState.value = _uiState.value.copy(isOrderPlaced = false)
    }
}
