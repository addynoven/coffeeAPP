package com.example.testing1.models

import kotlinx.serialization.Serializable

@Serializable
sealed class DiscountType {
    @Serializable
    data object Percentage : DiscountType()
    @Serializable
    data object FixedAmount : DiscountType()
}

@Serializable
data class Discount(
    val code: String,
    val description: String,
    val type: DiscountType,
    val value: Double,
    val minOrderAmount: Double = 0.0,
    val maxDiscountAmount: Double? = null
)
