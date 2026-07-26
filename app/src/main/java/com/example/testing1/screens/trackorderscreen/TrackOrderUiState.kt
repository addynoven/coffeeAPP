package com.example.testing1.screens.trackorderscreen

import com.example.testing1.data.local.order.OrderWithItems

data class TrackOrderUiState(
    val orderWithItems: OrderWithItems? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
