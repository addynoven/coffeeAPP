package com.example.testing1.screens.homescreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import com.example.testing1.R
import com.example.testing1.data.local.coffee.CoffeeEntity
import com.example.testing1.screens.ui_components.AnimatedFavoriteButton
import com.example.testing1.util.LocalCloudinaryHelper
import com.example.testing1.util.sharedElementExt
import com.example.testing1.util.shimmerLoading

@Composable
fun CoffeeCard(
    item: CoffeeEntity,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val cloudinaryHelper = LocalCloudinaryHelper.current
    val language = LocalConfiguration.current.locales[0].language

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box {
                SubcomposeAsyncImage(
                    model = cloudinaryHelper.optimize(item.imageUrl, width = 350),
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .shimmerLoading()
                        )
                    },
                    error = {
                        Icon(
                            painter = painterResource(R.drawable.default_bean),
                            contentDescription = null,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.25f)
                        .clip(RoundedCornerShape(12.dp))
                        .sharedElementExt("coffee-img-${item.id}")
                )

                // Top Left Special Roastery Tag (Burger King Style)
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                    color = Color(0xFFD97706),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "SIGNATURE",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Top Right Favorite Heart Button
                AnimatedFavoriteButton(
                    isFavorite = item.isFavorite,
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(
                            Color.Black.copy(alpha = 0.3f),
                            CircleShape
                        ),
                    size = 28
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.getLocalizedName(language).uppercase(),
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.getLocalizedDescription(language),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Row: Price + ADD + Pill Button (Burger King Style)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ${item.price}",
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // ADD + Pill Button with Accent Border
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.height(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFD97706)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "ADD +",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun CoffeeCardPreview() {
    CoffeeCard(
        item = CoffeeEntity(
            id = "0",
            name = "Espresso",
            description = "Strong and rich",
            price = 3.8,
            category = com.example.testing1.models.CoffeeCategory.Espresso,
            imageUrl = ""
        ),
        onClick = {},
        onToggleFavorite = {}
    )
}
