package com.example.testing1.data.local.cart

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToCart(cartEntity: CartEntity)

    @Update
    suspend fun updateCartItem(cartEntity: CartEntity)

    @Delete
    suspend fun removeFromCart(cartEntity: CartEntity)

    @Transaction
    @Query("SELECT * FROM cart WHERE userId = :userId")
    fun getCartItems(userId: String): Flow<List<CartItemWithCoffee>>

    @Query("SELECT * FROM cart WHERE userId = :userId AND coffeeId = :coffeeId AND size = :size LIMIT 1")
    suspend fun getCartItemByCoffeeAndSize(userId: String, coffeeId: Int, size: String): CartEntity?
}
