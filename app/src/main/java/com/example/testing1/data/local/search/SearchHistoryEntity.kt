package com.example.testing1.data.local.search

import kotlinx.serialization.Serializable

@Serializable
data class SearchHistoryEntity(
    val searchId: String,
    val userId: String,
    val query: String,
    val resultCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)
