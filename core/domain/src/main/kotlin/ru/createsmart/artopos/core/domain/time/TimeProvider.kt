package ru.createsmart.artopos.core.domain.time

// Abstraction over system clock. Use it to fake time in unit tests.
public interface TimeProvider {
    public fun getCurrentTimeMillis(): Long
}
