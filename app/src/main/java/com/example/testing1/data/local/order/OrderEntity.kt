package com.example.testing1.data.local.order

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val orderId: Int = 0,
    val userId: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val totalPrice: Double,
    val status: String = "Preparing", // Preparing, Delivered, etc.
    val snapshotAddress: String // Immutable copy of address at time of order
)
