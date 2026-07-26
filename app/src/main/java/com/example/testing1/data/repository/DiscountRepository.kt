package com.example.testing1.data.repository

import com.example.testing1.data.local.discount.DiscountDao
import com.example.testing1.data.local.discount.DiscountEntity
import com.example.testing1.data.local.discount.toEntity
import com.example.testing1.data.remote.model.RemoteDiscount
import com.example.testing1.models.Discount
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscountRepository @Inject constructor(
    private val discountDao: DiscountDao,
    private val supabase: SupabaseClient
) {
    fun getAllDiscounts(): Flow<List<Discount>> =
        discountDao.getAllDiscounts().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getDiscountByCode(code: String): Discount? =
        discountDao.getDiscountByCode(code)?.toDomain()

    suspend fun refreshDiscounts() {
        try {
            val remoteDiscounts = supabase.from("discounts")
                .select()
                .decodeList<RemoteDiscount>()

            val entities = remoteDiscounts.map { remote ->
                DiscountEntity(
                    code = remote.code,
                    description = remote.description,
                    type = remote.type,
                    value = remote.value,
                    minOrderAmount = remote.minOrderAmount,
                    maxDiscountAmount = remote.maxDiscountAmount
                )
            }

            discountDao.clearDiscounts()
            discountDao.insertDiscounts(entities)
        } catch (e: Exception) {
            println("Sync Discounts Error: ${e.message}")
        }
    }
}
