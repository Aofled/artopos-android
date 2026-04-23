package ru.createsmart.artopos.feature.favorites

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.createsmart.artopos.core.artworkcard.mapper.ArtworkUiMapper
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.domain.interactor.FavoritesInteractor
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ImageDimensions
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.favorites.model.FavoritesIntent
import ru.createsmart.artopos.feature.favorites.util.MainDispatcherRule

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCases: FavoritesInteractor = mockk(relaxed = true)
    private val messageManager: UiMessageManager = mockk(relaxed = true)
    private val mapper: ArtworkUiMapper = ArtworkUiMapper()
    private lateinit var viewModel: FavoritesViewModel
    private val favoritesFlow = MutableSharedFlow<List<Artwork>>()

    private fun setupViewModel() {
        every { useCases.getFavoritesUseCase() } returns favoritesFlow
        viewModel = FavoritesViewModel(
            useCases,
            messageManager,
            mapper,
        )
    }

    @Test
    fun `uiState emits Loading initially and then Empty when use case returns empty list`() =
        runTest {
            // GIVEN
            setupViewModel()

            viewModel.uiState.test {
                assertEquals(FavoritesUiState.Loading, awaitItem())

                // WHEN
                favoritesFlow.emit(emptyList())

                // THEN
                assertEquals(FavoritesUiState.Empty, awaitItem())
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
            date = null, yearInt = null, technique = null, description = null,
            url = null, provenance = null, creditLine = null, classification = null,
            culture = null, medium = null, period = null, style = null,
            dimensions = null, copyright = null, galleryLocation = null, images = emptyList(),
        )

        viewModel.uiState.test {
            assertEquals(FavoritesUiState.Loading, awaitItem())

            // WHEN
            favoritesFlow.emit(listOf(fakeArtwork))

            // THEN
            val state = awaitItem()
            assertTrue("Expected Success state but got $state", state is FavoritesUiState.Success)

            val successState = state as FavoritesUiState.Success
            assertEquals(1, successState.artworks.size)
            assertEquals(UiText.DynamicString("Mona Lisa"), successState.artworks[0].title)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onIntent Refresh increments content version when internet is available`() = runTest {
        // GIVEN
        setupViewModel()
        every { messageManager.checkInternetAndNotify() } returns true
        val initialVersion = viewModel.contentVersion.value

        // WHEN
        viewModel.onIntent(FavoritesIntent.Refresh)

        // THEN
        assertEquals(initialVersion + 1, viewModel.contentVersion.value)
        verify(exactly = 1) { messageManager.resetLastEmittedMessage() }
    }

    @Test
    fun `onIntent Refresh DOES NOT increment content version when NO internet`() = runTest {
        // GIVEN
        setupViewModel()
        every { messageManager.checkInternetAndNotify() } returns false
        val initialVersion = viewModel.contentVersion.value

        // WHEN
        viewModel.onIntent(FavoritesIntent.Refresh)

        // THEN
        assertEquals(initialVersion, viewModel.contentVersion.value)
    }

    @Test
    fun `onIntent ToggleFavorite calls useCase`() = runTest {
        // GIVEN
        setupViewModel()
        val artworkId = 123

        // WHEN
        viewModel.onIntent(FavoritesIntent.ToggleFavorite(artworkId))

        // THEN
        coVerify(exactly = 1) { useCases.toggleFavoriteUseCase(artworkId) }
    }
}
