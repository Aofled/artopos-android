package ru.createsmart.artopos.core.domain.repository

interface ImageCacheRepository {
    suspend fun clearCache(): Long
}
