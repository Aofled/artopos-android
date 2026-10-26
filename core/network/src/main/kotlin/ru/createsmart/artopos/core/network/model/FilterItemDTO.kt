package ru.createsmart.artopos.core.network.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(InternalSerializationApi::class)
@Serializable
data class FilterItemDTO(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("objectcount") val count: Int,
    @SerialName("temporalorder") val order: Int? = null,
)
