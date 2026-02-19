package ru.createsmart.artopos.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "filters")
data class FilterItemDBO(
    @PrimaryKey(autoGenerate = true) val uId: Long = 0,
    @ColumnInfo(name = "server_id") val id: Long,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "order") val order: Int? = null,
)
