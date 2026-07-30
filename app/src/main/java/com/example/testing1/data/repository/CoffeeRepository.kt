package com.example.testing1.data.repository

import android.util.Log
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.data.local.cart.CartItemWithCoffee
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.local.order.OrderEntity
import com.example.testing1.data.local.order.OrderItemEntity
import com.example.testing1.data.local.order.OrderWithItems
import com.example.testing1.data.local.search.SearchHistoryEntity
import com.example.testing1.data.local.user.UserEntity
import com.example.testing1.data.remote.model.OrderItemParams
import com.example.testing1.data.remote.model.PlaceOrderParams
import com.example.testing1.data.remote.model.RemoteOrder
import com.example.testing1.models.CoffeeCategory
import com.example.testing1.models.OrderStatus
import com.example.testing1.util.PricingEngine
import com.powersync.PowerSyncDatabase
import com.powersync.db.SqlCursor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID
import javax.inject.Inject

class CoffeeRepository @Inject constructor(
    private val powerSyncDatabase: PowerSyncDatabase,
    private val settingsRepository: SettingsRepository,
    private val discountRepository: DiscountRepository,
    private val authRepository: AuthRepository,
    private val supabase: SupabaseClient
) {
    val currentUserId: String
        get() = authRepository.currentUserId

    val syncStatus = powerSyncDatabase.currentStatus.asFlow()

    private fun mapCoffee(cursor: SqlCursor): CoffeeEntity {
        val cols = cursor.columnNames
        return try {
            val nameVal = cols["name"]?.let { cursor.getString(it) } ?: ""
            val descVal = cols["description"]?.let { cursor.getString(it) } ?: ""
            val priceVal = cols["price"]?.let { cursor.getDouble(it) } ?: 0.0

            CoffeeEntity(
                id = cols["id"]?.let { cursor.getString(it) } ?: "",
                name = nameVal,
                description = descVal,
                category = CoffeeCategory.fromString(cols["category"]?.let { cursor.getString(it) } ?: ""),
                price = priceVal,
                imageUrl = cols["image_url"]?.let { cursor.getString(it) } ?: ""
            )
        } catch (e: Exception) {
            Log.e("CoffeeRepository", "Error mapping coffee row", e)
            CoffeeEntity("", "Error Loading", "", CoffeeCategory.AllCoffee, 0.0, "")
        }
    }

    // Coffee Operations
    fun getCoffeeList(): Flow<List<CoffeeEntity>> =
        powerSyncDatabase.watch(
            "SELECT * FROM coffee",
            emptyList()
        ) { mapCoffee(it) }

    fun getAllCoffee(): Flow<List<CoffeeEntity>> = getCoffeeList()

    suspend fun refreshCoffee() {
        // PowerSync automatically handles offline/online syncing
    }

    fun getCoffeeById(id: String): Flow<CoffeeEntity?> =
        powerSyncDatabase.watch(
            "SELECT * FROM coffee WHERE id = ?",
            listOf(id)
        ) { mapCoffee(it) }.map { it.firstOrNull() }

    // Favorites Operations
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getFavoriteCoffees(): Flow<List<CoffeeEntity>> =
        authRepository.currentUserIdFlow.flatMapLatest { userId ->
            powerSyncDatabase.watch(
                """
                SELECT c.* FROM coffee c
                INNER JOIN favorites f ON c.id = f.coffee_id
                WHERE f.user_id = ?
                """.trimIndent(),
                listOf(userId)
            ) { mapCoffee(it) }
        }

    fun getFavoriteCoffee(): Flow<List<CoffeeEntity>> = getFavoriteCoffees()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun isFavorite(coffeeId: String): Flow<Boolean> =
        authRepository.currentUserIdFlow.flatMapLatest { userId ->
            powerSyncDatabase.watch(
                "SELECT 1 FROM favorites WHERE user_id = ? AND coffee_id = ?",
                listOf(userId, coffeeId)
            ) { true }.map { it.isNotEmpty() }
        }

    suspend fun toggleFavorite(coffeeId: String, targetState: Boolean) {
        val userId = currentUserId
        if (targetState) {
            powerSyncDatabase.execute(
                "INSERT OR IGNORE INTO favorites (id, user_id, coffee_id) VALUES (?, ?, ?)",
                listOf(UUID.randomUUID().toString(), userId, coffeeId)
            )
        } else {
            powerSyncDatabase.execute(
                "DELETE FROM favorites WHERE user_id = ? AND coffee_id = ?",
                listOf(userId, coffeeId)
            )
        }
    }

    // Cart Operations
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getCartItems(): Flow<List<CartItemWithCoffee>> =
        authRepository.currentUserIdFlow.flatMapLatest { userId ->
            powerSyncDatabase.watch(
                """
                SELECT cart.id as cart_id, cart.user_id, cart.coffee_id, cart.quantity, cart.size,
                       c.name, c.price, c.image_url
                FROM cart
                INNER JOIN coffee c ON cart.coffee_id = c.id
                WHERE cart.user_id = ?
                """.trimIndent(),
                listOf(userId)
            ) { cursor ->
                val cols = cursor.columnNames
                val cartItem = CartEntity(
                    cartId = cursor.getString(cols["cart_id"]!!)!!,
                    userId = cursor.getString(cols["user_id"]!!)!!,
                    coffeeId = cursor.getString(cols["coffee_id"]!!)!!,
                    quantity = cursor.getLong(cols["quantity"]!!)!!.toInt(),
                    size = cursor.getString(cols["size"]!!)!!
                )
                val coffee = CoffeeEntity(
                    id = cartItem.coffeeId,
                    name = cursor.getString(cols["name"]!!)!!,
                    price = cursor.getDouble(cols["price"]!!)!!,
                    imageUrl = cursor.getString(cols["image_url"]!!)!!,
                    description = "",
                    category = CoffeeCategory.Espresso
                )
                CartItemWithCoffee(cartItem, coffee)
            }
        }

    suspend fun addToCart(coffeeId: String, size: String, quantity: Int = 1) {
        powerSyncDatabase.writeTransaction { transaction ->
            val existing = transaction.getOptional(
                "SELECT * FROM cart WHERE user_id = ? AND coffee_id = ? AND size = ?",
                listOf(currentUserId, coffeeId, size)
            ) { cursor ->
                cursor.getLong(cursor.columnNames["quantity"]!!)!!
            }

            if (existing != null) {
                val newQty = existing + quantity
                transaction.execute(
                    "UPDATE cart SET quantity = ? WHERE user_id = ? AND coffee_id = ? AND size = ?",
                    listOf(newQty, currentUserId, coffeeId, size)
                )
            } else {
                transaction.execute(
                    "INSERT INTO cart (id, user_id, coffee_id, quantity, size) VALUES (?, ?, ?, ?, ?)",
                    listOf(
                        UUID.randomUUID().toString(),
                        currentUserId,
                        coffeeId,
                        quantity,
                        size
                    )
                )
            }
        }
    }

    suspend fun updateCartQuantity(cartItem: CartEntity, newQuantity: Int) {
        if (newQuantity <= 0) {
            removeFromCart(cartItem)
        } else {
            powerSyncDatabase.execute(
                "UPDATE cart SET quantity = ? WHERE user_id = ? AND coffee_id = ? AND size = ?",
                listOf(
                    newQuantity,
                    currentUserId,
                    cartItem.coffeeId,
                    cartItem.size
                )
            )
        }
    }

    suspend fun removeFromCart(cartItem: CartEntity) {
        powerSyncDatabase.execute(
            "DELETE FROM cart WHERE user_id = ? AND coffee_id = ? AND size = ?",
            listOf(currentUserId, cartItem.coffeeId, cartItem.size)
        )
    }

    // User Operations
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getUser(): Flow<UserEntity?> =
        authRepository.currentUserIdFlow.flatMapLatest { userId ->
            powerSyncDatabase.watch(
                "SELECT * FROM users WHERE id = ?",
                listOf(userId)
            ) { cursor ->
                val cols = cursor.columnNames
                val user = UserEntity(
                    id = cursor.getString(cols["id"]!!) ?: "",
                    name = cursor.getString(cols["name"]!!) ?: "Unknown",
                    email = cursor.getString(cols["email"]!!) ?: "No Email",
                    avatarUrl = cursor.getString(cols["avatar_url"]!!)
                )
                user
            }.map { 
                it.firstOrNull()
            }
        }

    suspend fun updateUser(user: UserEntity) {
        powerSyncDatabase.execute(
            "INSERT OR REPLACE INTO users (id, name, email) VALUES (?, ?, ?)",
            listOf(user.id, user.name, user.email)
        )
    }

    // Address Operations
    fun getAddresses(): Flow<List<AddressEntity>> =
        powerSyncDatabase.watch(
            "SELECT * FROM addresses WHERE user_id = ? ORDER BY last_used_timestamp DESC",
            listOf(currentUserId)
        ) { cursor ->
            val cols = cursor.columnNames
            val latIndex = cols["latitude"]
            val lonIndex = cols["longitude"]
            AddressEntity(
                addressId = cursor.getString(cols["id"]!!)!!,
                userId = cursor.getString(cols["user_id"]!!)!!,
                tag = cursor.getString(cols["tag"]!!)!!,
                fullAddress = cursor.getString(cols["full_address"]!!)!!,
                isDefault = cursor.getLong(cols["is_default"]!!) == 1L,
                lastUsedTimestamp = cursor.getLong(cols["last_used_timestamp"]!!) ?: 0L,
                latitude = latIndex?.let { cursor.getDouble(it) },
                longitude = lonIndex?.let { cursor.getDouble(it) }
            )
        }

    suspend fun addAddress(address: AddressEntity) {
        powerSyncDatabase.execute(
            "INSERT INTO addresses (id, user_id, tag, full_address, is_default, last_used_timestamp, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
            listOf(
                if (address.addressId.isNotBlank()) address.addressId else UUID.randomUUID().toString(),
                currentUserId,
                address.tag,
                address.fullAddress,
                if (address.isDefault) 1 else 0,
                address.lastUsedTimestamp,
                address.latitude,
                address.longitude
            )
        )
    }

    suspend fun deleteAddress(address: AddressEntity) {
        powerSyncDatabase.execute(
            "DELETE FROM addresses WHERE user_id = ? AND tag = ?",
            listOf(currentUserId, address.tag)
        )
    }

    suspend fun setAsDefaultAddress(addressId: String) {
        val userId = currentUserId
        powerSyncDatabase.writeTransaction { transaction ->
            transaction.execute(
                "UPDATE addresses SET is_default = 0 WHERE user_id = ?",
                listOf(userId)
            )
            transaction.execute(
                "UPDATE addresses SET is_default = 1 WHERE user_id = ? AND id = ?",
                listOf(userId, addressId)
            )
        }
    }

    // Order Operations
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getOrders(): Flow<List<OrderWithItems>> =
        authRepository.currentUserIdFlow.flatMapLatest { userId ->
            powerSyncDatabase.watch(
                "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC",
                listOf(userId)
            ) { cursor ->
                val cols = cursor.columnNames
                val orderId = cursor.getString(cols["id"]!!) ?: ""
                val priceStr = cursor.getString(cols["total_price"]!!)
                val priceDouble = priceStr?.toDoubleOrNull() ?: cursor.getDouble(cols["total_price"]!!) ?: 0.0
                val statusStr = cursor.getString(cols["status"]!!) ?: "PREPARING"
                val addressStr = cursor.getString(cols["snapshot_address"]!!) ?: ""
                val createdAtStr = cursor.getString(cols["created_at"]!!)

                val timestamp = try {
                    if (createdAtStr != null) {
                        java.time.Instant.parse(createdAtStr).toEpochMilli()
                    } else System.currentTimeMillis()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }

                OrderEntity(
                    orderId = orderId,
                    userId = userId,
                    totalPrice = priceDouble,
                    status = OrderStatus.fromString(statusStr),
                    snapshotAddress = addressStr,
                    timestamp = timestamp
                )
            }.flatMapLatest { orderList ->
                if (orderList.isEmpty()) return@flatMapLatest flowOf(emptyList())

                powerSyncDatabase.watch(
                    "SELECT * FROM order_items",
                    emptyList()
                ) { cursor ->
                    val cols = cursor.columnNames
                    val itemId = cursor.getString(cols["id"]!!) ?: ""
                    val orderId = cursor.getString(cols["order_id"]!!) ?: ""
                    val coffeeName = cursor.getString(cols["coffee_name"]!!) ?: ""
                    val quantity = cursor.getLong(cols["quantity"]!!)?.toInt() ?: 1
                    val size = cursor.getString(cols["size"]!!) ?: "M"
                    val priceStr = cursor.getString(cols["snapshot_price"]!!)
                    val price = priceStr?.toDoubleOrNull() ?: cursor.getDouble(cols["snapshot_price"]!!) ?: 0.0

                    OrderItemEntity(
                        orderItemId = itemId,
                        orderId = orderId,
                        coffeeName = coffeeName,
                        quantity = quantity,
                        size = size,
                        snapshotPrice = price
                    )
                }.map { allItems ->
                    val itemsByOrder = allItems.groupBy { it.orderId }
                    orderList.map { order ->
                        OrderWithItems(
                            order = order,
                            items = itemsByOrder[order.orderId] ?: emptyList()
                        )
                    }
                }
            }
        }

    fun getOrderById(orderId: String): Flow<OrderWithItems?> =
        getOrders().map { list -> list.find { it.order.orderId == orderId } }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        powerSyncDatabase.execute(
            "UPDATE orders SET status = ? WHERE id = ?",
            listOf(status.name, orderId)
        )
    }

    private val placeOrderMutex = Mutex()

    suspend fun placeOrder(
        address: AddressEntity,
        discountCode: String? = null
    ) = placeOrderMutex.withLock {
        val cartItems = getCartItems().first()
        if (cartItems.isEmpty()) {
            Log.w("CoffeeRepository", "placeOrder called but cart is empty. Ignoring duplicate request.")
            return@withLock
        }

        val selectedDiscount = if (!discountCode.isNull_orEmpty()) {
            discountRepository.getAllDiscounts().first().find { it.code == discountCode }
        } else null

        val pricing = PricingEngine.calculatePrice(cartItems, selectedDiscount)
        val orderId = UUID.randomUUID().toString()
        val createdAtISO = java.time.Instant.now().toString()

        // 1. Write order and order_items directly into local PowerSync database
        powerSyncDatabase.writeTransaction { transaction ->
            transaction.execute(
                "INSERT INTO orders (id, user_id, total_price, status, snapshot_address, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                listOf(
                    orderId,
                    currentUserId,
                    pricing.grandTotal.toString(),
                    OrderStatus.PREPARING.name,
                    "${address.tag}: ${address.fullAddress}",
                    createdAtISO
                )
            )

            cartItems.forEach { item ->
                transaction.execute(
                    "INSERT INTO order_items (id, order_id, coffee_name, quantity, size, snapshot_price) VALUES (?, ?, ?, ?, ?, ?)",
                    listOf(
                        UUID.randomUUID().toString(),
                        orderId,
                        item.coffee.name,
                        item.cartItem.quantity,
                        item.cartItem.size,
                        item.coffee.price.toString()
                    )
                )
            }

            // Clear cart
            transaction.execute(
                "DELETE FROM cart WHERE user_id = ?",
                listOf(currentUserId)
            )
        }

        // 2. Optional Supabase RPC trigger if available
        try {
            val itemParams = cartItems.map {
                OrderItemParams(
                    it.cartItem.coffeeId,
                    it.cartItem.quantity,
                    it.cartItem.size
                )
            }
            val params = PlaceOrderParams(
                userId = currentUserId,
                addressTag = address.tag,
                discountCode = discountCode,
                items = itemParams
            )
            supabase.postgrest.rpc("place_order", params)
            Log.d("CoffeeRepository", "Placed order successfully: $orderId")
        } catch (e: Exception) {
            Log.w("CoffeeRepository", "Remote place_order RPC failed, local write persisted: ${e.message}")
        }
    }

    // Search Operations
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> =
        powerSyncDatabase.watch(
            "SELECT * FROM search_history WHERE user_id = ? ORDER BY timestamp DESC",
            listOf(currentUserId)
        ) { cursor ->
            val cols = cursor.columnNames
            SearchHistoryEntity(
                searchId = cursor.getString(cols["id"]!!)!!,
                userId = cursor.getString(cols["user_id"]!!)!!,
                query = cursor.getString(cols["query"]!!)!!,
                resultCount = cursor.getLong(cols["result_count"]!!)!!.toInt(),
                timestamp = cursor.getLong(cols["timestamp"]!!) ?: 0L
            )
        }

    suspend fun saveSearch(query: String, resultCount: Int) {
        if (query.isBlank()) return
        powerSyncDatabase.execute(
            "INSERT OR REPLACE INTO search_history (id, user_id, query, result_count, timestamp) VALUES (?, ?, ?, ?, ?)",
            listOf(
                UUID.randomUUID().toString(),
                currentUserId,
                query,
                resultCount,
                System.currentTimeMillis()
            )
        )
    }

    suspend fun clearSearchHistory() {
        powerSyncDatabase.execute(
            "DELETE FROM search_history WHERE user_id = ?",
            listOf(currentUserId)
        )
    }
}

private fun String?.isNull_orEmpty(): Boolean = this == null || this.isEmpty()
private fun String?.isNull_orBlank(): Boolean = this == null || this.trim().isEmpty()
