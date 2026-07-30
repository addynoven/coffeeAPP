package com.example.testing1.data.local.user

import kotlinx.serialization.Serializable

@Serializable
data class UserEntity(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)
