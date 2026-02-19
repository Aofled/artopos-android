@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)

package ru.createsmart.artopos.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FilterItemDTO(
    @SerialName("id") val id: Long,
    @SerialName("name") val name: String,
    @SerialName("objectcount") val count: Int,
    @SerialName("temporalorder") val order: Int? = null,
)
