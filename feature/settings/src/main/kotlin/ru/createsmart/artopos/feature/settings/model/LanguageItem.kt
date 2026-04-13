package ru.createsmart.artopos.feature.settings.model

import ru.createsmart.artopos.feature.settings.R

data class LanguageItem(
    val tag: String,
    val nameResId: Int? = null,
    val nativeName: String? = null,
)

object LanguageConfig {
    val supportedLanguages = listOf(
        LanguageItem(tag = "", nameResId = R.string.settings_language_option_system),
        LanguageItem(tag = "en", nameResId = R.string.language_en),
        LanguageItem(tag = "ru", nameResId = R.string.language_ru),
        LanguageItem(tag = "fr", nameResId = R.string.language_fr),
        LanguageItem(tag = "be", nameResId = R.string.language_be),
        LanguageItem(tag = "ja", nameResId = R.string.language_ja),
        LanguageItem(tag = "zh", nameResId = R.string.language_zh),
        LanguageItem(tag = "de", nameResId = R.string.language_de),
        LanguageItem(tag = "it", nameResId = R.string.language_it),
    )
}
