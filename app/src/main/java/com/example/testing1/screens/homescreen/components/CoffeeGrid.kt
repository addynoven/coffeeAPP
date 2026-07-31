package com.example.testing1.screens.homescreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.models.CoffeeCategory
import com.example.testing1.util.shimmerLoading

@Composable
fun CoffeeGrid(
    items: List<CoffeeEntity>,
    isLoading: Boolean = false,
    onItemClick: (CoffeeEntity) -> Unit,
    onToggleFavorite: (CoffeeEntity) -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val columnCount = when {
        screenWidth < 600.dp -> 2       // Compact (Phone)
        screenWidth < 840.dp -> 3       // Medium (Foldable / Small Tablet)
        else -> 4                       // Expanded (Tablet)
    }

    if (isLoading) {
        CoffeeGridSkeleton(columnCount = columnCount)
    } else {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items.chunked(columnCount).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    rowItems.forEach { item ->
                        CoffeeCard(
                            item = item,
                            modifier = Modifier.weight(1f),
                            onClick = { onItemClick(item) },
                            onToggleFavorite = { onToggleFavorite(item) }
                        )
                    }
                    repeat(columnCount - rowItems.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun CoffeeGridSkeleton(columnCount: Int = 2) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        repeat(3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                repeat(columnCount) {
                    CoffeeCardSkeleton(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun CoffeeCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .shimmerLoading()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .shimmerLoading()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .shimmerLoading()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .shimmerLoading()
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray.copy(alpha = 0.3f))
                        .shimmerLoading()
                )
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeGridPreview() {

    CoffeeGrid(
        items = listOf(
            CoffeeEntity(
                id = "0",
                name = "Espresso",
                description = "Strong and rich",
                category = CoffeeCategory.Espresso,
                price = 3.8,
                imageUrl = ""
            ),
            CoffeeEntity(
                id = "1",
                name = "Latte",
                description = "Smooth and creamy",
                category = CoffeeCategory.Latte,
                price = 4.5,
                imageUrl = ""
            ),
            CoffeeEntity(
                id = "2",
                name = "Cappuccino",
                description = "With thick foam",
                category = CoffeeCategory.Cappuccino,
                price = 4.8,
                imageUrl = ""
            ),
            CoffeeEntity(
                id = "3",
                name = "Macchiato",
                description = "With a dash of milk",
                category = CoffeeCategory.Macchiato,
                price = 4.2,
                imageUrl = ""
            ),
        ),
        onItemClick = {},
        onToggleFavorite = {}
    )
}
