package com.example.testing1.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.testing1.data.local.address.AddressDao
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartDao
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.data.local.coffee.CoffeeDao
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.local.discount.DiscountDao
import com.example.testing1.data.local.discount.DiscountEntity
import com.example.testing1.data.local.order.OrderDao
import com.example.testing1.data.local.order.OrderEntity
import com.example.testing1.data.local.order.OrderItemEntity
import com.example.testing1.data.local.search.SearchDao
import com.example.testing1.data.local.search.SearchHistoryEntity
import com.example.testing1.data.local.user.UserDao
import com.example.testing1.data.local.user.UserEntity

@TypeConverters(Converters::class)
@Database(
    entities = [
        CoffeeEntity::class,
        CartEntity::class,
        UserEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        SearchHistoryEntity::class,
        DiscountEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class CoffeeDatabase : RoomDatabase() {
    abstract fun coffeeDao(): CoffeeDao
    abstract fun cartDao(): CartDao
    abstract fun userDao(): UserDao
    abstract fun addressDao(): AddressDao
    abstract fun orderDao(): OrderDao
    abstract fun searchDao(): SearchDao
    abstract fun discountDao(): DiscountDao
}
