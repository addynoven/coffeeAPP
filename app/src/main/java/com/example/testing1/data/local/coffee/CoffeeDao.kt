package com.example.testing1.data.local.coffee

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoffeeDao {
    @Query("SELECT * FROM coffee")
    fun getAllCoffee(): Flow<List<CoffeeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(coffees: List<CoffeeEntity>)

    @Query("SELECT * FROM coffee WHERE id = :id")
    suspend fun getCoffeeById(id: Int): CoffeeEntity?

    @Query("SELECT COUNT(*) FROM coffee")
    suspend fun getCoffeeCount(): Int

    @Query("UPDATE coffee SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Int, isFavorite: Boolean)

    @Query("SELECT * FROM coffee WHERE isFavorite = 1")
    fun getFavoriteCoffee(): Flow<List<CoffeeEntity>>
}
