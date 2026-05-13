package ru.createsmart.artopos.core.model.error

public sealed class AppError(message: String? = null) : RuntimeException(message) {
    // The external system (API) responded with an error (e.g. 404, 500)
    public class ExternalSystem(public val code: Int, message: String? = null) : AppError(message)

    // Data parsing error (e.g., corrupted JSON)
    public class DataFormat(message: String? = null) : AppError(message)
}
