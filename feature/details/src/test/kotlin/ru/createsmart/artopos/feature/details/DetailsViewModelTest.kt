package ru.createsmart.artopos.feature.details

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import ru.createsmart.artopos.core.domain.repository.ImageDownloader
import ru.createsmart.artopos.core.domain.usecase.GetArtworkDetailsUseCase
import ru.createsmart.artopos.core.domain.usecase.GetUserSettingsUseCase
import ru.createsmart.artopos.core.domain.usecase.SyncArtworkDetailsUseCase
import ru.createsmart.artopos.core.domain.usecase.ToggleFavoriteUseCase
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ArtworkDetails
import ru.createsmart.artopos.core.model.CreationDate
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.details.translation.ArtworkTranslationFacade
import ru.createsmart.artopos.feature.details.util.MainDispatcherRule

@RunWith(RobolectricTestRunner::class)
class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("artworkId" to 1))

    private val getArtworkDetails: GetArtworkDetailsUseCase = mockk()
    private val syncArtworkDetails: SyncArtworkDetailsUseCase = mockk(relaxed = true)
    private val getUserSettings: GetUserSettingsUseCase = mockk()
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase = mockk(relaxed = true)

    private val messageManager: UiMessageManager = mockk(relaxed = true)
    private val translationFacade: ArtworkTranslationFacade = mockk()
    private val imageDownloader: ImageDownloader = mockk(relaxed = true)

    private lateinit var viewModel: DetailsViewModel

    private val mockArtwork = createEmptyArtwork().copy(description = "English Description")
    private val mockFastArtwork = mockArtwork.copy()
    private val mockDeepArtwork = mockArtwork.copy(description = "Русское Описание")
    private val mockSettings = UserSettings(ThemeConfig.FOLLOW_SYSTEM, "ru")

    private fun setupViewModel() {
        every { getArtworkDetails(1) } returns flowOf(mockArtwork)
        every { getUserSettings() } returns flowOf(mockSettings)
        every { translationFacade.translateFast(mockArtwork, "ru") } returns mockFastArtwork

        viewModel = DetailsViewModel(
            getArtworkDetails = getArtworkDetails,
            syncArtworkDetails = syncArtworkDetails,
            getUserSettings = getUserSettings,
            toggleFavorite = toggleFavoriteUseCase,
            savedStateHandle = savedStateHandle,
            messageManager = messageManager,
            translationFacade = translationFacade,
            imageDownloader = imageDownloader,
        )
    }

    @Test
    fun `when translation is FAST, UI emits Success immediately without pending state`() = runTest {
        // GIVEN: Heavy Translation (ML Kit) responds INSTANTLY
        coEvery {
            translationFacade.translateDeep(mockArtwork, mockFastArtwork, "ru")
        } returns mockDeepArtwork

        setupViewModel()

        viewModel.uiState.test {
            val finalState = expectMostRecentItem()

            assertTrue(
                "State is not Success, but $finalState",
                finalState is ArtworkDetailUiState.Success,
            )

            val successState = finalState as ArtworkDetailUiState.Success

            // THEN: This is a translated version.
            assertTrue("Not translated", successState.artwork.isTranslated)
            // THEN: NO "waiting for translation" state (since it's a quick script)
            assertFalse("Pending is true", successState.artwork.isTranslationPending)
            // THEN
            assertEquals("Русское Описание", successState.artwork.description)
        }
    }

    @Test
    fun `when translation is SLOW, UI emits partial Success first, then final Success`() = runTest {
        // GIVEN: Heavy translation (ML Kit) is slow and responds after 1000ms
        coEvery {
            translationFacade.translateDeep(mockArtwork, mockFastArtwork, "ru")
        } coAnswers {
            delay(1000)
            mockDeepArtwork
        }

        setupViewModel()

        viewModel.uiState.test {
            // 1. Loading
            assertTrue(awaitItem() is ArtworkDetailUiState.Loading)

            advanceTimeBy(301)

            // 2. Intermediate state (300ms timeout occurred)
            val partialState = awaitItem() as ArtworkDetailUiState.Success

            // THEN
            assertEquals("English Description", partialState.artwork.description)
            assertTrue(partialState.artwork.isTranslationPending)

            // We skip forward another 700ms (total 1001ms, the transfer should be completed)
            advanceTimeBy(700)

            // 3. We obtain the FINAL state
            val finalState = awaitItem() as ArtworkDetailUiState.Success

            // THEN
            assertEquals("Русское Описание", finalState.artwork.description)
            assertFalse(finalState.artwork.isTranslationPending)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createEmptyArtwork(): ArtworkDetails {
        val base = Artwork(
            id = 1,
            title = "Title",
            artist = "Artist",
            imageUrl = "url",
            imageDimensions = null,
            creationDate = CreationDate.Unknown,
            isFavorite = false,
        )
        return ArtworkDetails(
            baseArtwork = base,
            technique = null, description = null, url = null, provenance = null,
            creditLine = null, classification = null, culture = null, images = emptyList(),
            medium = null, period = null, style = null, dimensions = null,
            copyright = null, galleryLocation = null, century = null,
        )
    }
}
