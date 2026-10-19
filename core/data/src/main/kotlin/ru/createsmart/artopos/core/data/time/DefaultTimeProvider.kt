package ru.createsmart.artopos.core.data.time

import ru.createsmart.artopos.core.domain.time.TimeProvider
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultTimeProvider @Inject constructor() : TimeProvider {
    override fun getCurrentTimeMillis(): Long = System.currentTimeMillis()
}
