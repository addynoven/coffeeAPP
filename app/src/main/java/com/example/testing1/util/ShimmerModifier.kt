package com.example.testing1.util

import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import com.valentinilk.shimmer.shimmer

/**
 * A reusable modifier to apply a shimmer effect to any composable.
 * This is useful for creating "Skeleton" loading states.
 */
fun Modifier.shimmerLoading(
    enabled: Boolean = true
): Modifier = composed {
    if (enabled) {
        this
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            .shimmer()
    } else {
        this
    }
}
