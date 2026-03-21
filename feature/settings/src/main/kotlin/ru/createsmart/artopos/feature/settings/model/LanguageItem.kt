package ru.createsmart.artopos.feature.settings.model

import ru.createsmart.artopos.feature.settings.R

data class LanguageItem(
    val tag: String,
    val nameResId: Int? = null,
    val nativeName: String? = null,
)

object LanguageConfig {
    val supportedLanguages = listOf(
        LanguageItem(tag = "", nameResId = R.string.language_system),
        LanguageItem(tag = "en", nameResId = R.string.language_en),
        LanguageItem(tag = "ru", nameResId = R.string.language_ru),
        LanguageItem(tag = "fr", nameResId = R.string.language_fr),
    )
}
