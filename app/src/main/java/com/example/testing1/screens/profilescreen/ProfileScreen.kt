package com.example.testing1.screens.profilescreen

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.testing1.R
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.user.UserEntity
import com.example.testing1.screens.ui_components.MyBottomBar

@Composable
fun ProfileRoute(
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileScreen(
        uiState = uiState,
        onHomeClick = onHomeClick,
        onCartClick = onCartClick,
        onFavoriteClick = onFavoriteClick,
        onProfileClick = onProfileClick,
        onOrdersClick = onOrdersClick,
        onSettingsClick = onSettingsClick,
        onEditClick = viewModel::onEditClick,
        onSaveClick = viewModel::onSaveClick,
        onCancelClick = viewModel::onCancelClick,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onAddAddressClick = viewModel::onAddAddressClick,
        onNewAddressTagChange = viewModel::onNewAddressTagChange,
        onNewAddressTextChange = viewModel::onNewAddressTextChange,
        onSaveNewAddress = viewModel::onSaveNewAddress,
        onCancelNewAddress = viewModel::onCancelNewAddress,
        onSetDefaultAddress = viewModel::onSetDefaultAddress,
        onDeleteAddress = viewModel::onDeleteAddress,
        onLogoutClick = viewModel::logout
    )
}

@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onHomeClick: () -> Unit,
    onCartClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCancelClick: () -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onAddAddressClick: () -> Unit,
    onNewAddressTagChange: (String) -> Unit,
    onNewAddressTextChange: (String) -> Unit,
    onSaveNewAddress: () -> Unit,
    onCancelNewAddress: () -> Unit,
    onSetDefaultAddress: (String) -> Unit,
    onDeleteAddress: (AddressEntity) -> Unit,
    onLogoutClick: () -> Unit
) {
    if (uiState.isAddingAddress) {
        AddAddressDialog(
            tag = uiState.newAddressTag,
            address = uiState.newAddressText,
            onTagChange = onNewAddressTagChange,
            onAddressChange = onNewAddressTextChange,
            onDismiss = onCancelNewAddress,
            onConfirm = onSaveNewAddress
        )
    }

    Scaffold(
        bottomBar = {
            MyBottomBar(
                selectedTabId = "profile",
                onTabSelected = { tabId ->
                    when (tabId) {
                        "home" -> onHomeClick()
                        "cart" -> onCartClick()
                        "favorite" -> onFavoriteClick()
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Header with Edit Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.profile_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (!uiState.isEditMode) {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_profile_desc))
                    }
                } else {
                    Row {
                        IconButton(onClick = onCancelClick) {
                            Icon(Icons.Default.Cancel, contentDescription = stringResource(R.string.cancel_label), tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = onSaveClick) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.save_label), tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Info Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.user?.avatarUrl != null) {
                        AsyncImage(
                            model = uiState.user.avatarUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(60.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                if (uiState.isEditMode) {
                    OutlinedTextField(
                        value = uiState.editName,
                        onValueChange = onNameChange,
                        label = { Text(stringResource(R.string.name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.editEmail,
                        onValueChange = onEmailChange,
                        label = { Text(stringResource(R.string.email_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = uiState.user?.name?.ifBlank { "Unknown Name" } ?: "Unknown Name",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = uiState.user?.email?.ifBlank { "No Email Address" } ?: "No Email Address",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Multi-Address Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.my_addresses_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onAddAddressClick) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.add_new_address_button))
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (uiState.addresses.isEmpty()) {
                Text(
                    text = stringResource(R.string.no_addresses_message),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(uiState.addresses) { address ->
                        AddressCard(
                            address = address,
                            onSetDefault = { onSetDefaultAddress(address.addressId) },
                            onDelete = { onDeleteAddress(address) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            if (!uiState.isEditMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileMenuItem(
                            icon = Icons.Default.ShoppingCart,
                            title = stringResource(R.string.order_history_menu),
                            onClick = onOrdersClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileMenuItem(
                            icon = Icons.Default.Favorite,
                            title = stringResource(R.string.my_favorites_menu),
                            onClick = onFavoriteClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileMenuItem(
                            icon = Icons.Default.Settings,
                            title = stringResource(R.string.settings_menu),
                            onClick = onSettingsClick
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        ProfileMenuItem(
                            icon = Icons.AutoMirrored.Filled.ExitToApp,
                            title = "Logout",
                            onClick = onLogoutClick,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AddressCard(
    address: AddressEntity,
    onSetDefault: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable { onSetDefault() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (address.isDefault) 
                MaterialTheme.colorScheme.primaryContainer 
            else MaterialTheme.colorScheme.surface
        ),
        border = if (address.isDefault) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn, 
                        contentDescription = null, 
                        modifier = Modifier.size(16.dp),
                        tint = if (address.isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = address.tag,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (address.isDefault) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (!address.isDefault) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = address.fullAddress,
                fontSize = 12.sp,
                maxLines = 2,
                color = if (address.isDefault) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
            if (address.isDefault) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.default_label),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddAddressDialog(
    tag: String,
    address: String,
    onTagChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_address_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = tag,
                    onValueChange = onTagChange,
                    label = { Text(stringResource(R.string.tag_label)) },
                    singleLine = true
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = onAddressChange,
                    label = { Text(stringResource(R.string.full_address_label)) },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.save_label)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_label)) }
        }
    )
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector, 
    title: String, 
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = tint
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        uiState = ProfileUiState(
            user = UserEntity(id = "1", name = "Anas", email = "anas@test.com"),
            isLoading = false
        ),
        onHomeClick = {},
        onCartClick = {},
        onFavoriteClick = {},
        onProfileClick = {},
        onOrdersClick = {},
        onSettingsClick = {},
        onEditClick = {},
        onSaveClick = {},
        onCancelClick = {},
        onNameChange = {},
        onEmailChange = {},
        onAddAddressClick = {},
        onNewAddressTagChange = {},
        onNewAddressTextChange = {},
        onSaveNewAddress = {},
        onCancelNewAddress = {},
        onSetDefaultAddress = {},
        onDeleteAddress = {},
        onLogoutClick = {}
    )
}
