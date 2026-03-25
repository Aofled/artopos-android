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
import ru.createsmart.artopos.core.domain.repository.SettingsRepository
import ru.createsmart.artopos.core.domain.usecase.ClearImageCacheUseCase
import ru.createsmart.artopos.core.domain.usecase.GetImageCacheSizeUseCase
import ru.createsmart.artopos.core.model.settings.ThemeConfig
import ru.createsmart.artopos.core.model.settings.UserSettings
import ru.createsmart.artopos.core.designsystem.manager.UiMessageManager
import ru.createsmart.artopos.feature.settings.util.MainDispatcherRule

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository: SettingsRepository = mockk(relaxed = true)
    private val clearImageCacheUseCase: ClearImageCacheUseCase = mockk()
    private val getImageCacheSizeUseCase: GetImageCacheSizeUseCase = mockk()
    private val uiMessageManager: UiMessageManager = mockk(relaxed = true)

    private lateinit var viewModel: SettingsViewModel

    private fun setupViewModel() {
        viewModel = SettingsViewModel(
            settingsRepository,
            clearImageCacheUseCase,
            getImageCacheSizeUseCase,
            uiMessageManager,
        )
    }

    @Test
    fun `uiState emits Loading initially, then Success when repository emits data`() = runTest {
        // GIVEN
        val settingsFlow = MutableSharedFlow<UserSettings>()
        every { settingsRepository.userSettingsStream } returns settingsFlow

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
    fun `updateTheme calls repository with correct theme`() = runTest {
        // GIVEN
        every { settingsRepository.userSettingsStream } returns MutableSharedFlow()
        setupViewModel()

        // WHEN
        viewModel.updateTheme(ThemeConfig.LIGHT)

        // THEN
        coVerify(exactly = 1) { settingsRepository.setThemeConfig(ThemeConfig.LIGHT) }
    }

    @Test
    fun `calculateCacheSize updates cacheSizeMb state correctly`() = runTest {
        // GIVEN
        every { settingsRepository.userSettingsStream } returns MutableSharedFlow()
        coEvery { getImageCacheSizeUseCase() } returns (5L * 1024 * 1024) // 5 MB

        setupViewModel()

        // WHEN
        viewModel.calculateCacheSize()

        // THEN
        viewModel.cacheSizeMb.test {
            assertEquals(5L, awaitItem())
        }
    }

    @Test
    fun `clearImageCache sends correct success messages and recalculates size`() = runTest {
        // GIVEN
        every { settingsRepository.userSettingsStream } returns MutableSharedFlow()

        coEvery { clearImageCacheUseCase() } returns (10L * 1024 * 1024) // 10 MB
        coEvery { getImageCacheSizeUseCase() } returns 0L // 0 MB

        setupViewModel()

        // WHEN
        viewModel.clearImageCache()

        // THEN
        // "Clearing..."
        coVerify { uiMessageManager.sendSideEffect(UiText.StringResource(R.string.msg_clearing_cache)) }
        // Cleared 10 MB
        coVerify { uiMessageManager.sendSideEffect(UiText.StringResource(R.string.msg_cache_cleared, 10L)) }

        // THEN
        coVerify(exactly = 1) { getImageCacheSizeUseCase() }
    }
}
