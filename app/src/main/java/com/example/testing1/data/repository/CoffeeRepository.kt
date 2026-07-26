package com.example.testing1.data.repository

import com.example.testing1.data.local.address.AddressDao
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartDao
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.data.local.cart.CartItemWithCoffee
import com.example.testing1.data.local.coffee.CoffeeDao
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.local.order.OrderDao
import com.example.testing1.data.local.order.OrderEntity
import com.example.testing1.data.local.order.OrderItemEntity
import com.example.testing1.data.local.order.OrderWithItems
import com.example.testing1.data.local.search.SearchDao
import com.example.testing1.data.local.search.SearchHistoryEntity
import com.example.testing1.data.local.user.UserDao
import com.example.testing1.data.local.user.UserEntity
import com.example.testing1.data.remote.model.RemoteCoffee
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class CoffeeRepository @Inject constructor(
    private val coffeeDao: CoffeeDao,
    private val cartDao: CartDao,
    private val userDao: UserDao,
    private val addressDao: AddressDao,
    private val orderDao: OrderDao,
    private val searchDao: SearchDao,
    private val supabase: SupabaseClient
) {

    fun getAllCoffee(): Flow<List<CoffeeEntity>> =
        coffeeDao.getAllCoffee()

    suspend fun getCoffeeById(id: Int): CoffeeEntity? =
        coffeeDao.getCoffeeById(id)

    suspend fun insertAll(coffees: List<CoffeeEntity>) {
        coffeeDao.insertAll(coffees)
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        coffeeDao.updateFavoriteStatus(id, isFavorite)
    }

    fun getFavoriteCoffee(): Flow<List<CoffeeEntity>> =
        coffeeDao.getFavoriteCoffee()

    // Sync Operations
    suspend fun refreshCoffee() {
        try {
            val remoteCoffees = supabase.from("coffee")
                .select().decodeList<RemoteCoffee>()
            
            val entities = remoteCoffees.map { it.toEntity() }
            coffeeDao.insertAll(entities)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun RemoteCoffee.toEntity() = CoffeeEntity(
        id = id,
        name = name,
        description = description,
        category = category,
        price = price,
        imageUrl = imageUrl
    )

    // Cart Operations
    fun getCartItems(): Flow<List<CartItemWithCoffee>> =
        cartDao.getCartItems()

    suspend fun addToCart(coffeeId: Int, size: String, quantity: Int = 1) {
        val existingItem = cartDao.getCartItemByCoffeeAndSize(coffeeId, size)
        if (existingItem != null) {
            cartDao.updateCartItem(existingItem.copy(quantity = existingItem.quantity + quantity))
        } else {
            cartDao.addToCart(CartEntity(coffeeId = coffeeId, quantity = quantity, size = size))
        }
    }

    suspend fun updateCartQuantity(cartItem: CartEntity, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartDao.removeFromCart(cartItem)
        } else {
            cartDao.updateCartItem(cartItem.copy(quantity = newQuantity))
        }
    }

    suspend fun removeFromCart(cartItem: CartEntity) {
        cartDao.removeFromCart(cartItem)
    }

    // User Operations
    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun updateUser(user: UserEntity) {
        userDao.upsertUser(user)
    }

    // Address Operations
    fun getAddresses(): Flow<List<AddressEntity>> = addressDao.getAddresses()

    suspend fun addAddress(address: AddressEntity) {
        addressDao.insertAddress(address)
    }

    suspend fun deleteAddress(address: AddressEntity) {
        addressDao.deleteAddress(address)
    }

    suspend fun setAsDefaultAddress(addressId: Int) {
        addressDao.setAsDefault(addressId)
    }

    // Order Operations
    fun getOrders(): Flow<List<OrderWithItems>> = orderDao.getOrders()

    suspend fun placeOrder(address: AddressEntity, totalPrice: Double) {
        addressDao.updateAddress(address.copy(lastUsedTimestamp = System.currentTimeMillis()))

        val order = OrderEntity(
            totalPrice = totalPrice,
            snapshotAddress = "${address.tag}: ${address.fullAddress}"
        )
        val orderId = orderDao.insertOrder(order).toInt()

        val cartItems = cartDao.getCartItems().first()
        val orderItems = cartItems.map { item ->
            OrderItemEntity(
                orderId = orderId,
                coffeeName = item.coffee.name,
                quantity = item.cartItem.quantity,
                size = item.cartItem.size,
                snapshotPrice = item.coffee.price
            )
        }
        orderDao.insertOrderItems(orderItems)
        cartItems.forEach { cartDao.removeFromCart(it.cartItem) }
    }

    // Search Operations
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> = searchDao.getRecentSearches()

    suspend fun saveSearch(query: String, resultCount: Int) {
        if (query.isBlank()) return
        searchDao.insertSearch(SearchHistoryEntity(query = query, resultCount = resultCount))
        searchDao.deleteOldSearches()
    }

    suspend fun clearSearchHistory() {
        searchDao.clearHistory()
    }
}
