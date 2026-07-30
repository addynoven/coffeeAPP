package com.example.testing1.data.local.order

import com.example.testing1.models.OrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class OrderEntity(
    val orderId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalPrice: Double,
    val status: OrderStatus = OrderStatus.PREPARING,
    val snapshotAddress: String // Immutable copy of address at time of order
)
