package ru.createsmart.artopos.feature.discover.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.createsmart.artopos.core.model.FilterItem
import ru.createsmart.artopos.core.model.FilterType

class FilterUiMapperTest {

    @Test
    fun `map filter item to ui sets isSelected to true when name matches`() {
        val item = FilterItem(1, 123, FilterType.CENTURY, "19th century", 100)

        val uiItem = item.toUi("19th century")

        assertTrue(uiItem.isSelected)
        assertEquals("19th century", uiItem.name)
    }

    @Test
    fun `map filter item to ui sets isSelected to false when name does not match`() {
        val item = FilterItem(1, 123, FilterType.CENTURY, "19th century", 100)

        val uiItem = item.toUi("20th century")

        assertFalse(uiItem.isSelected)
    }

    @Test
    fun `map filter item to ui sets isSelected to false when selection is null`() {
        val item = FilterItem(1, 123, FilterType.CENTURY, "19th century", 100)

        val uiItem = item.toUi(null)

        assertFalse(uiItem.isSelected)
    }

    @Test
    fun `map list to ui marks correct item as selected`() {
        val list = listOf(
            FilterItem(1, 1, FilterType.CULTURE, "French", 10),
            FilterItem(2, 2, FilterType.CULTURE, "American", 20),
            FilterItem(3, 3, FilterType.CULTURE, "British", 30),
        )

        val uiList = list.toUi("American")

        assertFalse(uiList[0].isSelected)
        assertTrue(uiList[1].isSelected) // American
        assertFalse(uiList[2].isSelected)
    }
}
