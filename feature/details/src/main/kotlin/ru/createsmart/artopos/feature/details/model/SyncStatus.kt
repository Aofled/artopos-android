package ru.createsmart.artopos.feature.details.model

internal sealed interface SyncStatus {
    data object Running : SyncStatus
    data object Idle : SyncStatus
    data object Failed : SyncStatus
}
