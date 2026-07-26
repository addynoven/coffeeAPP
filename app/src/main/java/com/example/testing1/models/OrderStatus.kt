package com.example.testing1.models

import androidx.annotation.StringRes
import com.example.testing1.R

enum class OrderStatus(@StringRes val labelRes: Int) {
    PREPARING(R.string.status_preparing),
    IN_DELIVERY(R.string.status_in_delivery),
    DELIVERED(R.string.status_delivered),
    CANCELLED(R.string.status_cancelled);

    companion object {
        fun fromString(value: String): OrderStatus {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: PREPARING
        }
    }
}
