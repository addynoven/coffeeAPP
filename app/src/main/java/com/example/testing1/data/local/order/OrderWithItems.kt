package com.example.testing1.data.local.order

data class OrderWithItems(
    val order: OrderEntity,
    val items: List<OrderItemEntity>
)
