package ru.createsmart.artopos.feature.discover.model

sealed interface DiscoverEvent {
    data object ScrollToTop : DiscoverEvent
}
