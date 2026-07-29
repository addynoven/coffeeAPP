package com.example.testing1.data.local.cart

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart",
    indices = [Index(value = ["userId", "coffeeId", "size"], unique = true)]
)
data class CartEntity(
    @PrimaryKey(autoGenerate = true)
    val cartId: Int = 0,
    val userId: String,
    val coffeeId: Int,
    val quantity: Int,
    val size: String
)
