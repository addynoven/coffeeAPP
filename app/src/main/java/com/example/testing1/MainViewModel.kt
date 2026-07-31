package com.example.testing1

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.repository.AuthRepository
import com.example.testing1.data.repository.SettingsRepository
import com.example.testing1.data.settings.ThemeConfig
import com.example.testing1.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = settingsRepository.getThemeConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeConfig.FOLLOW_SYSTEM
        )

    val startDestination: StateFlow<Routes?> = combine(
        settingsRepository.getHasSeenWelcome(),
        authRepository.sessionStatus
    ) { hasSeenWelcome, sessionStatus ->
        Log.d("MainViewModel", "Calculating destination - hasSeenWelcome: $hasSeenWelcome, sessionStatus: $sessionStatus")
        val dest = when {
            !hasSeenWelcome -> Routes.WelcomeScreen
            sessionStatus is SessionStatus.Authenticated -> Routes.HomeScreen
            sessionStatus is SessionStatus.Initializing -> null
            else -> Routes.LoginScreen
        }
        Log.d("MainViewModel", "Calculated destination: $dest")
        dest
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
}
