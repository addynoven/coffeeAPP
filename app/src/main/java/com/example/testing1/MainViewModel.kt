package com.example.testing1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.repository.SettingsRepository
import com.example.testing1.data.settings.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val themeConfig: StateFlow<ThemeConfig> = settingsRepository.getThemeConfig()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeConfig.FOLLOW_SYSTEM
        )
}
