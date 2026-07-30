package com.example.testing1.data.local.cart

import kotlinx.serialization.Serializable

@Serializable
data class CartEntity(
    val cartId: String,
    val userId: String,
    val coffeeId: String,
    val quantity: Int,
    val size: String
)
