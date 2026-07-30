package com.example.testing1.data.local.discount

import com.example.testing1.models.Discount
import com.example.testing1.models.DiscountType
import kotlinx.serialization.Serializable

@Serializable
data class DiscountEntity(
    val code: String,
    val description: String,
    val type: String, // Stored as String for simplicity in this step, or use converter
    val value: Double,
    val minOrderAmount: Double,
    val maxDiscountAmount: Double?
) {
    fun toDomain() = Discount(
        code = code,
        description = description,
        type = if (type == "Percentage") DiscountType.Percentage else DiscountType.FixedAmount,
        value = value,
        minOrderAmount = minOrderAmount,
        maxDiscountAmount = maxDiscountAmount
    )
}

fun Discount.toEntity() = DiscountEntity(
    code = code,
    description = description,
    type = when (type) {
        is DiscountType.Percentage -> "Percentage"
        is DiscountType.FixedAmount -> "FixedAmount"
    },
    value = value,
    minOrderAmount = minOrderAmount,
    maxDiscountAmount = maxDiscountAmount
)
