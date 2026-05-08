package ru.createsmart.artopos.core.common.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.createsmart.artopos.core.common.translation.FilterTranslator
import ru.createsmart.artopos.core.common.translation.FilterTranslatorImpl

@Module
@InstallIn(SingletonComponent::class)
interface CommonModule {
    @Binds
    fun bindFilterTranslator(impl: FilterTranslatorImpl): FilterTranslator
}
