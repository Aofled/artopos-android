package ru.createsmart.artopos.core.common.result

import kotlinx.coroutines.CancellationException

/**
 * A safe wrapper over runCatching for use with coroutines.
 * It catches all errors EXCEPT [CancellationException].
 *
 * If the coroutine is canceled (for example, the user navigates away from the screen),
 * CancellationException is rethrown without breaking Structured Concurrency.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <R> suspendRunCatching(block: () -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (c: CancellationException) {
        throw c
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
