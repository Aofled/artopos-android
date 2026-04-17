package ru.createsmart.artopos.core.network.di

import kotlinx.serialization.json.Json

internal val NetworkJson = Json {
    ignoreUnknownKeys = true // Stability: Don't crash if API adds new fields
    coerceInputValues = true // Stability: Convert nulls/errors to default values (safe parsing)
    encodeDefaults = true
}
