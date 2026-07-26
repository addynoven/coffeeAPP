package com.example.testing1.screens.homescreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.testing1.models.CoffeeCategory

@Composable
fun CategoryRow(
    categories: List<CoffeeCategory>,
    selectedCategory: CoffeeCategory,
    onCategorySelected: (CoffeeCategory) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryTab(
                title = category.displayName,
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Preview
@Composable
private fun CategoryRowPreview() {
    CategoryRow(
        categories = CoffeeCategory.entries,
        selectedCategory = CoffeeCategory.AllCoffee,
        onCategorySelected = {}
    )
}