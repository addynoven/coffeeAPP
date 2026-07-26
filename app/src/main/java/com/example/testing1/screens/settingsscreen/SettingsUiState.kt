package com.example.testing1.screens.settingsscreen

import com.example.testing1.data.settings.AppLanguage
import com.example.testing1.data.settings.ThemeConfig

data class SettingsUiState(
    val themeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val selectedLanguage: AppLanguage = AppLanguage.FOLLOW_SYSTEM
)
