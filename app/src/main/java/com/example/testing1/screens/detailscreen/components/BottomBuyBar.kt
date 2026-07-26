package com.example.testing1.screens.detailscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testing1.ui.theme.CoffeeBrown

@Composable
fun BottomBuyBar(price: Double, onAddToCartClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Price", color = Color.Gray, fontSize = 14.sp)
                Text(
                    text = "$ $price",
                    color = CoffeeBrown,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Button(
                onClick = onAddToCartClick,
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(containerColor = CoffeeBrown),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Add To Cart",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
