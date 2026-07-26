package com.example.testing1.data.local

import androidx.room.TypeConverter
import com.example.testing1.models.CoffeeCategory
import com.example.testing1.models.OrderStatus

class Converters {
    @TypeConverter
    fun fromCoffeeCategory(category: CoffeeCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCoffeeCategory(value: String): CoffeeCategory {
        return CoffeeCategory.valueOf(value)
    }

    @TypeConverter
    fun fromOrderStatus(status: OrderStatus): String {
        return status.name
    }

    @TypeConverter
    fun toOrderStatus(value: String): OrderStatus {
        return OrderStatus.fromString(value)
    }
}