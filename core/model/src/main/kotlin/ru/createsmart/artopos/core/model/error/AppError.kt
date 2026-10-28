package ru.createsmart.artopos.core.model.error

public sealed class AppError(
    message: String? = null,
    cause: Throwable? = null,
) : RuntimeException(message, cause) {

    // Network connection errors: no internet, DNS, socket timeouts
    public class Network(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppError(message ?: cause?.message ?: "Network connection error", cause)

    // Server errors (HTTP 4xx, 5xx)
    public class ExternalSystem(
        public val code: Int,
        message: String? = null,
        cause: Throwable? = null,
    ) : AppError(message ?: "External system error with HTTP code: $code", cause)

    // Deserialization errors (corrupted JSON, schema mismatch)
    public class DataFormat(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppError(message ?: cause?.message ?: "Data serialization error", cause)

    // Any other unexpected failures
    public class Unknown(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppError(message ?: cause?.message ?: "Unknown application error", cause)
}
