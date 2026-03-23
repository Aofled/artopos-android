package ru.createsmart.artopos.core.domain.usecase

import ru.createsmart.artopos.core.domain.translation.TextTranslator
import javax.inject.Inject

class PreloadTranslationModelUseCase @Inject constructor(
    private val translator: TextTranslator,
) {
    suspend operator fun invoke(targetLanguage: String) {
        translator.preloadModel(targetLanguage)
    }
}
