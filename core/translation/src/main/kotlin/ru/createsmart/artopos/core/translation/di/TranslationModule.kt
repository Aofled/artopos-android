package ru.createsmart.artopos.core.translation.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.domain.translation.TextTranslator
import ru.createsmart.artopos.core.translation.MLKitTranslatorImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface TranslationModule {

    @Binds
    @Singleton
    fun bindTextTranslator(
        impl: MLKitTranslatorImpl,
    ): TextTranslator
}
