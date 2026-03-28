package ru.createsmart.artopos.feature.favorites

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.createsmart.artopos.core.domain.usecase.GetFavoriteArtworksUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ImageDimensions
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.favorites.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getFavoritesUseCase: GetFavoriteArtworksUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)
    private val messageManager: UiMessageManager = mockk(relaxed = true)

    private lateinit var viewModel: FavoritesViewModel
    private val favoritesFlow = MutableSharedFlow<List<Artwork>>()

    private fun setupViewModel() {
        every { getFavoritesUseCase() } returns favoritesFlow
        viewModel = FavoritesViewModel(getFavoritesUseCase, toggleFavoriteUseCase, messageManager)
    }

    @Test
    fun `uiState emits Empty when use case returns empty list`() = runTest {
        // GIVEN
        setupViewModel()

        viewModel.uiState.test {
            // WHEN
            favoritesFlow.emit(emptyList())

            // THEN
            assertEquals(FavoritesUiState.Empty, viewModel.uiState.value)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `uiState emits Success with mapped items when use case returns data`() = runTest {
        // GIVEN
        setupViewModel()
        val fakeArtwork = Artwork(
            id = 1, title = "Mona Lisa", artist = "Da Vinci", imageUrl = "url",
            imageDimensions = ImageDimensions(100, 100), isFavorite = true,
            date = null, yearInt = null, technique = null,
            description = null, url = null, provenance = null, creditLine = null,
            classification = null, culture = null, medium = null, period = null,
            style = null, dimensions = null, copyright = null, galleryLocation = null, images = emptyList(),
        )

        viewModel.uiState.test {
            awaitItem() // Skip initial state

            // WHEN
            favoritesFlow.emit(listOf(fakeArtwork))

            // THEN
            val state = awaitItem()
            assertTrue("Expected Success state but got $state", state is FavoritesUiState.Success)

            val successState = state as FavoritesUiState.Success
            assertEquals(1, successState.artworks.size)
            assertEquals("Mona Lisa", successState.artworks[0].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onRefresh increments content version when internet is available`() = runTest {
        // GIVEN
        setupViewModel()
        every { messageManager.checkInternetAndNotify() } returns true
        val initialVersion = viewModel.contentVersion.value

        // WHEN
        viewModel.onRefresh()
        advanceUntilIdle()

        // THEN
        assertEquals(initialVersion + 1, viewModel.contentVersion.value)
    }
}
