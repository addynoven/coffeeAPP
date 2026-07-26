package com.example.testing1.screens.cartscreen

import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartItemWithCoffee

data class CartUiState(
    val cartItems: List<CartItemWithCoffee> = emptyList(),
    val addresses: List<AddressEntity> = emptyList(),
    val selectedAddress: AddressEntity? = null,
    val totalPrice: Double = 0.0,
    val deliveryFee: Double = 1.0,
    val isLoading: Boolean = true,
    val isOrderPlaced: Boolean = false
)
