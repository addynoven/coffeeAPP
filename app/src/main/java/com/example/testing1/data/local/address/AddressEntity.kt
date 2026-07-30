package com.example.testing1.data.local.address

import kotlinx.serialization.Serializable

@Serializable
data class AddressEntity(
    val addressId: String,
    val userId: String,
    val tag: String, // Home, Work, etc.
    val fullAddress: String,
    val isDefault: Boolean = false,
    val lastUsedTimestamp: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null
)
