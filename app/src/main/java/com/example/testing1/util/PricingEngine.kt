package com.example.testing1.util

import com.example.testing1.data.local.cart.CartItemWithCoffee
import com.example.testing1.models.Discount
import com.example.testing1.models.DiscountType

object PricingEngine {

    data class PriceBreakdown(
        val subtotal: Double,
        val discountAmount: Double,
        val deliveryFee: Double,
        val grandTotal: Double
    )

    fun calculatePrice(
        items: List<CartItemWithCoffee>,
        appliedDiscount: Discount?,
        deliveryFee: Double = 1.0
    ): PriceBreakdown {
        // 1. Calculate Subtotal (with size modifiers if any)
        val subtotal = items.sumOf { item ->
            val basePrice = item.coffee.price
            val sizeModifier = when (item.cartItem.size.uppercase()) {
                "M" -> 0.5
                "L" -> 1.0
                else -> 0.0
            }
            (basePrice + sizeModifier) * item.cartItem.quantity
        }

        // 2. Calculate Discount
        var discountAmount = 0.0
        appliedDiscount?.let { discount ->
            if (subtotal >= discount.minOrderAmount) {
                discountAmount = when (discount.type) {
                    is DiscountType.Percentage -> (subtotal * (discount.value / 100.0))
                    is DiscountType.FixedAmount -> discount.value
                }
                
                // Apply cap if exists
                discount.maxDiscountAmount?.let { cap ->
                    if (discountAmount > cap) {
                        discountAmount = cap
                    }
                }
            }
        }

        // 3. Final Total
        val grandTotal = (subtotal - discountAmount + deliveryFee).coerceAtLeast(0.0)

        return PriceBreakdown(
            subtotal = subtotal,
            discountAmount = discountAmount,
            deliveryFee = deliveryFee,
            grandTotal = grandTotal
        )
    }
}
