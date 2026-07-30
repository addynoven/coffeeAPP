package com.example.testing1.screens.profilescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.local.address.AddressEntity
import com.example.testing1.data.local.user.UserEntity
import com.example.testing1.data.repository.AuthRepository
import com.example.testing1.data.repository.CoffeeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: CoffeeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(repository.getUser(), repository.getAddresses()) { user, addresses ->
                user to addresses
            }.collect { (user, addresses) ->
                _uiState.value = _uiState.value.copy(
                    user = user,
                    addresses = addresses,
                    editName = user?.name ?: "",
                    editEmail = user?.email ?: "",
                    isLoading = false
                )
            }
        }
    }

    fun onEditClick() {
        _uiState.value = _uiState.value.copy(isEditMode = true)
    }

    fun onCancelClick() {
        val user = _uiState.value.user
        _uiState.value = _uiState.value.copy(
            isEditMode = false,
            editName = user?.name ?: "",
            editEmail = user?.email ?: ""
        )
    }

    fun onSaveClick() {
        val currentState = _uiState.value
        viewModelScope.launch {
            repository.updateUser(
                UserEntity(
                    id = authRepository.currentUserId,
                    name = currentState.editName,
                    email = currentState.editEmail
                )
            )
            _uiState.value = _uiState.value.copy(isEditMode = false)
        }
    }

    fun onNameChange(newName: String) {
        _uiState.value = _uiState.value.copy(editName = newName)
    }

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(editEmail = newEmail)
    }

    // Address Actions
    fun onAddAddressClick() {
        _uiState.value = _uiState.value.copy(isAddingAddress = true)
    }

    fun onNewAddressTagChange(tag: String) {
        _uiState.value = _uiState.value.copy(newAddressTag = tag)
    }

    fun onNewAddressTextChange(text: String) {
        _uiState.value = _uiState.value.copy(newAddressText = text)
    }

    fun onSaveNewAddress() {
        val currentState = _uiState.value
        if (currentState.newAddressText.isBlank()) return
        
        viewModelScope.launch {
            repository.addAddress(
                AddressEntity(
                    addressId = java.util.UUID.randomUUID().toString(),
                    userId = authRepository.currentUserId,
                    tag = currentState.newAddressTag,
                    fullAddress = currentState.newAddressText,
                    isDefault = currentState.addresses.isEmpty()
                )
            )
            _uiState.value = _uiState.value.copy(
                isAddingAddress = false,
                newAddressText = ""
            )
        }
    }

    fun saveMapAddress(tag: String, fullAddress: String, lat: Double, lng: Double) {
        viewModelScope.launch {
            val isDefault = _uiState.value.addresses.isEmpty()
            repository.addAddress(
                AddressEntity(
                    addressId = java.util.UUID.randomUUID().toString(),
                    userId = authRepository.currentUserId,
                    tag = tag,
                    fullAddress = fullAddress,
                    isDefault = isDefault,
                    latitude = lat,
                    longitude = lng
                )
            )
            _uiState.value = _uiState.value.copy(isAddingAddress = false)
        }
    }

    fun onCancelNewAddress() {
        _uiState.value = _uiState.value.copy(isAddingAddress = false, newAddressText = "")
    }

    fun onSetDefaultAddress(addressId: String) {
        viewModelScope.launch {
            repository.setAsDefaultAddress(addressId)
        }
    }

    fun onDeleteAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.deleteAddress(address)
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }
}
