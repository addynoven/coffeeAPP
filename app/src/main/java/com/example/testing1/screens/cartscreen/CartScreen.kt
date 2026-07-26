package com.example.testing1.screens.cartscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.testing1.R
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.screens.cartscreen.components.BottomOrderBar
import com.example.testing1.screens.cartscreen.components.OrderItemCard
import com.example.testing1.screens.cartscreen.components.PaymentRow
import com.example.testing1.screens.ui_components.MyBottomBar
import com.example.testing1.util.LottieAnimations

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    CartScreen(
        uiState = CartUiState(),
        onBackClick = {},
        onHomeClick = {},
        onFavoriteClick = {},
        onCartClick = {},
        onProfileClick = {},
        onUpdateQuantity = { _, _ -> },
        onAddressSelected = {},
        onPlaceOrder = {},
        onDismissSuccess = {}
    )
}

@Composable
fun CartRoute(
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CartScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onFavoriteClick = onFavoriteClick,
        onCartClick = onCartClick,
        onProfileClick = onProfileClick,
        onUpdateQuantity = viewModel::updateQuantity,
        onAddressSelected = viewModel::onAddressSelected,
        onPlaceOrder = viewModel::placeOrder,
        onDismissSuccess = viewModel::dismissOrderSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    uiState: CartUiState,
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUpdateQuantity: (CartEntity, Int) -> Unit,
    onAddressSelected: (AddressEntity) -> Unit,
    onPlaceOrder: () -> Unit,
    onDismissSuccess: () -> Unit
) {
    if (uiState.isOrderPlaced) {
        OrderSuccessDialog(onDismiss = onDismissSuccess)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.order_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column {
                BottomOrderBar(
                    totalPrice = uiState.totalPrice + uiState.deliveryFee,
                    onPlaceOrder = onPlaceOrder
                )
                MyBottomBar(
                    selectedTab = "Cart",
                    onTabSelected = { tab ->
                        if (tab == "Home") onHomeClick()
                        if (tab == "Favorite") onFavoriteClick()
                        if (tab == "Cart") onCartClick()
                        if (tab == "Profile") onProfileClick()
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = stringResource(R.string.delivery_address_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (uiState.addresses.isEmpty()) {
                    Text(stringResource(R.string.no_addresses_error), color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.addresses) { address ->
                            AddressChip(
                                address = address,
                                isSelected = uiState.selectedAddress?.addressId == address.addressId,
                                onClick = { onAddressSelected(address) }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.selected_label, uiState.selectedAddress?.fullAddress ?: "None"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            items(uiState.cartItems) { item ->
                OrderItemCard(
                    item = item,
                    onIncreaseQuantity = { onUpdateQuantity(item.cartItem, item.cartItem.quantity + 1) },
                    onDecreaseQuantity = { onUpdateQuantity(item.cartItem, item.cartItem.quantity - 1) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.payment_summary_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(12.dp))
                PaymentRow(stringResource(R.string.price_label), "$ ${"%.2f".format(uiState.totalPrice)}")
                PaymentRow(stringResource(R.string.delivery_fee_label), "$ ${"%.2f".format(uiState.deliveryFee)}")
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun AddressChip(
    address: AddressEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.LocationOn, 
                contentDescription = null, 
                modifier = Modifier.size(14.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = address.tag,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun OrderSuccessDialog(onDismiss: () -> Unit) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(LottieAnimations.ADD_TO_CART))

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Box(
                modifier = Modifier.size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                if (composition == null) {
                    Icon(
                        Icons.Default.CheckCircle, 
                        contentDescription = null, 
                        modifier = Modifier.size(80.dp), 
                        tint = Color(0xFF4CAF50)
                    )
                } else {
                    LottieAnimation(
                        composition = composition,
                        iterations = LottieConstants.IterateForever
                    )
                }
            }
        },
        title = { Text(stringResource(R.string.order_placed_title)) },
        text = { Text(stringResource(R.string.order_placed_desc)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.awesome_button)) }
        }
    )
}
