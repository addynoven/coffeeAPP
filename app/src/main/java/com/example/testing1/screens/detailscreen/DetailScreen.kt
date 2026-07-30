package com.example.testing1.screens.detailscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.SubcomposeAsyncImage
import com.example.testing1.R
import com.example.testing1.screens.detailscreen.components.BottomBuyBar
import com.example.testing1.screens.detailscreen.components.SizeOption
import com.example.testing1.screens.ui_components.AnimatedFavoriteButton
import com.example.testing1.util.LocalCloudinaryHelper
import com.example.testing1.util.UiEvent
import com.example.testing1.util.sharedElementExt
import com.example.testing1.util.shimmerLoading

@Composable
fun DetailRoute(
    coffeeId: String,
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(coffeeId) {
        viewModel.loadCoffee(coffeeId)
    }

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

    DetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onBackClick = onBackClick,
        onSizeSelected = viewModel::onSizeSelected,
        onToggleFavorite = viewModel::toggleFavorite,
        onAddToCart = viewModel::addToCart
    )
}

@Composable
fun DetailScreen(
    uiState: DetailUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBackClick: () -> Unit,
    onSizeSelected: (String) -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToCart: () -> Unit
) {
    val item = uiState.coffeeItem ?: return
    val cloudinaryHelper = LocalCloudinaryHelper.current
    val language = LocalConfiguration.current.locales[0].language
    val haptic = LocalHapticFeedback.current

    // Burger King Style Customization states
    var selectedCombo by remember { mutableStateOf<String?>(null) }
    var extraShotChecked by remember { mutableStateOf(false) }
    var oatMilkChecked by remember { mutableStateOf(false) }

    val comboPrice = when (selectedCombo) {
        "Croissant" -> 2.50
        "Muffin" -> 2.00
        "Cookie" -> 1.50
        else -> 0.00
    }
    val addOnPrice = (if (extraShotChecked) 0.50 else 0.0) + (if (oatMilkChecked) 0.75 else 0.0)
    val grandTotal = item.price + comboPrice + addOnPrice

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.Default.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "DETAIL",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 1.sp
                )
                AnimatedFavoriteButton(
                    isFavorite = item.isFavorite,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleFavorite()
                    },
                    modifier = Modifier.background(Color.Black.copy(alpha = 0.2f), CircleShape),
                    size = 38
                )
            }
        },
        bottomBar = {
            BottomBuyBar(price = grandTotal, onAddToCartClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onAddToCart()
            })
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Hero Coffee Image with Ingredient Overlay Badge (Burger King Style)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            ) {
                SubcomposeAsyncImage(
                    model = cloudinaryHelper.optimize(item.imageUrl, width = 600),
                    contentDescription = item.name,
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .shimmerLoading()
                        )
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.default_bean),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .sharedElementExt("coffee-img-${item.id}"),
                    contentScale = ContentScale.Crop
                )

                // FLAME ROASTED / 100% ARABICA Badge
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    color = Color(0xFFD97706),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "🔥 100% ARABICA ROASTED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Coffee Title & Subtitle
            Text(
                text = item.getLocalizedName(language).uppercase(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${item.getLocalizedDescription(language)}. ${stringResource(R.string.description_footer)}",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // SIZE SELECTOR
            Text(
                text = "SELECT SIZE",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                listOf("S", "M", "L").forEach { size ->
                    SizeOption(
                        size = size,
                        isSelected = uiState.selectedSize == size,
                        onClick = { onSizeSelected(size) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // MAKE IT A MEAL / COMBOS Section (Burger King Style)
            Text(
                text = "MAKE IT A COMBO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD97706),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            val comboOptions = listOf(
                "Croissant" to "Butter Croissant + Coffee (+$ 2.50)",
                "Muffin" to "Chocolate Muffin + Coffee (+$ 2.00)",
                "Cookie" to "Artisanal Cookie + Coffee (+$ 1.50)"
            )

            comboOptions.forEach { (key, label) ->
                val isSelected = selectedCombo == key
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { selectedCombo = if (isSelected) null else key },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD97706)) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedCombo = if (isSelected) null else key }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(20.dp))

            // ADD-ONS & CUSTOMIZATION Section (Burger King Style)
            Text(
                text = "ADD-ONS & CUSTOMIZATION",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { extraShotChecked = !extraShotChecked }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = extraShotChecked,
                        onCheckedChange = { extraShotChecked = it }
                    )
                    Text("Extra Espresso Shot", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text("+$ 0.50", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { oatMilkChecked = !oatMilkChecked }
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = oatMilkChecked,
                        onCheckedChange = { oatMilkChecked = it }
                    )
                    Text("Oat Milk Swap", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                Text("+$ 0.75", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
