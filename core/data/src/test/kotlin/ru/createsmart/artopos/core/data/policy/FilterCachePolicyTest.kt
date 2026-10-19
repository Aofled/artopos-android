package ru.createsmart.artopos.core.data.policy

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.createsmart.artopos.core.domain.policy.FilterCachePolicy
import kotlin.time.Duration.Companion.days

class FilterCachePolicyTest {

    private val now = 1_700_000_000_000L

    @Test
    fun `isCacheExpired returns true when lastSyncTimestamp is 0 (first launch)`() {
        val isExpired = FilterCachePolicy.isCacheExpired(
            lastSyncTimestamp = 0L,
            currentTime = now,
        )
        assertTrue("Cache must be expired if it was never synced", isExpired)
    }

    @Test
    fun `isCacheExpired returns false when cache is less than 30 days old`() {
        val twentyNineDaysAgo = now - 29.days.inWholeMilliseconds

        val isExpired = FilterCachePolicy.isCacheExpired(
            lastSyncTimestamp = twentyNineDaysAgo,
            currentTime = now,
        )
        assertFalse("Cache should be valid when 29 days have passed", isExpired)
    }

    @Test
    fun `isCacheExpired returns true when cache is exactly 30 days old`() {
        val exactlyThirtyDaysAgo = now - 30.days.inWholeMilliseconds

        val isExpired = FilterCachePolicy.isCacheExpired(
            lastSyncTimestamp = exactlyThirtyDaysAgo,
            currentTime = now,
        )
        assertTrue("Cache must be expired when exactly 30 days passed", isExpired)
    }

    @Test
    fun `isCacheExpired returns true when cache is more than 30 days old`() {
        val thirtyOneDaysAgo = now - 31.days.inWholeMilliseconds

        val isExpired = FilterCachePolicy.isCacheExpired(
            lastSyncTimestamp = thirtyOneDaysAgo,
            currentTime = now,
        )
        assertTrue("Cache must be expired when 31 days have passed", isExpired)
    }
}
