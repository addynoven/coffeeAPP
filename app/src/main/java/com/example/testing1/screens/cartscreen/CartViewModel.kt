package com.example.testing1.screens.cartscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.data.repository.CoffeeRepository
import com.example.testing1.data.repository.DiscountRepository
import com.example.testing1.models.Discount
import com.example.testing1.util.PricingEngine
import com.example.testing1.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val repository: CoffeeRepository,
    private val discountRepository: DiscountRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _selectedDiscount = MutableStateFlow<Discount?>(null)
    private val _promoCodeInput = MutableStateFlow("")

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
                _selectedDiscount,
                _promoCodeInput
            ) { items, addresses, discounts, selectedDiscount, promoCodeInput ->
                val currentSelectedAddress = _uiState.value.selectedAddress
                val newSelectedAddress = currentSelectedAddress ?: addresses.find { it.isDefault } ?: addresses.firstOrNull()
                
                val pricing = PricingEngine.calculatePrice(items, selectedDiscount)

                val previousDiscount = _uiState.value.selectedDiscount
                if (selectedDiscount != null && previousDiscount?.code != selectedDiscount.code) {
                    _uiEvent.send(UiEvent.ShowSnackbar("Coupon '${selectedDiscount.code}' applied! 🏷️"))
                }

                _uiState.value = _uiState.value.copy(
                    cartItems = items,
                    addresses = addresses,
                    availableDiscounts = discounts,
                    selectedDiscount = selectedDiscount,
                    promoCodeInput = promoCodeInput,
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

    fun onPromoCodeChange(newText: String) {
        _promoCodeInput.value = newText
        // Auto-apply if match found
        val discounts = _uiState.value.availableDiscounts
        val match = discounts.find { it.code.equals(newText, ignoreCase = true) }
        _selectedDiscount.value = match
    }

    fun onClearPromoCode() {
        _promoCodeInput.value = ""
        _selectedDiscount.value = null
    }

    fun onDiscountSelected(discount: Discount?) {
        _selectedDiscount.value = discount
        _promoCodeInput.value = discount?.code ?: ""
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
