package ru.createsmart.artopos.feature.discover.model

data class FilterState(
    val selectedClassification: String? = null, // "Paintings"
    val selectedCentury: String? = null, // "19th century"
    val selectedCulture: String? = null, // "French"
)
