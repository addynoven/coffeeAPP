package com.example.testing1.screens.profilescreen

import com.example.testing1.data.local.order.OrderWithItems

data class OrderHistoryUiState(
    val orders: List<OrderWithItems> = emptyList(),
    val isLoading: Boolean = true
)
