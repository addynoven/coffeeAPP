package com.example.testing1.data.local.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemEntity(
    val orderItemId: String,
    val orderId: String,
    val coffeeName: String,
    val quantity: Int,
    val size: String,
    val snapshotPrice: Double // Price at time of purchase
)
