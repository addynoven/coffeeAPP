package com.example.testing1.screens.settingsscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.repository.SettingsRepository
import com.example.testing1.data.settings.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            settingsRepository.getThemeConfig().collect { config ->
                _uiState.value = _uiState.value.copy(themeConfig = config)
            }
        }
    }

    fun onThemeConfigChange(config: ThemeConfig) {
        viewModelScope.launch {
            settingsRepository.setThemeConfig(config)
        }
    }
}
