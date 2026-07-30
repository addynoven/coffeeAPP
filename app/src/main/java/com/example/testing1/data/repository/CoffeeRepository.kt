package com.example.testing1.data.repository

import android.util.Log
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.data.local.cart.CartItemWithCoffee
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.data.local.order.OrderEntity
import com.example.testing1.data.local.order.OrderWithItems
import com.example.testing1.data.local.search.SearchHistoryEntity
import com.example.testing1.data.local.user.UserEntity
import com.example.testing1.data.remote.model.OrderItemParams
import com.example.testing1.data.remote.model.PlaceOrderParams
import com.example.testing1.data.remote.model.RemoteOrder
import com.example.testing1.models.CoffeeCategory
import com.example.testing1.models.OrderStatus
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
import kotlinx.coroutines.flow.map
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
            CoffeeEntity(
                id = cursor.getString(cols["id"]!!)!!,
                name = cursor.getString(cols["name"]!!)!!,
                description = cursor.getString(cols["description"]!!)!!,
                category = CoffeeCategory.fromString(cursor.getString(cols["category"]!!)!!),
                price = cursor.getDouble(cols["price"]!!)!!,
                imageUrl = cursor.getString(cols["image_url"]!!)!!,
                isFavorite = cursor.getBoolean(cols["is_favorite"]!!) ?: false,
                nameJa = cursor.getString(cols["name_ja"]!!),
                descriptionJa = cursor.getString(cols["description_ja"]!!),
                nameDe = cursor.getString(cols["name_de"]!!),
                descriptionDe = cursor.getString(cols["description_de"]!!),
                nameRu = cursor.getString(cols["name_ru"]!!),
                descriptionRu = cursor.getString(cols["description_ru"]!!),
                namePt = cursor.getString(cols["name_pt"]!!),
                descriptionPt = cursor.getString(cols["description_pt"]!!),
                nameFr = cursor.getString(cols["name_fr"]!!),
                descriptionFr = cursor.getString(cols["description_fr"]!!),
                nameAr = cursor.getString(cols["name_ar"]!!),
                descriptionAr = cursor.getString(cols["description_ar"]!!),
                nameEs = cursor.getString(cols["name_es"]!!),
                descriptionEs = cursor.getString(cols["description_es"]!!),
                nameZh = cursor.getString(cols["name_zh"]!!),
                descriptionZh = cursor.getString(cols["description_zh"]!!),
                nameIt = cursor.getString(cols["name_it"]!!),
                descriptionIt = cursor.getString(cols["description_it"]!!)
            )
        } catch (e: Exception) {
            Log.e("CoffeeRepository", "Error mapping coffee: ${e.message}", e)
            throw e
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getAllCoffee(): Flow<List<CoffeeEntity>> =
        authRepository.currentUserIdFlow.flatMapLatest { userId ->
            Log.d("CoffeeRepository", "getAllCoffee() flatMap triggered for User: $userId")
            powerSyncDatabase.watch(
                "SELECT c.*, f.coffee_id IS NOT NULL as is_favorite FROM coffee c LEFT JOIN favorites f ON c.id = f.coffee_id AND f.user_id = ?",
                listOf(userId)
            ) {
                Log.d("CoffeeRepository", "SQL Watch Triggered - Mapping Row...")
                val entity = mapCoffee(it)
                Log.d(
                    "CoffeeRepository",
                    "Fetched coffee: ${entity.name} (${entity.id})"
                )
                entity
            }
        }.map {
            Log.d("CoffeeRepository", "Repository emitting ${it.size} coffee items to UI")
            it
        }

    suspend fun getCoffeeById(id: String): CoffeeEntity? =
        powerSyncDatabase.getOptional(
            "SELECT c.*, f.coffee_id IS NOT NULL as is_favorite FROM coffee c LEFT JOIN favorites f ON c.id = f.coffee_id AND f.user_id = ? WHERE c.id = ?",
            listOf(currentUserId, id)
        ) { mapCoffee(it) }

    suspend fun insertAll(coffees: List<CoffeeEntity>) {
        // Redundant with PowerSync
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        if (isFavorite) {
            powerSyncDatabase.execute(
                "INSERT OR REPLACE INTO favorites (id, user_id, coffee_id) VALUES (?, ?, ?)",
                listOf(
                    UUID.randomUUID().toString(),
                    currentUserId,
                    id
                )
            )
        } else {
            powerSyncDatabase.execute(
                "DELETE FROM favorites WHERE user_id = ? AND coffee_id = ?",
                listOf(currentUserId, id)
            )
        }
    }

    fun getFavoriteCoffee(): Flow<List<CoffeeEntity>> =
        powerSyncDatabase.watch(
            "SELECT c.*, 1 as is_favorite FROM coffee c JOIN favorites f ON c.id = f.coffee_id WHERE f.user_id = ?",
            listOf(currentUserId)
        ) { mapCoffee(it) }

    suspend fun refreshCoffee() {
        discountRepository.refreshDiscounts()
    }

    // Cart Operations
    fun getCartItems(): Flow<List<CartItemWithCoffee>> =
        powerSyncDatabase.watch(
            "SELECT ct.*, c.name, c.price, c.image_url FROM cart ct JOIN coffee c ON ct.coffee_id = c.id WHERE ct.user_id = ?",
            listOf(currentUserId)
        ) { cursor ->
            val cols = cursor.columnNames
            val cartItem = CartEntity(
                cartId = cursor.getString(cols["id"]!!)!!,
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
                category = CoffeeCategory.Espresso // Partial object for UI
            )
            CartItemWithCoffee(cartItem, coffee)
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
                Log.d("CoffeeRepository", "getUser() successfully mapped: $user")
                user
            }.map { 
                val user = it.firstOrNull()
                Log.d("CoffeeRepository", "getUser() flow emitted for user $userId: $user")
                user
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
        // Implementation omitted for brevity in migration
    }

    // Order Operations
    fun getOrders(): Flow<List<OrderWithItems>> =
        powerSyncDatabase.watch(
            "SELECT * FROM orders WHERE user_id = ? ORDER BY created_at DESC",
            listOf(currentUserId)
        ) { cursor ->
            val cols = cursor.columnNames
            val order = OrderEntity(
                orderId = cursor.getString(cols["id"]!!)!!,
                userId = cursor.getString(cols["user_id"]!!)!!,
                totalPrice = cursor.getDouble(cols["total_price"]!!)!!,
                status = OrderStatus.fromString(cursor.getString(cols["status"]!!)!!),
                snapshotAddress = cursor.getString(cols["snapshot_address"]!!)!!,
                timestamp = 0L
            )
            OrderWithItems(order, emptyList())
        }

    fun getOrderById(orderId: String): Flow<OrderWithItems?> =
        powerSyncDatabase.watch(
            "SELECT * FROM orders WHERE id = ?",
            listOf(orderId)
        ) { cursor ->
            val cols = cursor.columnNames
            OrderWithItems(
                OrderEntity(
                    orderId = cursor.getString(cols["id"]!!)!!,
                    userId = cursor.getString(cols["user_id"]!!)!!,
                    totalPrice = cursor.getDouble(cols["total_price"]!!)!!,
                    status = OrderStatus.fromString(cursor.getString(cols["status"]!!)!!),
                    snapshotAddress = cursor.getString(cols["snapshot_address"]!!)!!,
                    timestamp = 0L
                ),
                emptyList()
            )
        }.map { it.firstOrNull() }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        powerSyncDatabase.execute(
            "UPDATE orders SET status = ? WHERE id = ?",
            listOf(status.name, orderId)
        )
    }

    suspend fun placeOrder(
        address: AddressEntity,
        discountCode: String? = null
    ) {
        val cartItems = getCartItems().first()
        if (cartItems.isEmpty()) return

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
                .decodeSingle<RemoteOrder>()

            cartItems.forEach { removeFromCart(it.cartItem) }
        } catch (e: Exception) {
            e.printStackTrace()
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
