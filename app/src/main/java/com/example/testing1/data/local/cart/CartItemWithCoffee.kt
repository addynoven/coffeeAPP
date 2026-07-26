package com.example.testing1.data.local.cart

import com.example.testing1.data.local.coffee.CoffeeEntity


import androidx.room.Embedded
import androidx.room.Relation

data class CartItemWithCoffee(
    @Embedded val cartItem: CartEntity,
    @Relation(
        parentColumn = "coffeeId",
        entityColumn = "id"
    )
    val coffee: CoffeeEntity
)
