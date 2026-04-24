package ru.createsmart.artopos.core.model

/**
 * Encapsulates the messy date format from the API into a clean domain model.
 */
sealed interface CreationDate {
    /**
     * Exact or parsed year (e.g., 1920)
     */
    data class ExactYear(val year: Int) : CreationDate

    /**
     * Unparseable text representation (e.g., "17th century", "Ming Dynasty")
     */
    data class TextOnly(val text: String) : CreationDate

    /**
     * Date is completely unknown or missing
     */
    data object Unknown : CreationDate
}
