package com.example.testing1.screens.cartscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.airbnb.lottie.compose.*
import com.example.testing1.R
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.cart.CartEntity
import com.example.testing1.screens.cartscreen.components.BottomOrderBar
import com.example.testing1.screens.cartscreen.components.OrderItemCard
import com.example.testing1.screens.cartscreen.components.PaymentRow
import com.example.testing1.screens.ui_components.EmptyStateContent
import com.example.testing1.screens.ui_components.MyBottomBar
import com.example.testing1.util.LottieAnimations
import com.example.testing1.util.UiEvent

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
        onPromoCodeChange = {},
        onClearPromoCode = {},
        onDiscountSelected = {},
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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current

    CartScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onHomeClick = onHomeClick,
        onFavoriteClick = onFavoriteClick,
        onCartClick = onCartClick,
        onProfileClick = onProfileClick,
        onUpdateQuantity = viewModel::updateQuantity,
        onAddressSelected = viewModel::onAddressSelected,
        onPromoCodeChange = viewModel::onPromoCodeChange,
        onClearPromoCode = viewModel::onClearPromoCode,
        onDiscountSelected = viewModel::onDiscountSelected,
        onPlaceOrder = {
            val activity = context as? android.app.Activity
            if (activity != null) {
                viewModel.startPaymentCheckout(activity)
            } else {
                viewModel.placeOrderDirectlyWithoutPayment()
            }
        },
        onDismissSuccess = viewModel::dismissOrderSuccess
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    uiState: CartUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackClick: () -> Unit,
    onHomeClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onUpdateQuantity: (CartEntity, Int) -> Unit,
    onAddressSelected: (AddressEntity) -> Unit,
    onPromoCodeChange: (String) -> Unit,
    onClearPromoCode: () -> Unit,
    onDiscountSelected: (com.example.testing1.models.Discount?) -> Unit,
    onPlaceOrder: () -> Unit,
    onDismissSuccess: () -> Unit
) {
    if (uiState.isOrderPlaced) {
        OrderSuccessDialog(onDismiss = onDismissSuccess)
    }

    var showAddressSelector by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                if (uiState.cartItems.isNotEmpty()) {
                    BottomOrderBar(
                        totalPrice = uiState.totalPrice,
                        onPlaceOrder = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPlaceOrder()
                        }
                    )
                }
                MyBottomBar(
                    selectedTabId = "cart",
                    onTabSelected = { tabId ->
                        when (tabId) {
                            "home" -> onHomeClick()
                            "favorite" -> onFavoriteClick()
                            "profile" -> onProfileClick()
                        }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        if (!uiState.isLoading && uiState.cartItems.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding)) {
                EmptyStateContent(
                    icon = Icons.Default.ShoppingCart,
                    title = "Your cart is empty",
                    description = "Looks like you haven't added any coffee yet. Start exploring our delicious beans!",
                    actionLabel = "Browse Coffee",
                    onActionClick = onHomeClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Delivery Address",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = if (showAddressSelector) "Done" else "Edit",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                showAddressSelector = !showAddressSelector
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (showAddressSelector) {
                        if (uiState.addresses.isEmpty()) {
                            Text(
                                stringResource(R.string.no_addresses_error),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp
                            )
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
                    } else {
                        // Show just the selected address compactly
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = uiState.selectedAddress?.let { "${it.tag}: ${it.fullAddress}" }
                                    ?: "No address selected",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }

                items(uiState.cartItems) { item ->
                    OrderItemCard(
                        item = item,
                        onIncreaseQuantity = {
                            onUpdateQuantity(
                                item.cartItem,
                                item.cartItem.quantity + 1
                            )
                        },
                        onDecreaseQuantity = {
                            onUpdateQuantity(
                                item.cartItem,
                                item.cartItem.quantity - 1
                            )
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Discounts & Promo",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Promo Code Entry (Manual)
                    val clipboardManager = LocalClipboardManager.current
                    val hasError = uiState.promoCodeError != null
                    val isValid = uiState.selectedDiscount != null && !hasError

                    OutlinedTextField(
                        value = uiState.promoCodeInput,
                        onValueChange = onPromoCodeChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Enter promo code") },
                        isError = hasError,
                        supportingText = {
                            if (hasError) {
                                Text(text = uiState.promoCodeError ?: "", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        trailingIcon = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.promoCodeInput.isNotEmpty()) {
                                    IconButton(onClick = onClearPromoCode) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                } else {
                                    IconButton(onClick = {
                                        clipboardManager.getText()?.text?.let { onPromoCodeChange(it) }
                                    }) {
                                        Icon(
                                            Icons.Default.ContentPaste,
                                            contentDescription = "Paste",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                if (isValid) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Applied",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(20.dp).padding(end = 8.dp)
                                    )
                                } else if (hasError) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp).padding(end = 8.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isValid) Color(0xFF4CAF50) else if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isValid) Color(0xFF4CAF50) else if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                    )

                    if (uiState.availableDiscounts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "AVAILABLE COUPONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.availableDiscounts.forEach { discount ->
                            BurgerKingCouponCard(
                                discount = discount,
                                isSelected = uiState.selectedDiscount?.code == discount.code,
                                onApply = { onDiscountSelected(if (uiState.selectedDiscount?.code == discount.code) null else discount) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BurgerKingCouponCard(
    discount: com.example.testing1.models.Discount,
    isSelected: Boolean,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD97706)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ticket Coupon Green Badge (Burger King Style)
                Surface(
                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Text(
                        text = discount.code,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // APPLY CTA Button
                Button(
                    onClick = onApply,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) Color(0xFF047857) else Color(0xFFD97706),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text(
                        text = if (isSelected) "APPLIED ✓" else "APPLY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = discount.description.ifBlank { "Get extra savings on your coffee order" },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "+ View offer details & terms",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
        }
    }
}

@Composable
fun PaymentRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = if (isBold) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = color
        )
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
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(
            LottieAnimations.ADD_TO_CART
        )
    )

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
