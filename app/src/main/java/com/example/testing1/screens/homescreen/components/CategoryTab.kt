package com.example.testing1.screens.homescreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryTab(title: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = title.uppercase(),
            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
            fontSize = 13.sp,
            color = if (isSelected) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Active Underline Indicator Bar (Burger King Style)
        Box(
            modifier = Modifier
                .width(if (isSelected) 36.dp else 0.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) Color(0xFFD97706) else Color.Transparent)
        )
    }
}

@Preview
@Composable
private fun CategoryTabPreview() {
    CategoryTab(
        title = "All Coffee",
        isSelected = true,
        onClick = {}
    )
}
