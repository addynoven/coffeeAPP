package com.example.testing1.data.local.coffee

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.testing1.models.CoffeeCategory

@Entity(tableName = "coffee")
data class CoffeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String,
    val category: CoffeeCategory,
    val price: Double,
    val imageUrl: String,
    val isFavorite: Boolean = false
)
