package com.example.testing1.data.local.coffee

import com.example.testing1.models.CoffeeCategory
import kotlinx.serialization.Serializable

@Serializable
data class CoffeeEntity(
    val id: String,
    val name: String,
    val description: String,
    val category: CoffeeCategory,
    val price: Double,
    val imageUrl: String,
    val isFavorite: Boolean = false,
    
    // Localization
    val nameJa: String? = null,
    val descriptionJa: String? = null,
    val nameDe: String? = null,
    val descriptionDe: String? = null,
    val nameRu: String? = null,
    val descriptionRu: String? = null,
    val namePt: String? = null,
    val descriptionPt: String? = null,
    val nameFr: String? = null,
    val descriptionFr: String? = null,
    val nameAr: String? = null,
    val descriptionAr: String? = null,
    val nameEs: String? = null,
    val descriptionEs: String? = null,
    val nameZh: String? = null,
    val descriptionZh: String? = null,
    val nameIt: String? = null,
    val descriptionIt: String? = null
) {
    fun getLocalizedName(language: String): String {
        return when (language) {
            "ja" -> nameJa ?: name
            "de" -> nameDe ?: name
            "ru" -> nameRu ?: name
            "pt" -> namePt ?: name
            "fr" -> nameFr ?: name
            "ar" -> nameAr ?: name
            "es" -> nameEs ?: name
            "zh" -> nameZh ?: name
            "it" -> nameIt ?: name
            else -> name
        }.ifBlank { name }
    }

    fun getLocalizedDescription(language: String): String {
        return when (language) {
            "ja" -> descriptionJa ?: description
            "de" -> descriptionDe ?: description
            "ru" -> descriptionRu ?: description
            "pt" -> descriptionPt ?: description
            "fr" -> descriptionFr ?: description
            "ar" -> descriptionAr ?: description
            "es" -> descriptionEs ?: description
            "zh" -> descriptionZh ?: description
            "it" -> descriptionIt ?: description
            else -> description
        }.ifBlank { description }
    }
}
