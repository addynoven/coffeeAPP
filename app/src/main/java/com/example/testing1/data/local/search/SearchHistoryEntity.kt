package com.example.testing1.data.local.search

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.testing1.data.local.user.UserEntity

@Entity(
    tableName = "search_history",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val searchId: Int = 0,
    val userId: Int = 1,
    val query: String,
    val resultCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
