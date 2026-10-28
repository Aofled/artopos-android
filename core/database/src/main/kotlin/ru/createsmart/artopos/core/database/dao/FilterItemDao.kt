package ru.createsmart.artopos.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.createsmart.artopos.core.database.model.FilterItemDBO

@Dao
interface FilterItemDao {
    @Query("SELECT * FROM filters WHERE type = :filterType ORDER BY count DESC")
    fun getFiltersByType(filterType: String): Flow<List<FilterItemDBO>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilters(filter: List<FilterItemDBO>)

    @Query("SELECT DISTINCT type FROM filters")
    suspend fun getDistinctFilterTypes(): List<String>

    @Query("DELETE FROM filters")
    suspend fun clearFilters()

    @Transaction
    suspend fun replaceFilters(filters: List<FilterItemDBO>) {
        clearFilters()
        insertFilters(filters)
    }
}
