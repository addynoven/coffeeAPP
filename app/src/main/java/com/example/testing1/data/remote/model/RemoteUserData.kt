package com.example.testing1.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteFavorite(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("coffee_id")
    val coffeeId: String
)

@Serializable
data class RemoteCart(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("coffee_id")
    val coffeeId: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("size")
    val size: String
)

@Serializable
data class RemoteAddress(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("tag")
    val tag: String,
    @SerialName("full_address")
    val fullAddress: String,
    @SerialName("is_default")
    val isDefault: Boolean,
    @SerialName("last_used_timestamp")
    val lastUsedTimestamp: Long
)

@Serializable
data class RemoteOrder(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("total_price")
    val totalPrice: Double,
    @SerialName("status")
    val status: String,
    @SerialName("snapshot_address")
    val snapshotAddress: String,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class RemoteOrderItem(
    @SerialName("id")
    val id: String? = null,
    @SerialName("order_id")
    val orderId: String,
    @SerialName("coffee_name")
    val coffeeName: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("size")
    val size: String,
    @SerialName("snapshot_price")
    val snapshotPrice: Double
)

@Serializable
data class RemoteSearchHistory(
    @SerialName("id")
    val id: String? = null,
    @SerialName("user_id")
    val userId: String,
    @SerialName("query")
    val query: String,
    @SerialName("result_count")
    val resultCount: Int,
    @SerialName("timestamp")
    val timestamp: Long
)

@Serializable
data class RemoteDiscount(
    @SerialName("code")
    val code: String,
    @SerialName("description")
    val description: String,
    @SerialName("type")
    val type: String,
    @SerialName("value")
    val value: Double,
    @SerialName("min_order_amount")
    val minOrderAmount: Double,
    @SerialName("max_discount_amount")
    val maxDiscountAmount: Double? = null
)

@Serializable
data class OrderItemParams(
    @SerialName("coffee_id")
    val coffeeId: String,
    @SerialName("quantity")
    val quantity: Int,
    @SerialName("size")
    val size: String
)

@Serializable
data class PlaceOrderParams(
    @SerialName("p_user_id")
    val userId: String,
    @SerialName("p_address_tag")
    val addressTag: String,
    @SerialName("p_discount_code")
    val discountCode: String?,
    @SerialName("p_items")
    val items: List<OrderItemParams>
)
