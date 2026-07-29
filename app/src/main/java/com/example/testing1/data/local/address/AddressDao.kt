package com.example.testing1.data.local.address

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDao {
    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY lastUsedTimestamp DESC")
    fun getAddresses(userId: String): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("DELETE FROM addresses WHERE userId = :userId")
    suspend fun clearAddresses(userId: String)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaults(userId: String)

    @Transaction
    suspend fun setAsDefault(userId: String, addressId: Int) {
        clearDefaults(userId)
        setAsDefaultById(addressId)
    }

    @Query("UPDATE addresses SET isDefault = 1 WHERE addressId = :addressId")
    suspend fun setAsDefaultById(addressId: Int)

    @Query("SELECT COUNT(*) FROM addresses")
    suspend fun getAddressCount(): Int
}
