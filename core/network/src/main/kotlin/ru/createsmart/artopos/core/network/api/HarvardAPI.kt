package ru.createsmart.artopos.core.network.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.createsmart.artopos.core.network.model.ArtworkDTO
import ru.createsmart.artopos.core.network.model.FilterItemDTO
import ru.createsmart.artopos.core.network.model.NetworkResponse

/**
 * https://github.com/harvardartmuseums/api-docs
 */
interface HarvardAPI {
    @GET("object")
    suspend fun getArtworks(
        // Default filters for the main feed (Public Paintings with Images)
        @Query("classification") classification: String = "Paintings",
        @Query("hasimage") hasImage: Int = 1,
        @Query("permission") permission: Int = 0,
        @Query("sort") sort: String = "rank",
        @Query("sortorder") sortOrder: String = "desc",
        @Query("size") size: Int = 20,
        @Query("page") page: Int = 1,
        // Optimization: Request only necessary fields to save traffic/bandwidth
        @Query(
            "fields",
        ) fields: String = "id,title,dated,technique,primaryimageurl,description,url,people,images,places",
    ): NetworkResponse<ArtworkDTO>

    @GET("classification")
    suspend fun getClassification(
        @Query("sort") sort: String = "objectcount",
        @Query("sortorder") sortOrder: String = "desc",
        @Query("size") size: Int = 250,
    ): NetworkResponse<FilterItemDTO>

    @GET("century")
    suspend fun getCentury(
        @Query("sort") sort: String = "objectcount",
        @Query("sortorder") sortOrder: String = "desc",
        @Query("size") size: Int = 250,
    ): NetworkResponse<FilterItemDTO>

    @GET("culture")
    suspend fun getCulture(
        @Query("sort") sort: String = "objectcount",
        @Query("sortorder") sortOrder: String = "desc",
        @Query("size") size: Int = 250,
    ): NetworkResponse<FilterItemDTO>
}
