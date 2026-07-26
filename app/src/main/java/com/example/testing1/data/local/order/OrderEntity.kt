package com.example.testing1.data.local.order

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testing1.models.OrderStatus

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val orderId: Int = 0,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val totalPrice: Double,
    val status: OrderStatus = OrderStatus.PREPARING,
    val snapshotAddress: String // Immutable copy of address at time of order
)
