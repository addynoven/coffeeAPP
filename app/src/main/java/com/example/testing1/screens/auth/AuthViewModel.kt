package com.example.testing1.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.repository.AuthRepository
import com.example.testing1.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    fun onEmailChange(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun login() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                successMessage = null
            )
            try {
                authRepository.signIn(
                    _uiState.value.email,
                    _uiState.value.password
                )
                settingsRepository.setHasSeenWelcome(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "login() failed: ${e.message}")
                val errorMessage =
                    if (e.message?.contains("email_not_confirmed") == true) {
                        "Please confirm your email address before logging in."
                    } else {
                        e.message
                    }
                _uiState.value =
                    _uiState.value.copy(error = errorMessage, isLoading = false)
            }
        }
    }

    fun signUp() {
        viewModelScope.launch {
            Log.d(
                "AuthViewModel",
                "signUp() started for email: ${_uiState.value.email}"
            )
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                successMessage = null
            )
            try {
                authRepository.signUp(
                    _uiState.value.email,
                    _uiState.value.password,
                    _uiState.value.name
                )
                Log.d("AuthViewModel", "authRepository.signUp() finished")

                // Check if we got a session automatically
                if (authRepository.sessionStatus.value is SessionStatus.Authenticated) {
                    settingsRepository.setHasSeenWelcome(true)
                } else {
                    // Confirmation required
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Success! Please check your email to confirm your account.",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "signUp() failed: ${e.message}", e)
                val errorMessage =
                    if (e.message?.contains("email_not_confirmed") == true) {
                        "Email not confirmed. Please check your inbox."
                    } else {
                        e.message
                    }
                _uiState.value =
                    _uiState.value.copy(error = errorMessage, isLoading = false)
            }
        }
    }

    fun onGoogleSignIn(
        idToken: String,
        rawName: String? = null,
        rawEmail: String? = null,
        rawAvatarUrl: String? = null
    ) {
        viewModelScope.launch {
            Log.d("AuthViewModel", "onGoogleSignIn() started with token length: ${idToken.length}")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                authRepository.signInWithIdToken(
                    idToken = idToken,
                    rawName = rawName,
                    rawEmail = rawEmail,
                    rawAvatarUrl = rawAvatarUrl
                )
                Log.d("AuthViewModel", "authRepository.signInWithIdToken() finished")
                settingsRepository.setHasSeenWelcome(true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Sign-In failed in repository: ${e.message}", e)
                _uiState.value =
                    _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun setWelcomeSeen() {
        viewModelScope.launch {
            settingsRepository.setHasSeenWelcome(true)
        }
    }

    fun onError(message: String) {
        val friendlyMessage = when {
            message.contains("28444") -> "Google Login Error: SHA-1 fingerprint mismatch. Add your debug SHA-1 to the Google Cloud Console."
            message.contains("email_not_confirmed") -> "Email not confirmed. Please check your inbox."
            message.contains("provider_email_needs_verification") -> "Your Google email is not verified. Please verify your email in your Google Account settings."
            message.contains("invalid login credentials") -> "Invalid email or password."
            else -> message
        }
        _uiState.value = _uiState.value.copy(error = friendlyMessage, isLoading = false)
    }
}

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)
