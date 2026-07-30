package com.example.testing1.data.remote.model

import com.example.testing1.models.CoffeeCategory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteCoffee(
    val id: String,
    val name: String,
    val description: String,
    val category: CoffeeCategory,
    val price: Double,
    @SerialName("image_url")
    val imageUrl: String,
    @SerialName("updated_at")
    val updatedAt: String,
    
    // Localization fields
    @SerialName("name_ja") val nameJa: String? = null,
    @SerialName("description_ja") val descriptionJa: String? = null,
    @SerialName("name_de") val nameDe: String? = null,
    @SerialName("description_de") val descriptionDe: String? = null,
    @SerialName("name_ru") val nameRu: String? = null,
    @SerialName("description_ru") val descriptionRu: String? = null,
    @SerialName("name_pt") val namePt: String? = null,
    @SerialName("description_pt") val descriptionPt: String? = null,
    @SerialName("name_fr") val nameFr: String? = null,
    @SerialName("description_fr") val descriptionFr: String? = null,
    @SerialName("name_ar") val nameAr: String? = null,
    @SerialName("description_ar") val descriptionAr: String? = null,
    @SerialName("name_es") val nameEs: String? = null,
    @SerialName("description_es") val descriptionEs: String? = null,
    @SerialName("name_zh") val nameZh: String? = null,
    @SerialName("description_zh") val descriptionZh: String? = null,
    @SerialName("name_it") val nameIt: String? = null,
    @SerialName("description_it") val descriptionIt: String? = null
)
