package ru.createsmart.artopos.feature.discover

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.createsmart.artopos.core.artworkcard.mapper.ArtworkUiMapper
import ru.createsmart.artopos.core.domain.usecase.GetArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.GetFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.GetUserSettingsUseCase
import ru.createsmart.artopos.core.domain.usecase.InitializeFiltersUseCase
import ru.createsmart.artopos.core.domain.usecase.PreloadTranslationModelUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import ru.createsmart.artopos.core.model.FilterParamItem
import ru.createsmart.artopos.core.model.FilterSortOption
import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.discover.model.DiscoverIntent

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    private val getArtworks: GetArtworksUseCase = mockk()
    private val getFilters: GetFiltersUseCase = mockk()
    private val getUserSettings: GetUserSettingsUseCase = mockk()
    private val toggleFavorite: ToggleFavoriteUseCase = mockk()
    private val preloadTranslationModel: PreloadTranslationModelUseCase = mockk()
    private val initializeFilters: InitializeFiltersUseCase = mockk()

    private val messageManager: UiMessageManager = mockk(relaxed = true)
    private val mapper: ArtworkUiMapper = ArtworkUiMapper()

    private lateinit var viewModel: DiscoverViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        coEvery { initializeFilters() } returns Result.success(Unit)
        every { getFilters(any()) } returns flowOf(emptyList())

        val defaultSettings = UserSettings(ThemeConfig.FOLLOW_SYSTEM, "en")
        every { getUserSettings() } returns flowOf(defaultSettings)
        coEvery { preloadTranslationModel(any()) } returns Unit

        viewModel = DiscoverViewModel(
            getArtworks = getArtworks,
            getFilters = getFilters,
            getUserSettings = getUserSettings,
            toggleFavorite = toggleFavorite,
            preloadTranslationModel = preloadTranslationModel,
            initializeFilters = initializeFilters,
            messageManager = messageManager,
            mapper = mapper,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `intent SearchQueryChanged updates searchQuery in filtersUiState`() = runTest {
        viewModel.filtersUiState.test {
            // GIVEN
            val initialState = awaitItem()
            assertEquals("", initialState.searchQuery)

            // WHEN
            viewModel.onIntent(DiscoverIntent.SearchQueryChanged("Japan"))

            // THEN
            val updatedState = awaitItem()
            assertEquals("Japan", updatedState.searchQuery)
        }
    }

    @Test
    fun `intent ToggleFilterSort cycles through sorting options`() = runTest {
        viewModel.filtersUiState.test {
            // GIVEN
            val initialState = awaitItem()
            assertEquals(FilterSortOption.RANK, initialState.sort) // Дефолт

            // WHEN
            viewModel.onIntent(DiscoverIntent.ToggleFilterSort)

            // THEN
            val stateAfterFirstClick = awaitItem()
            assertEquals(FilterSortOption.TOTAL_PAGE_VIEWS, stateAfterFirstClick.sort)
        }
    }

    @Test
    fun `intent FilterSelected does NOT update activeFilterParams until FilterApply is sent`() =
        runTest {
            viewModel.activeFilterParams.test {
                // GIVEN
                val initialParams = awaitItem()
                assertEquals(null, initialParams.culture)

                // WHEN
                val frenchFilter = FilterParamItem(id = 37527426L, rawName = "French")
                viewModel.onIntent(DiscoverIntent.FilterSelected(FilterType.CULTURE, frenchFilter))

                // THEN
                expectNoEvents()

                // THEN
                viewModel.onIntent(DiscoverIntent.FilterApply)

                // THEN
                val appliedParams = awaitItem()
                assertEquals(frenchFilter, appliedParams.culture)
            }
        }

    @Test
    fun `intent FilterReset clears draft params and search query`() = runTest {
        viewModel.filtersUiState.test {
            val initialState = awaitItem()
            assertEquals("", initialState.searchQuery)

            // GIVEN
            viewModel.onIntent(DiscoverIntent.SearchQueryChanged("Picasso"))
            val paintingsFilter = FilterParamItem(id = 26L, rawName = "Paintings")
            viewModel.onIntent(DiscoverIntent.FilterSelected(FilterType.CLASSIFICATION, paintingsFilter))

            val stateBeforeReset = expectMostRecentItem()
            assertEquals("Picasso", stateBeforeReset.searchQuery)

            // WHEN
            viewModel.onIntent(DiscoverIntent.FilterReset)

            // THEN
            val stateAfterReset = awaitItem()
            assertEquals("", stateAfterReset.searchQuery)
            assertEquals(FilterSortOption.RANK, stateAfterReset.sort)
        }
    }

    @Test
    fun `intent ToggleFavorite calls use case with correct id`() = runTest {
        // GIVEN
        coEvery { toggleFavorite(any()) } returns Unit

        // WHEN
        viewModel.onIntent(DiscoverIntent.ToggleFavorite(123))

        // THEN
        coVerify(exactly = 1) { toggleFavorite(123) }
    }

    @Test
    fun `intent Refresh checks internet and initializes filters`() = runTest {
        // GIVEN
        every { messageManager.checkInternetAndNotify() } returns true

        // WHEN
        viewModel.onIntent(DiscoverIntent.Refresh)

        // THEN
        io.mockk.verify(exactly = 1) { messageManager.checkInternetAndNotify() }

        // THEN
        coVerify(exactly = 2) { initializeFilters() }

        // THEN
        io.mockk.verify(exactly = 1) { messageManager.resetLastEmittedMessage() }
    }
}
