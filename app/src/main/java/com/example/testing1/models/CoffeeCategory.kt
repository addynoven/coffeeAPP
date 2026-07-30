package com.example.testing1.models

import com.example.testing1.R
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CoffeeCategory(val displayNameRes: Int) {
    @SerialName("All Coffee")
    AllCoffee(R.string.all_coffee_label),
    @SerialName("Macchiato")
    Macchiato(R.string.category_macchiato),
    @SerialName("Latte")
    Latte(R.string.category_latte),
    @SerialName("Cappuccino")
    Cappuccino(R.string.category_cappuccino),
    @SerialName("Espresso")
    Espresso(R.string.category_espresso),
    @SerialName("Flat White")
    FlatWhite(R.string.category_flat_white);

    companion object {
        fun fromString(value: String): CoffeeCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: when (value) {
                    "All Coffee" -> AllCoffee
                    "Flat White" -> FlatWhite
                    else -> Espresso // Fallback
                }
        }
    }
}
