package ru.createsmart.artopos.core.model.error

sealed class AppError(message: String? = null) : RuntimeException(message) {
    // The external system (API) responded with an error (e.g. 404, 500)
    class ExternalSystem(val code: Int, message: String? = null) : AppError(message)

    // Data parsing error (e.g., corrupted JSON)
    class DataFormat(message: String? = null) : AppError(message)
}
