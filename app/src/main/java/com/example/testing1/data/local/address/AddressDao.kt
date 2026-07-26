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
    @Query("SELECT * FROM addresses WHERE userId = 1 ORDER BY lastUsedTimestamp DESC")
    fun getAddresses(): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity)

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = 1")
    suspend fun clearDefaults()

    @Transaction
    suspend fun setAsDefault(addressId: Int) {
        clearDefaults()
        setAsDefaultById(addressId)
    }

    @Query("UPDATE addresses SET isDefault = 1 WHERE addressId = :addressId")
    suspend fun setAsDefaultById(addressId: Int)

    @Query("SELECT COUNT(*) FROM addresses")
    suspend fun getAddressCount(): Int
}
