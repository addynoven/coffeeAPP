package com.example.testing1.data.local

import androidx.room.TypeConverter
import com.example.testing1.models.CoffeeCategory

class Converters {
    @TypeConverter
    fun fromCoffeeCategory(category: CoffeeCategory): String {
        return category.name
    }

    @TypeConverter
    fun toCoffeeCategory(value: String): CoffeeCategory {
        return CoffeeCategory.valueOf(value)
    }
}