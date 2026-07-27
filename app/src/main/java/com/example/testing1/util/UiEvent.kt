package com.example.testing1.util

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data object NavigateBack : UiEvent()
    // Add more as needed
}
