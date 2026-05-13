package ru.createsmart.artopos.core.model

/**
 * Encapsulates the messy date format from the API into a clean domain model.
 */
public sealed interface CreationDate {
    /**
     * Exact or parsed year (e.g., 1920)
     */
    public data class ExactYear(val year: Int) : CreationDate

    /**
     * Unparseable text representation (e.g., "17th century", "Ming Dynasty")
     */
    public data class TextOnly(val text: String) : CreationDate

    /**
     * Date is completely unknown or missing
     */
    public data object Unknown : CreationDate
}
