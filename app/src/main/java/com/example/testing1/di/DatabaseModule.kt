package com.example.testing1.di

import android.content.Context
import androidx.room.Room
import com.example.testing1.data.local.CoffeeDatabase
import com.example.testing1.data.local.address.AddressDao
import com.example.testing1.data.local.cart.CartDao
import com.example.testing1.data.local.coffee.CoffeeDao
import com.example.testing1.data.local.discount.DiscountDao
import com.example.testing1.data.local.order.OrderDao
import com.example.testing1.data.local.search.SearchDao
import com.example.testing1.data.local.user.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideCoffeeDatabase(
        @ApplicationContext context: Context
    ): CoffeeDatabase {
        return Room.databaseBuilder(
            context,
            CoffeeDatabase::class.java,
            "coffee_database"
        ).fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideCoffeeDao(
        database: CoffeeDatabase
    ): CoffeeDao {
        return database.coffeeDao()
    }

    @Provides
    fun provideCartDao(
        database: CoffeeDatabase
    ): CartDao {
        return database.cartDao()
    }

    @Provides
    fun provideUserDao(
        database: CoffeeDatabase
    ): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideAddressDao(
        database: CoffeeDatabase
    ): AddressDao {
        return database.addressDao()
    }

    @Provides
    fun provideOrderDao(
        database: CoffeeDatabase
    ): OrderDao {
        return database.orderDao()
    }

    @Provides
    fun provideSearchDao(
        database: CoffeeDatabase
    ): SearchDao {
        return database.searchDao()
    }

    @Provides
    fun provideDiscountDao(
        database: CoffeeDatabase
    ): DiscountDao {
        return database.discountDao()
    }
}
