package com.example.testing1.screens.profilescreen

import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.user.UserEntity

data class ProfileUiState(
    val user: UserEntity? = null,
    val addresses: List<AddressEntity> = emptyList(),
    val isEditMode: Boolean = false,
    val isAddingAddress: Boolean = false,
    val editName: String = "",
    val editEmail: String = "",
    val newAddressTag: String = "Home",
    val newAddressText: String = "",
    val isLoading: Boolean = true
)
