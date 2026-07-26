package com.example.testing1.data.settings

enum class AppLanguage(val code: String, val displayName: String) {
    FOLLOW_SYSTEM("", "Follow System"),
    ENGLISH("en", "English"),
    JAPANESE("ja", "日本語"),
    GERMAN("de", "Deutsch"),
    RUSSIAN("ru", "Русский"),
    PORTUGUESE("pt", "Português"),
    FRENCH("fr", "Français"),
    ARABIC("ar", "العربية"),
    SPANISH("es", "Español"),
    CHINESE("zh", "中文"),
    ITALIAN("it", "Italiano");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: FOLLOW_SYSTEM
        }
    }
}
