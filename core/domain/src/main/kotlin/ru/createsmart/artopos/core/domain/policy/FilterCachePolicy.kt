package ru.createsmart.artopos.core.domain.policy

import kotlin.time.Duration.Companion.days

public object FilterCachePolicy {
    // Cache time-to-live: 30 days.
    public val CACHE_TTL_MILLIS: Long = 30.days.inWholeMilliseconds

    // Returns true if cache is expired or was never synced (lastSyncTimestamp <= 0).
    public fun isCacheExpired(lastSyncTimestamp: Long, currentTime: Long): Boolean {
        if (lastSyncTimestamp <= 0L) return true
        return (currentTime - lastSyncTimestamp) >= CACHE_TTL_MILLIS
    }
}
