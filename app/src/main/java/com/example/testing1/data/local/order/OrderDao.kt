package com.example.testing1.data.local.order

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Transaction
    @Query("SELECT * FROM orders WHERE userId = 1 ORDER BY timestamp DESC")
    fun getOrders(): Flow<List<OrderWithItems>>
}
