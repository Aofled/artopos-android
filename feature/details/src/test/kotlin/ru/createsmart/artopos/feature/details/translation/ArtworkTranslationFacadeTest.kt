package ru.createsmart.artopos.feature.details.translation

import android.content.Context
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.createsmart.artopos.core.common.util.LocaleHelper
import ru.createsmart.artopos.core.designsystem.util.FilterNameHelper
import ru.createsmart.artopos.core.domain.translation.TextTranslator
import ru.createsmart.artopos.core.model.Artwork
import ru.createsmart.artopos.core.model.ArtworkDetails

class ArtworkTranslationFacadeTest {

    private val mockContext: Context = mockk()
    private val mockLocalizedContext: Context = mockk()
    private val translator: TextTranslator = mockk()

    private lateinit var facade: ArtworkTranslationFacade

    @Before
    fun setup() {
        mockkObject(LocaleHelper)
        mockkObject(FilterNameHelper)

        every { LocaleHelper.getLocalizedContext(any(), any()) } returns mockLocalizedContext

        facade = ArtworkTranslationFacade(mockContext, translator)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // --- TESTS FOR translateFast (DICTIONARY) ---

    @Test
    fun `translateFast uses dictionary for classification, century, culture`() {
        // GIVEN
        val original = createEmptyArtwork().copy(
            classification = "Paintings",
            century = "19th century",
            culture = "French",
        )

        // GIVEN
        every { FilterNameHelper.getLocalizedName(mockLocalizedContext, "Paintings") } returns "Живопись"
        every { FilterNameHelper.getLocalizedName(mockLocalizedContext, "19th century") } returns "19 век"
        every { FilterNameHelper.getLocalizedName(mockLocalizedContext, "French") } returns "Французская"

        // WHEN
        val result = facade.translateFast(original, "ru")

        // THEN
        assertEquals("Живопись", result.classification)
        assertEquals("19 век", result.century)
        assertEquals("Французская", result.culture)
    }

    // --- TESTS FOR translateDeep (HYBRID LOGIC) ---

    @Test
    fun `translateDeep DOES NOT call ML Kit for medium if dictionary translated it`() = runTest {
        // GIVEN
        val original = createEmptyArtwork().copy(medium = "Oil on canvas")

        // GIVEN
        val fast = createEmptyArtwork().copy(medium = "Масло на холсте")

        coEvery { translator.translate(any(), any()) } returns "ML_KIT_TRANSLATION"

        // WHEN
        val result = facade.translateDeep(original, fast, "ru")

        // THEN
        assertEquals("Масло на холсте", result.medium)
        coVerify(exactly = 0) { translator.translate("Oil on canvas", any()) }
    }

    @Test
    fun `translateDeep CALLS ML Kit for medium if dictionary failed`() = runTest {
        // GIVEN
        val original = createEmptyArtwork().copy(medium = "Unknown Medium")

        // GIVEN
        val fast = createEmptyArtwork().copy(medium = "Unknown Medium")

        coEvery { translator.translate("Unknown Medium", "ru") } returns "Неизвестный материал"
        coEvery { translator.translate(null, any()) } returns null

        // WHEN
        val result = facade.translateDeep(original, fast, "ru")

        // THEN
        assertEquals("Неизвестный материал", result.medium)
        coVerify(exactly = 1) { translator.translate("Unknown Medium", "ru") }
    }

    private fun createEmptyArtwork(): ArtworkDetails {
        val base = Artwork(
            id = 1,
            title = "",
            artist = "",
            imageUrl = "",
            imageDimensions = null,
            date = null,
            yearInt = null,
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
