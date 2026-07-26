package com.example.testing1.data.local.cart

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart")
data class CartEntity(
    @PrimaryKey(autoGenerate = true)
    val cartId: Int = 0,
    val coffeeId: Int,
    val quantity: Int,
    val size: String
)
