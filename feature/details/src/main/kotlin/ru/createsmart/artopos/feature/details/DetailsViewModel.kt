package ru.createsmart.artopos.feature.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import ru.createsmart.artopos.core.designsystem.components.toUiText
import ru.createsmart.artopos.core.designsystem.manager.UiMessageManager
import ru.createsmart.artopos.core.domain.usecase.GetArtworkDetailsUseCase
import ru.createsmart.artopos.core.domain.usecase.GetUserSettingsUseCase
import ru.createsmart.artopos.core.domain.usecase.SyncArtworkDetailsUseCase
import ru.createsmart.artopos.core.navigation.DetailsRoute
import ru.createsmart.artopos.feature.details.mapper.toDetailUi
import ru.createsmart.artopos.feature.details.translation.ArtworkTranslationFacade
import java.util.Locale
import javax.inject.Inject

private const val TRANSLATION_TIMEOUT_MS = 300L

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getArtworkDetails: GetArtworkDetailsUseCase,
    getUserSettings: GetUserSettingsUseCase,
    private val syncArtworkDetails: SyncArtworkDetailsUseCase,
    private val messageManager: UiMessageManager,
    private val translationFacade: ArtworkTranslationFacade,
) : ViewModel() {
    private val routeArgs = savedStateHandle.toRoute<DetailsRoute>()
    private val artworkId = routeArgs.artworkId

    private val _contentVersion = MutableStateFlow(0) // To update images (bad internet)
    val contentVersion = _contentVersion.asStateFlow()

    val uiEffect = messageManager.uiEffect

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _showTranslation = MutableStateFlow(true)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ArtworkDetailUiState> = combine(
        getArtworkDetails(artworkId),
        getUserSettings(),
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
                        ),
                    ),
                )
                return@transformLatest
            }

            // Get basic data (No heavy ML involved), передаем languageCode
            val fastTranslatedArtwork = translationFacade.translateFast(rawArtwork, languageCode)

            val quickDeepTranslation = withTimeoutOrNull(TRANSLATION_TIMEOUT_MS) {
                translationFacade.translateDeep(rawArtwork, fastTranslatedArtwork, languageCode)
            }

            if (quickDeepTranslation != null) {
                // Scenario A: Fast device/cache.
                // Show final result immediately. Avoids UI flickering (Fast -> Deep).
                emit(ArtworkDetailUiState.Success(quickDeepTranslation.toDetailUi(isTranslated = true)))
            } else {
                // Scenario B: Slow translation.
                // 1. Show "Fast" version first (Partial/Original text) so user sees content instantly.
                emit(ArtworkDetailUiState.Success(fastTranslatedArtwork.toDetailUi(isTranslated = true)))

                val slowDeepTranslation = translationFacade.translateDeep(
                    rawArtwork,
                    fastTranslatedArtwork,
                    languageCode,
                )
                emit(ArtworkDetailUiState.Success(slowDeepTranslation.toDetailUi(isTranslated = true)))
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
            syncArtworkDetails(artworkId)
        }
    }

    fun onRefresh() {
        if (!messageManager.checkInternetAndNotify()) return
        viewModelScope.launch {
            _isRefreshing.value = true

            val result = syncArtworkDetails(artworkId)

            result.onFailure { error ->
                messageManager.sendSideEffect(error.toUiText())
            }

            _isRefreshing.value = false
        }
        _contentVersion.value++
        messageManager.resetLastEmittedMessage() // Reset debounce history so new errors can be shown fresh
    }

    fun toggleTranslation() {
        _showTranslation.value = !_showTranslation.value
    }
}
