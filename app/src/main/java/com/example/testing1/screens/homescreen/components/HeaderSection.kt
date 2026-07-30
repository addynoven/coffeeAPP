package com.example.testing1.screens.homescreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.testing1.R

@Composable
fun HeaderSection(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onSearchClick: () -> Unit,
    onSearchFocusChange: (Boolean) -> Unit,
    selectedLocationText: String = stringResource(R.string.default_location),
    onLocationClick: () -> Unit = {}
) {
    var deliveryMode by remember { mutableStateOf("DELIVERY") } // "DINE_IN" or "DELIVERY"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 1. Top Bar: Dine-In/Takeaway vs Delivery Toggle + Search Icon
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Dine-in vs Delivery Segmented Pill (Burger King Style)
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (deliveryMode == "DINE_IN") Color(0xFFC67C4E) else Color.Transparent)
                        .clickable { deliveryMode = "DINE_IN" }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DINE-IN/TAKEAWAY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (deliveryMode == "DINE_IN") Color.White else Color.LightGray
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (deliveryMode == "DELIVERY") Color(0xFFC67C4E) else Color.Transparent)
                        .clickable { deliveryMode = "DELIVERY" }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "DELIVERY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (deliveryMode == "DELIVERY") Color.White else Color.LightGray
                    )
                }
            }

            // Quick Search Icon Button
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f))
                    .size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Deliver To Address Bar (Burger King Dropdown Style)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onLocationClick() },
            color = Color.White.copy(alpha = 0.12f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFED9153),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Deliver to:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFED9153)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedLocationText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.change_location_desc),
                    tint = Color(0xFFED9153),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Search Bar
        SearchBar(
            searchText = searchText,
            onSearchTextChange = onSearchTextChange,
            onClearSearch = onClearSearch,
            onSearchClick = onSearchClick,
            onFocusChange = onSearchFocusChange
        )
    }
}
