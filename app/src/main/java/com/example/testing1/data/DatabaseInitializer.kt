package com.example.testing1.data

import com.example.testing1.data.local.address.AddressDao
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.discount.DiscountDao
import com.example.testing1.data.local.discount.DiscountEntity
import com.example.testing1.data.local.user.UserDao
import com.example.testing1.data.local.user.UserEntity
import javax.inject.Inject

class DatabaseInitializer @Inject constructor(
    private val userDao: UserDao,
    private val addressDao: AddressDao,
    private val discountDao: DiscountDao
) {
    suspend fun initialize() {
        // Coffee is now synced from Supabase in HomeViewModel
        
        // Seed Discounts
        val discounts = listOf(
            DiscountEntity(
                code = "COFFEE10",
                description = "10% off your order",
                type = "Percentage",
                value = 10.0,
                minOrderAmount = 0.0,
                maxDiscountAmount = null
            )
        )
        discountDao.insertDiscounts(discounts)
        
        if (userDao.getUserCount() == 0) {
            userDao.upsertUser(
                UserEntity(
                    id = "dev_user_123",
                    name = "Mohammad Anas",
                    email = "anas@example.com"
                )
            )
        }

        if (addressDao.getAddressCount() == 0) {
            addressDao.insertAddress(
                AddressEntity(
                    tag = "Home",
                    fullAddress = "Janatha Road, Palarivattom, Ernakulam, Kerala - 682025",
                    isDefault = true,
                    userId = "dev_user_123"
                )
            )
        }
    }
}
