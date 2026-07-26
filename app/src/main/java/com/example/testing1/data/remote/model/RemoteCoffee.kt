package com.example.testing1.data.remote.model

import com.example.testing1.models.CoffeeCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteCoffee(
    val id: Int,
    val name: String,
    val description: String,
    val category: CoffeeCategory,
    val price: Double,
    @SerialName("image_url")
    val imageUrl: String
)
