package com.example.testing1.data.local.cart

import com.example.testing1.data.local.coffee.CoffeeEntity

data class CartItemWithCoffee(
    val cartItem: CartEntity,
    val coffee: CoffeeEntity
)
