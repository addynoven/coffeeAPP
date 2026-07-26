package com.example.testing1.screens.settingsscreen

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.testing1.data.repository.SettingsRepository
import com.example.testing1.data.settings.AppLanguage
import com.example.testing1.data.settings.ThemeConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
            combine(
                settingsRepository.getThemeConfig(),
                settingsRepository.getSelectedLanguage()
            ) { theme, languageCode ->
                theme to AppLanguage.fromCode(languageCode)
            }.collect { (theme, language) ->
                _uiState.value = _uiState.value.copy(
                    themeConfig = theme,
                    selectedLanguage = language
                )
            }
        }
    }

    fun onThemeConfigChange(config: ThemeConfig) {
        viewModelScope.launch {
            settingsRepository.setThemeConfig(config)
        }
    }

    fun onLanguageChange(language: AppLanguage) {
        viewModelScope.launch {
            settingsRepository.setSelectedLanguage(language.code)
            
            val appLocale: LocaleListCompat = if (language.code.isEmpty()) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(language.code)
            }
            AppCompatDelegate.setApplicationLocales(appLocale)
        }
    }
}
