package com.example.testing1.data.local.discount

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiscountDao {
    @Query("SELECT * FROM discounts")
    fun getAllDiscounts(): Flow<List<DiscountEntity>>

    @Query("SELECT * FROM discounts WHERE code = :code")
    suspend fun getDiscountByCode(code: String): DiscountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiscounts(discounts: List<DiscountEntity>)

    @Query("DELETE FROM discounts")
    suspend fun clearDiscounts()
}
