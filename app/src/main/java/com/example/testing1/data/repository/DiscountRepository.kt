package com.example.testing1.data.repository

import com.example.testing1.data.local.discount.DiscountEntity
import com.example.testing1.models.Discount
import com.powersync.PowerSyncDatabase
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscountRepository @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase,
    private val supabase: SupabaseClient
) {
    fun getAllDiscounts(): Flow<List<Discount>> =
        powerSyncDatabase.watch("SELECT * FROM discounts") { cursor ->
            val cols = cursor.columnNames
            DiscountEntity(
                code = cursor.getString(cols["code"]!!)!!,
                description = cursor.getString(cols["description"]!!)!!,
                type = cursor.getString(cols["type"]!!)!!,
                value = cursor.getDouble(cols["value"]!!)!!,
                minOrderAmount = cursor.getDouble(cols["min_order_amount"]!!)!!,
                maxDiscountAmount = cursor.getDouble(cols["max_discount_amount"]!!)
            ).toDomain()
        }

    suspend fun getDiscountByCode(code: String): Discount? =
        powerSyncDatabase.getOptional("SELECT * FROM discounts WHERE code = ?", listOf(code)) { cursor ->
            val cols = cursor.columnNames
            DiscountEntity(
                code = cursor.getString(cols["code"]!!)!!,
                description = cursor.getString(cols["description"]!!)!!,
                type = cursor.getString(cols["type"]!!)!!,
                value = cursor.getDouble(cols["value"]!!)!!,
                minOrderAmount = cursor.getDouble(cols["min_order_amount"]!!)!!,
                maxDiscountAmount = cursor.getDouble(cols["max_discount_amount"]!!)
            )
        }?.toDomain()

    suspend fun refreshDiscounts() {
        // PowerSync handles sync automatically
    }
}
