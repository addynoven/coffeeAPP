package com.example.testing1.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CoffeeCategory(val displayName: String) {
    @SerialName("All Coffee")
    AllCoffee("All Coffee"),
    @SerialName("Macchiato")
    Macchiato("Macchiato"),
    @SerialName("Latte")
    Latte("Latte"),
    @SerialName("Cappuccino")
    Cappuccino("Cappuccino"),
    @SerialName("Espresso")
    Espresso("Espresso"),
    
    @SerialName("Flat White")
    FlatWhite("Flat White")
}