package com.example.testing1.data.local.order

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["orderId"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"])]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val orderItemId: Int = 0,
    val orderId: Int,
    val coffeeName: String,
    val quantity: Int,
    val size: String,
    val snapshotPrice: Double // Price at time of purchase
)
