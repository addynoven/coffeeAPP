package com.example.testing1.data.local.search

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    @Insert
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("SELECT * FROM search_history WHERE userId = 1 ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE userId = 1 AND searchId NOT IN (SELECT searchId FROM search_history WHERE userId = 1 ORDER BY timestamp DESC LIMIT 50)")
    suspend fun deleteOldSearches()

    @Query("DELETE FROM search_history WHERE userId = 1")
    suspend fun clearHistory()
}
