package com.example.testing1.screens.homescreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.models.CoffeeCategory


@Composable
fun CoffeeGrid(
    items: List<CoffeeEntity>, 
    onItemClick: (CoffeeEntity) -> Unit,
    onToggleFavorite: (CoffeeEntity) -> Unit
) {
    Column(
        modifier = Modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items.chunked(2).forEach { rowItems ->
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
                if (rowItems.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
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
                id = 0,
                name = "Espresso",
                description = "Strong and rich",
                category = CoffeeCategory.Espresso,
                price = 3.8,
                imageUrl = ""
            ),
            CoffeeEntity(
                id = 1,
                name = "Latte",
                description = "Smooth and creamy",
                category = CoffeeCategory.Latte,
                price = 4.5,
                imageUrl = ""
            ),
            CoffeeEntity(
                id = 2,
                name = "Cappuccino",
                description = "With thick foam",
                category = CoffeeCategory.Cappuccino,
                price = 4.8,
                imageUrl = ""
            ),
            CoffeeEntity(
                id = 3,
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
