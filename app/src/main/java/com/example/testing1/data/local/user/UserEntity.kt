package com.example.testing1.data.local.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val email: String
)
