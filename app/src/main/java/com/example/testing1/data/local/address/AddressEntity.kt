package com.example.testing1.data.local.address

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.testing1.data.local.user.UserEntity

@Entity(
    tableName = "addresses",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["isDefault"])]
)
data class AddressEntity(
    @PrimaryKey(autoGenerate = true)
    val addressId: Int = 0,
    val userId: String,
    val tag: String, // Home, Work, etc.
    val fullAddress: String,
    val isDefault: Boolean = false,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
