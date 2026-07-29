package com.example.testing1.screens.cartscreen

import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartItemWithCoffee
import com.example.testing1.models.Discount

data class CartUiState(
    val cartItems: List<CartItemWithCoffee> = emptyList(),
    val addresses: List<AddressEntity> = emptyList(),
    val selectedAddress: AddressEntity? = null,
    val availableDiscounts: List<Discount> = emptyList(),
    val selectedDiscount: Discount? = null,
    val promoCodeInput: String = "",
    val promoCodeError: String? = null,
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val totalPrice: Double = 0.0,
    val deliveryFee: Double = 1.0,
    val isLoading: Boolean = true,
    val isOrderPlaced: Boolean = false
)
