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
import com.example.testing1.models.OrderStatus
import com.example.testing1.data.local.search.SearchDao
import com.example.testing1.data.local.search.SearchHistoryEntity
import com.example.testing1.data.local.user.UserDao
import com.example.testing1.data.local.user.UserEntity
import com.example.testing1.data.remote.model.*
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
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
    private val settingsRepository: SettingsRepository,
    private val discountRepository: DiscountRepository,
    private val supabase: SupabaseClient
) {
    companion object {
        const val CURRENT_USER_ID = "dev_user_123"
    }

    fun getAllCoffee(): Flow<List<CoffeeEntity>> =
        coffeeDao.getAllCoffee()

    suspend fun getCoffeeById(id: Int): CoffeeEntity? =
        coffeeDao.getCoffeeById(id)

    suspend fun insertAll(coffees: List<CoffeeEntity>) {
        coffeeDao.insertAll(coffees)
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean) {
        try {
            if (isFavorite) {
                supabase.from("favorites").insert(RemoteFavorite(userId = CURRENT_USER_ID, coffeeId = id))
            } else {
                supabase.from("favorites").delete {
                    filter {
                        eq("user_id", CURRENT_USER_ID)
                        eq("coffee_id", id)
                    }
                }
            }
        } catch (e: Exception) {
            println("Push Favorite Error: ${e.message}")
        }
        coffeeDao.updateFavoriteStatus(id, isFavorite)
    }

    fun getFavoriteCoffee(): Flow<List<CoffeeEntity>> =
        coffeeDao.getFavoriteCoffee()

    // Sync Operations
    suspend fun refreshCoffee() {
        try {
            val localCount = coffeeDao.getCoffeeCount()
            val lastSync = if (localCount == 0) "1970-01-01T00:00:00Z" else settingsRepository.getLastCoffeeSync().first()

            // A. Fetch items changed since last sync
            val remoteCoffees = supabase.from("coffee")
                .select {
                    filter {
                        gt("updated_at", lastSync)
                    }
                }
                .decodeList<RemoteCoffee>()

            if (remoteCoffees.isEmpty()) {
                println("Sync Catalog: No new updates found.")
            } else {
                println("Sync Catalog: Found ${remoteCoffees.size} updates.")

                // B. Get current local state to preserve favorites
                val localCoffees =
                    coffeeDao.getAllCoffee().first().associateBy { it.id }

                // C. Merge Remote data with Local state
                val entities = remoteCoffees.map { remote ->
                    val localItem = localCoffees[remote.id]
                    remote.toEntity(isFavorite = localItem?.isFavorite ?: false)
                }

                // D. Save to Local Room DB
                coffeeDao.insertAll(entities)

                // E. Save the latest timestamp from the received items
                val latestTimestamp = remoteCoffees.maxBy { it.updatedAt }.updatedAt
                settingsRepository.setLastCoffeeSync(latestTimestamp)

                println("Sync Catalog: Completed. Last sync set to $latestTimestamp")
            }

            // Sync Discounts
            discountRepository.refreshDiscounts()

            // F. Sync User Data (Favorites, Cart, etc.)
            syncUserData()

        } catch (e: Exception) {
            println("Sync Error: ${e.message}")
            e.printStackTrace()
        }
    }

    private suspend fun syncUserData() {
        try {
            println("Sync User Data: Starting for $CURRENT_USER_ID")

            // 1. Sync Favorites
            val remoteFavorites = supabase.from("favorites")
                .select { filter { eq("user_id", CURRENT_USER_ID) } }
                .decodeList<RemoteFavorite>()

            val favoriteIds = remoteFavorites.map { it.coffeeId }.toSet()
            val localCoffees = coffeeDao.getAllCoffee().first()
            localCoffees.forEach { coffee ->
                val shouldBeFavorite = favoriteIds.contains(coffee.id)
                if (coffee.isFavorite != shouldBeFavorite) {
                    coffeeDao.updateFavoriteStatus(coffee.id, shouldBeFavorite)
                }
            }

            // 2. Sync Cart
            val remoteCart = supabase.from("cart")
                .select { filter { eq("user_id", CURRENT_USER_ID) } }
                .decodeList<RemoteCart>()

            cartDao.clearCart(CURRENT_USER_ID)
            val cartEntities = remoteCart.map { remote ->
                CartEntity(
                    userId = CURRENT_USER_ID,
                    coffeeId = remote.coffeeId,
                    quantity = remote.quantity,
                    size = remote.size
                )
            }
            cartEntities.forEach { cartDao.addToCart(it) }

            // 3. Sync Addresses
            val remoteAddresses = supabase.from("addresses")
                .select { filter { eq("user_id", CURRENT_USER_ID) } }
                .decodeList<RemoteAddress>()

            addressDao.clearAddresses(CURRENT_USER_ID)
            remoteAddresses.forEach { remote ->
                addressDao.insertAddress(
                    AddressEntity(
                        userId = CURRENT_USER_ID,
                        tag = remote.tag,
                        fullAddress = remote.fullAddress,
                        isDefault = remote.isDefault,
                        lastUsedTimestamp = remote.lastUsedTimestamp
                    )
                )
            }

            // 4. Sync Search History
            val remoteSearchHistory = supabase.from("search_history")
                .select { filter { eq("user_id", CURRENT_USER_ID) } }
                .decodeList<RemoteSearchHistory>()

            searchDao.clearHistory(CURRENT_USER_ID)
            remoteSearchHistory.forEach { remote ->
                searchDao.insertSearch(
                    SearchHistoryEntity(
                        userId = CURRENT_USER_ID,
                        query = remote.query,
                        resultCount = remote.resultCount,
                        timestamp = remote.timestamp
                    )
                )
            }

            println("Sync User Data: Completed")
        } catch (e: Exception) {
            println("Sync User Data Error: ${e.message}")
        }
    }

    private fun RemoteCoffee.toEntity(isFavorite: Boolean) = CoffeeEntity(
        id = id,
        name = name,
        description = description,
        category = category,
        price = price,
        imageUrl = imageUrl,
        isFavorite = isFavorite,
        nameJa = nameJa,
        descriptionJa = descriptionJa,
        nameDe = nameDe,
        descriptionDe = descriptionDe,
        nameRu = nameRu,
        descriptionRu = descriptionRu,
        namePt = namePt,
        descriptionPt = descriptionPt,
        nameFr = nameFr,
        descriptionFr = descriptionFr,
        nameAr = nameAr,
        descriptionAr = descriptionAr,
        nameEs = nameEs,
        descriptionEs = descriptionEs,
        nameZh = nameZh,
        descriptionZh = descriptionZh,
        nameIt = nameIt,
        descriptionIt = descriptionIt
    )

    // Cart Operations
    fun getCartItems(): Flow<List<CartItemWithCoffee>> =
        cartDao.getCartItems(CURRENT_USER_ID)

    suspend fun addToCart(coffeeId: Int, size: String, quantity: Int = 1) {
        try {
            val remoteCart = RemoteCart(userId = CURRENT_USER_ID, coffeeId = coffeeId, quantity = quantity, size = size)
            supabase.from("cart").upsert(remoteCart)
        } catch (e: Exception) {
            println("Push Cart Error: ${e.message}")
        }

        val existingItem = cartDao.getCartItemByCoffeeAndSize(CURRENT_USER_ID, coffeeId, size)
        if (existingItem != null) {
            cartDao.updateCartItem(existingItem.copy(quantity = existingItem.quantity + quantity))
        } else {
            cartDao.addToCart(
                CartEntity(
                    userId = CURRENT_USER_ID,
                    coffeeId = coffeeId,
                    quantity = quantity,
                    size = size
                )
            )
        }
    }

    suspend fun updateCartQuantity(cartItem: CartEntity, newQuantity: Int) {
        try {
            if (newQuantity <= 0) {
                supabase.from("cart").delete {
                    filter {
                        eq("user_id", CURRENT_USER_ID)
                        eq("coffee_id", cartItem.coffeeId)
                        eq("size", cartItem.size)
                    }
                }
            } else {
                val remoteCart = RemoteCart(
                    userId = CURRENT_USER_ID,
                    coffeeId = cartItem.coffeeId,
                    quantity = newQuantity,
                    size = cartItem.size
                )
                supabase.from("cart").upsert(remoteCart)
            }
        } catch (e: Exception) {
            println("Update Cart Error: ${e.message}")
        }

        if (newQuantity <= 0) {
            cartDao.removeFromCart(cartItem)
        } else {
            cartDao.updateCartItem(cartItem.copy(quantity = newQuantity))
        }
    }

    suspend fun removeFromCart(cartItem: CartEntity) {
        try {
            supabase.from("cart").delete {
                filter {
                    eq("user_id", CURRENT_USER_ID)
                    eq("coffee_id", cartItem.coffeeId)
                    eq("size", cartItem.size)
                }
            }
        } catch (e: Exception) {
            println("Remove Cart Error: ${e.message}")
        }
        cartDao.removeFromCart(cartItem)
    }

    // User Operations
    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun updateUser(user: UserEntity) {
        userDao.upsertUser(user)
    }

    // Address Operations
    fun getAddresses(): Flow<List<AddressEntity>> = addressDao.getAddresses(CURRENT_USER_ID)

    suspend fun addAddress(address: AddressEntity) {
        try {
            supabase.from("addresses").insert(
                RemoteAddress(
                    userId = CURRENT_USER_ID,
                    tag = address.tag,
                    fullAddress = address.fullAddress,
                    isDefault = address.isDefault,
                    lastUsedTimestamp = address.lastUsedTimestamp
                )
            )
        } catch (e: Exception) {}
        addressDao.insertAddress(address.copy(userId = CURRENT_USER_ID))
    }

    suspend fun deleteAddress(address: AddressEntity) {
        try {
            supabase.from("addresses").delete {
                filter {
                    eq("user_id", CURRENT_USER_ID)
                    eq("tag", address.tag)
                }
            }
        } catch (e: Exception) {}
        addressDao.deleteAddress(address)
    }

    suspend fun setAsDefaultAddress(addressId: Int) {
        addressDao.setAsDefault(CURRENT_USER_ID, addressId)
    }

    // Order Operations
    fun getOrders(): Flow<List<OrderWithItems>> = orderDao.getOrders(CURRENT_USER_ID)

    fun getOrderById(orderId: Int): Flow<OrderWithItems?> = orderDao.getOrderById(orderId)

    suspend fun updateOrderStatus(orderId: Int, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status)
        // Optionally update remote as well
        try {
            supabase.from("orders").update(mapOf("status" to status.name)) {
                filter { eq("id", orderId) }
            }
        } catch (e: Exception) {}
    }

    suspend fun placeOrder(address: AddressEntity, discountCode: String? = null) {
        addressDao.updateAddress(address.copy(lastUsedTimestamp = System.currentTimeMillis()))

        val cartItems = cartDao.getCartItems(CURRENT_USER_ID).first()
        if (cartItems.isEmpty()) return

        try {
            // 1. Prepare Params for Backend RPC
            val itemParams = cartItems.map { 
                OrderItemParams(it.cartItem.coffeeId, it.cartItem.quantity, it.cartItem.size)
            }
            val params = PlaceOrderParams(
                userId = CURRENT_USER_ID,
                addressTag = address.tag,
                discountCode = discountCode,
                items = itemParams
            )

            // 2. Call Supabase RPC (Authoritative Pricing)
            val remoteOrder = supabase.postgrest.rpc("place_order", params).decodeSingle<RemoteOrder>()
            
            // 3. Save to Local DB using backend result
            val orderEntity = OrderEntity(
                userId = CURRENT_USER_ID,
                totalPrice = remoteOrder.totalPrice,
                status = OrderStatus.fromString(remoteOrder.status),
                snapshotAddress = remoteOrder.snapshotAddress
            )
            val localOrderId = orderDao.insertOrder(orderEntity).toInt()

            val orderItems = cartItems.map { item ->
                OrderItemEntity(
                    orderId = localOrderId,
                    coffeeName = item.coffee.name,
                    quantity = item.cartItem.quantity,
                    size = item.cartItem.size,
                    snapshotPrice = item.coffee.price // Note: Backend also has its own price truth
                )
            }
            orderDao.insertOrderItems(orderItems)

            // 4. Clear Local & Remote Cart
            cartItems.forEach { cartDao.removeFromCart(it.cartItem) }
            supabase.from("cart").delete { filter { eq("user_id", CURRENT_USER_ID) } }

        } catch (e: Exception) {
            println("Place Order Secure Error: ${e.message}")
            e.printStackTrace()
            // Fallback or rethrow UI error
        }
    }

    // Search Operations
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>> =
        searchDao.getRecentSearches(CURRENT_USER_ID)

    suspend fun saveSearch(query: String, resultCount: Int) {
        if (query.isBlank()) return
        
        try {
            supabase.from("search_history").insert(
                RemoteSearchHistory(
                    userId = CURRENT_USER_ID,
                    query = query,
                    resultCount = resultCount,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {}

        searchDao.insertSearch(
            SearchHistoryEntity(
                userId = CURRENT_USER_ID,
                query = query,
                resultCount = resultCount
            )
        )
        searchDao.deleteOldSearches(CURRENT_USER_ID)
    }

    suspend fun clearSearchHistory() {
        try {
            supabase.from("search_history").delete { filter { eq("user_id", CURRENT_USER_ID) } }
        } catch (e: Exception) {}
        searchDao.clearHistory(CURRENT_USER_ID)
    }
}
