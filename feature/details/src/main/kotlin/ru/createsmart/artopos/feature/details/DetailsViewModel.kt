package ru.createsmart.artopos.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.designsystem.components.toUiText
import ru.createsmart.artopos.core.domain.interactor.DetailsInteractor
import ru.createsmart.artopos.core.domain.repository.ImageDownloader
import ru.createsmart.artopos.core.navigation.DetailsRoute
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.details.mapper.toDetailUi
import ru.createsmart.artopos.feature.details.model.DetailsIntent
import ru.createsmart.artopos.feature.details.translation.ArtworkTranslationFacade
import java.io.IOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.Locale
import javax.inject.Inject
import ru.createsmart.artopos.core.designsystem.R as DSR

private const val TRANSLATION_TIMEOUT_MS = 300L

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val useCases: DetailsInteractor,
    private val messageManager: UiMessageManager,
    private val translationFacade: ArtworkTranslationFacade,
    private val imageDownloader: ImageDownloader,
) : ViewModel() {
    private val routeArgs = savedStateHandle.toRoute<DetailsRoute>()
    private val artworkId = routeArgs.artworkId

    private val _contentVersion = MutableStateFlow(0) // To update images (bad internet)
    val contentVersion = _contentVersion.asStateFlow()

    val uiEffect = messageManager.uiEffect

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _showTranslation = MutableStateFlow(true)

    internal fun onIntent(intent: DetailsIntent) {
        when (intent) {
            is DetailsIntent.Refresh -> refresh()
            is DetailsIntent.ToggleFavorite -> toggleFavorite()
            is DetailsIntent.ToggleTranslation -> toggleTranslation()
            is DetailsIntent.DownloadImage -> downloadImage(intent.url, intent.title)
            is DetailsIntent.ShowMessage -> messageManager.sendSideEffect(intent.message)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ArtworkDetailUiState> = combine(
        useCases.getArtworkDetails(artworkId),
        useCases.getUserSettings(),
        _showTranslation,
    ) { artwork, settings, showTranslation ->
        Triple(artwork, settings.languageCode, showTranslation)
    }
        .transformLatest { (rawArtwork, languageCode, showTranslation) ->
            if (rawArtwork == null) {
                emit(ArtworkDetailUiState.Loading)
                return@transformLatest
            }

            // CONDITION 1: Language is English or the system language (if the system is in English)
            // CONDITION 2: The user clicked "Show Original" (showTranslation = false)
            val isTargetEnglish = languageCode == "en" ||
                (languageCode.isEmpty() && Locale.getDefault().language == "en")

            if (isTargetEnglish || !showTranslation) {
                emit(
                    ArtworkDetailUiState.Success(
                        rawArtwork.toDetailUi(
                            isTranslated = false, // To show/hide the bar
                            canBeTranslated = !isTargetEnglish, // If the language is NOT English
                            isTranslationPending = false,
                        ),
                    ),
                )
                return@transformLatest
            }

            // Get basic data (No heavy ML involved)
            val fastTranslatedArtwork = translationFacade.translateFast(rawArtwork, languageCode)

            // This prevents cancelling and restarting the ML Kit process if it takes longer than the timeout.
            coroutineScope {
                val deepTranslationDeferred = async {
                    translationFacade.translateDeep(rawArtwork, fastTranslatedArtwork, languageCode)
                }

                // Wait for up to TRANSLATION_TIMEOUT_MS to see if the translation finishes quickly
                val quickDeepTranslation = withTimeoutOrNull(TRANSLATION_TIMEOUT_MS) {
                    deepTranslationDeferred.await()
                }

                if (quickDeepTranslation != null) {
                    // Scenario A: Fast device/cache.
                    // Show final result immediately. Avoids UI flickering (Fast -> Deep).
                    emit(
                        ArtworkDetailUiState.Success(
                            quickDeepTranslation.toDetailUi(isTranslated = true, isTranslationPending = false),
                        ),
                    )
                } else {
                    // Scenario B: Slow translation.
                    // 1. Show "Fast" version first (Partial/Original text) so user sees content instantly.
                    emit(
                        ArtworkDetailUiState.Success(
                            fastTranslatedArtwork.toDetailUi(isTranslated = true, isTranslationPending = true),
                        ),
                    )

                    // 2. Wait for the previously started background translation to finish.
                    val slowDeepTranslation = deepTranslationDeferred.await()

                    // 3. Update the UI with the final translated text.
                    emit(
                        ArtworkDetailUiState.Success(
                            slowDeepTranslation.toDetailUi(isTranslated = true, isTranslationPending = false),
                        ),
                    )
                }
            }
        }
        .catch { emit(ArtworkDetailUiState.Error) }
        .stateIn(
            scope = viewModelScope,
            // Keep flow active for 5s after UI unsubscribe (avoids reloads on fast navigation)
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ArtworkDetailUiState.Loading,
        )

    init {
        viewModelScope.launch {
            useCases.syncArtworkDetails(artworkId)
        }
    }

    private fun refresh() {
        if (!messageManager.checkInternetAndNotify()) return
        viewModelScope.launch {
            _isRefreshing.value = true

            val result = useCases.syncArtworkDetails(artworkId)

            result.onFailure { error ->
                messageManager.sendSideEffect(error.toUiText())
            }

            _isRefreshing.value = false
        }
        _contentVersion.value++
        messageManager.resetLastEmittedMessage() // Reset debounce history so new errors can be shown fresh
    }

    private fun toggleTranslation() {
        _showTranslation.value = !_showTranslation.value
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            useCases.toggleFavorite(artworkId)
        }
    }

    private fun downloadImage(url: String, title: String) {
        viewModelScope.launch {
            val result = imageDownloader.downloadImage(url, title)

            result.onSuccess { path ->
                messageManager.sendSideEffect(UiText.StringResource(R.string.details_msg_save_success, path))
            }.onFailure { error ->

                val uiMessage = when (error) {
                    is UnknownHostException, is ConnectException -> {
                        // Network error (and the image is not in the offline cache)
                        UiText.StringResource(DSR.string.core_error_no_internet)
                    }
                    is IOException -> {
                        // Network or disk problems (e.g. no space)
                        UiText.StringResource(R.string.details_error_save_failed)
                    }
                    else -> {
                        // Unknown failure (e.g. MediaStore crashed)
                        UiText.StringResource(R.string.details_error_save_generic)
                    }
                }
                messageManager.sendSideEffect(uiMessage)
            }
        }
    }
}
