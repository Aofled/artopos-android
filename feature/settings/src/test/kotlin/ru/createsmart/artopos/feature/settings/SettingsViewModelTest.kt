package ru.createsmart.artopos.feature.settings

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import ru.createsmart.artopos.core.designsystem.components.UiText
import ru.createsmart.artopos.core.domain.interactor.SettingsInteractor
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import ru.createsmart.artopos.core.uicomponents.manager.UiMessageManager
import ru.createsmart.artopos.feature.settings.model.SettingsIntent
import ru.createsmart.artopos.feature.settings.util.MainDispatcherRule

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val useCases: SettingsInteractor = mockk(relaxed = true)
    private val uiMessageManager: UiMessageManager = mockk(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    private fun setupViewModel() {
        viewModel = SettingsViewModel(
            useCases = useCases,
            uiMessageManager = uiMessageManager,
        )
    }

    @Test
    fun `uiState emits Loading initially, then Success when interactor emits data`() = runTest {
        // GIVEN
        val settingsFlow = MutableSharedFlow<UserSettings>()
        every { useCases.getUserSettings() } returns settingsFlow

        setupViewModel()

        viewModel.uiState.test {
            // THEN
            assertEquals(SettingsUiState.Loading, awaitItem())

            // WHEN
            val mockSettings = UserSettings(ThemeConfig.DARK, "ru")
            settingsFlow.emit(mockSettings)

            // THEN
            val successState = awaitItem() as SettingsUiState.Success
            assertEquals(ThemeConfig.DARK, successState.settings.themeConfig)
            assertEquals("ru", successState.settings.languageCode)
        }
    }

    @Test
    fun `onIntent UpdateTheme calls interactor with correct theme`() = runTest {
        // GIVEN
        every { useCases.getUserSettings() } returns MutableSharedFlow()
        setupViewModel()

        // WHEN
        viewModel.onIntent(SettingsIntent.UpdateTheme(ThemeConfig.LIGHT))

        // THEN
        coVerify(exactly = 1) { useCases.setThemeConfig(ThemeConfig.LIGHT) }
    }

    @Test
    fun `onIntent UpdateLanguage calls interactor with correct language`() = runTest {
        // GIVEN
        every { useCases.getUserSettings() } returns MutableSharedFlow()
        setupViewModel()

        // WHEN
        viewModel.onIntent(SettingsIntent.UpdateLanguage("fr"))

        // THEN
        coVerify(exactly = 1) { useCases.setLanguage("fr") }
    }

    @Test
    fun `calculateCacheSize updates cacheSizeMb state correctly`() = runTest {
        // GIVEN
        every { useCases.getUserSettings() } returns MutableSharedFlow()
        coEvery { useCases.getImageCacheSizeUseCase() } returns (5L * 1024 * 1024) // 5 MB

        setupViewModel()

        // WHEN
        viewModel.calculateCacheSize()

        // THEN
        viewModel.cacheSizeMb.test {
            val firstItem = awaitItem()
            if (firstItem == null) {
                // Ждем следующее значение (5L)
                assertEquals(5L, awaitItem())
            } else {
                assertEquals(5L, firstItem)
            }
        }
    }

    @Test
    fun `onIntent ClearCache sends correct success messages and recalculates size`() = runTest {
        // GIVEN
        every { useCases.getUserSettings() } returns MutableSharedFlow()

        coEvery { useCases.clearAppCacheUseCase() } returns (10L * 1024 * 1024) // 10 MB
        coEvery { useCases.getImageCacheSizeUseCase() } returns 0L // 0 MB

        setupViewModel()

        // WHEN
        viewModel.onIntent(SettingsIntent.ClearCache)

        // THEN
        // "Clearing..."
        coVerify { uiMessageManager.sendSideEffect(UiText.StringResource(R.string.settings_msg_cache_clearing)) }
        // Cleared 10 MB
        coVerify { uiMessageManager.sendSideEffect(UiText.StringResource(R.string.settings_msg_cache_cleared, 10L)) }

        coVerify(exactly = 1) { useCases.getImageCacheSizeUseCase() }
    }
}
