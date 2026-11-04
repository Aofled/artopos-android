package ru.createsmart.artopos.feature.search

import androidx.paging.PagingData
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import ru.createsmart.artopos.core.artworkcard.mapper.ArtworkUiMapper
import ru.createsmart.artopos.core.common.translation.FilterTranslator
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.domain.usecase.GetFavoriteArtworkIdsUseCase
import ru.createsmart.artopos.core.domain.usecase.GetFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.InitializeFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.SearchArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import ru.createsmart.artopos.core.model.FilterParamItem
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.testing.util.MainDispatcherRule
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.search.model.ActiveFilterItemUi
import ru.createsmart.artopos.feature.search.model.SearchEvent
import ru.createsmart.artopos.feature.search.model.SearchIntent
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val searchArtworks: SearchArtworksUseCase = mockk()
    private val getFilters: GetFiltersUseCase = mockk()
    private val initializeFilters: InitializeFiltersUseCase = mockk()
    private val toggleFavorite: ToggleFavoriteUseCase = mockk(relaxed = true)
    private val getFavoriteArtworkIds: GetFavoriteArtworkIdsUseCase = mockk()
    private val messageManager: UiMessageManager = mockk(relaxed = true)
    private val mapper: ArtworkUiMapper = ArtworkUiMapper()
    private val filterTranslator: FilterTranslator = mockk()

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setUp() {
        coEvery { initializeFilters(any<Boolean>()) } returns Result.success(Unit)
        every { getFilters(any()) } returns flowOf(emptyList())
        every { getFavoriteArtworkIds() } returns flowOf(setOf(10, 20))
        every { searchArtworks(any(), any()) } returns flowOf(PagingData.empty())
        every { filterTranslator.translate(any()) } answers { "Translated_${firstArg<String>()}" }

        viewModel = SearchViewModel(
            searchArtworks = searchArtworks,
            getFilters = getFilters,
            initializeFilters = initializeFilters,
            toggleFavorite = toggleFavorite,
            getFavoriteArtworkIds = getFavoriteArtworkIds,
            messageManager = messageManager,
            mapper = mapper,
            filterTranslator = filterTranslator,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has empty query, default sort, no active filters and favorite ids from use case`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("", state.searchQuery)
            assertEquals(FilterSortOption.TOTAL_PAGE_VIEWS, state.activeFilterParams.sort)
            assertNull(state.activeFilterParams.classification)
            assertTrue(state.activeFilters.isEmpty())
            assertFalse(state.hasActiveFilters)
            assertTrue(state.favoriteArtworkIds.contains(10))
            assertTrue(state.favoriteArtworkIds.contains(20))
        }

        verify(exactly = 0) { searchArtworks(any(), any()) }
    }

    @Test
    fun `intent QueryChanged debounces 500ms before calling searchArtworks`() = runTest {
        viewModel.artworksFlow.test {
            awaitItem()

            viewModel.onIntent(SearchIntent.QueryChanged("Monet"))

            advanceTimeBy(300.milliseconds)
            verify(exactly = 0) { searchArtworks(any(), any()) }

            advanceTimeBy(201.milliseconds)

            verify(exactly = 1) { searchArtworks("Monet", any()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `intent ClearQuery resets searchQuery in state`() = runTest {
        viewModel.uiState.test {
            assertEquals("", awaitItem().searchQuery)

            viewModel.onIntent(SearchIntent.QueryChanged("Van Gogh"))
            assertEquals("Van Gogh", awaitItem().searchQuery)

            viewModel.onIntent(SearchIntent.ClearQuery)
            assertEquals("", awaitItem().searchQuery)
        }
    }

    @Test
    fun `intent FilterSelected does not update activeFilterParams until FilterApply`() = runTest {
        viewModel.uiState.test {
            val initialState = awaitItem()
            assertNull(initialState.activeFilterParams.culture)
            assertFalse(initialState.hasActiveFilters)

            val frenchFilter = FilterParamItem(id = 100L, rawName = "French")
            viewModel.onIntent(SearchIntent.FilterSelected(FilterType.CULTURE, frenchFilter))

            expectNoEvents()

            viewModel.onIntent(SearchIntent.FilterApply)

            val updatedState = awaitItem()
            assertEquals(frenchFilter, updatedState.activeFilterParams.culture)
            assertTrue(updatedState.hasActiveFilters)
            assertEquals(
                listOf(
                    ActiveFilterItemUi(
                        type = FilterType.CULTURE,
                        label = UiText.DynamicString("Translated_French"),
                    ),
                ),
                updatedState.activeFilters,
            )
        }
    }

    @Test
    fun `intent RemoveActiveFilter removes filter from activeFilters and updates hasActiveFilters`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            val paintingFilter = FilterParamItem(id = 26L, rawName = "Paintings")
            viewModel.onIntent(SearchIntent.FilterSelected(FilterType.CLASSIFICATION, paintingFilter))
            viewModel.onIntent(SearchIntent.FilterApply)

            val withFilterState = awaitItem()
            assertTrue(withFilterState.hasActiveFilters)
            assertEquals(1, withFilterState.activeFilters.size)

            viewModel.onIntent(SearchIntent.RemoveActiveFilter(FilterType.CLASSIFICATION))

            val clearedState = awaitItem()
            assertFalse(clearedState.hasActiveFilters)
            assertTrue(clearedState.activeFilters.isEmpty())
        }
    }

    @Test
    fun `intent FilterApply emits ScrollToTop event when params change`() = runTest {
        viewModel.actions.test {
            val filter = FilterParamItem(id = 26L, rawName = "Paintings")
            viewModel.onIntent(SearchIntent.FilterSelected(FilterType.CLASSIFICATION, filter))

            viewModel.onIntent(SearchIntent.FilterApply)

            assertEquals(SearchEvent.ScrollToTop, awaitItem())
        }
    }

    @Test
    fun `intent ToggleFavorite calls use case with pure artwork ID`() = runTest {
        val artworkId = 999

        viewModel.onIntent(SearchIntent.ToggleFavorite(artworkId))

        coVerify(exactly = 1) { toggleFavorite(artworkId) }
    }

    @Test
    fun `intent FilterSearchQueryChanged updates bottom sheet query without affecting main search query`() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals("", initial.searchQuery)
            assertEquals("", initial.filtersUiState.searchQuery)

            viewModel.onIntent(SearchIntent.FilterSearchQueryChanged("Sculpt"))

            val updated = awaitItem()
            assertEquals("", updated.searchQuery)
            assertEquals("Sculpt", updated.filtersUiState.searchQuery)
        }
    }

    @Test
    fun `intent Refresh checks internet and increments contentVersion`() = runTest {
        every { messageManager.checkInternetAndNotify() } returns true

        viewModel.uiState.test {
            val initial = awaitItem()
            assertEquals(0, initial.contentVersion)

            viewModel.onIntent(SearchIntent.Refresh)

            val updated = awaitItem()
            assertEquals(1, updated.contentVersion)

            verify(exactly = 1) { messageManager.checkInternetAndNotify() }
            verify(exactly = 1) { messageManager.resetLastEmittedMessage() }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
