package ru.createsmart.artopos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.database.model.FilterItemDBO

@Dao
interface FilterItemDao {
    @Query("SELECT * FROM filters WHERE type = :filterType ORDER BY count DESC")
    fun getFiltersByType(filterType: String): Flow<List<FilterItemDBO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilters(filter: List<FilterItemDBO>)

    // "3" - Count of categories. We need all 3 to display filters.
    @Query("SELECT (COUNT(DISTINCT type) = 3) FROM filters")
    suspend fun hasAllCategories(): Boolean

    @Query("DELETE FROM filters")
    suspend fun clearFilters()
}
