@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package ru.createsmart.artopos.core.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class StoredImage(
    val url: String,
    val width: Int,
    val height: Int,
)

class ImagesConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromImagesList(images: List<StoredImage>?): String? {
        return images?.let { json.encodeToString(it) }
    }

    @TypeConverter
    fun toImagesList(data: String?): List<StoredImage>? {
        return data?.let { json.decodeFromString(it) }
    }
}
