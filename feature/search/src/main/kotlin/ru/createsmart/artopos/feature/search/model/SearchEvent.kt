package ru.createsmart.artopos.feature.search.model

sealed interface SearchEvent {
    data object ScrollToTop : SearchEvent
}
