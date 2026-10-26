package ru.createsmart.artopos.core.network.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// API Wrapper: Handles the specific structure "info + records"
@OptIn(InternalSerializationApi::class)
@Serializable
data class NetworkResponse<T> (
    @SerialName("info") val info: PageInfo,
    @SerialName("records") val records: List<T>,
)

@Serializable
data class PageInfo(
    @SerialName("page") val page: Int,
    @SerialName("pages") val totalPages: Int,
    @SerialName("totalrecords") val totalRecords: Int,
    @SerialName("next") val nextUrl: String? = null, // "next" may be "null" if this is the last page
)
