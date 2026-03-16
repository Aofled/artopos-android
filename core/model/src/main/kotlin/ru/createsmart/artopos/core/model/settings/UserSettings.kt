package ru.createsmart.artopos.core.model.settings

data class UserSettings(
    val themeConfig: ThemeConfig,
    val languageCode: String, // Example: "en", "ru", "" - system language (by default)
)
