package ru.createsmart.artopos.core.domain.repository

public interface ImageCacheRepository {
    public suspend fun clearCache(): Long
    public suspend fun getCacheSize(): Long
}
