package ru.createsmart.artopos.feature.discover.ui.preview

import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.feature.discover.model.FilterListItem

object FilterPreviewData {

    val classifications = listOf(
        FilterListItem("1", FilterType.CLASSIFICATION, "Paintings", "Paintings", 12500, isSelected = true),
        FilterListItem("2", FilterType.CLASSIFICATION, "Prints", "Prints", 8400),
        FilterListItem("3", FilterType.CLASSIFICATION, "Drawings", "Drawings", 6200),
        FilterListItem("4", FilterType.CLASSIFICATION, "Photographs", "Photographs", 4100),
        FilterListItem("5", FilterType.CLASSIFICATION, "Sculpture", "Sculpture", 2300),
        FilterListItem("6", FilterType.CLASSIFICATION, "Coins", "Coins", 1200),
        FilterListItem("7", FilterType.CLASSIFICATION, "Vessels", "Vessels", 800),
        FilterListItem("8", FilterType.CLASSIFICATION, "Textile", "Textile", 450),
        FilterListItem("9", FilterType.CLASSIFICATION, "Jewelry", "Jewelry", 120),
    )

    val centuries = listOf(
        FilterListItem("10", FilterType.CENTURY, "19th century", "19th century", 53000),
        FilterListItem("11", FilterType.CENTURY, "20th century", "20th century", 48000),
        FilterListItem("12", FilterType.CENTURY, "18th century", "18th century", 21000, isSelected = true),
        FilterListItem("13", FilterType.CENTURY, "17th century", "17th century", 15000),
        FilterListItem("14", FilterType.CENTURY, "16th century", "16th century", 8000),
        FilterListItem("15", FilterType.CENTURY, "15th century", "15th century", 5000),
        FilterListItem("16", FilterType.CENTURY, "14th century", "14th century", 2000),
        FilterListItem("17", FilterType.CENTURY, "13th century", "13th century", 1000),
        FilterListItem("18", FilterType.CENTURY, "12th century", "12th century", 500),
    )

    val cultures = listOf(
        FilterListItem("20", FilterType.CULTURE, "American", "American", 25000),
        FilterListItem("21", FilterType.CULTURE, "French", "French", 18000, isSelected = true),
        FilterListItem("22", FilterType.CULTURE, "British", "British", 12000),
        FilterListItem("23", FilterType.CULTURE, "Japanese", "Japanese", 9000),
        FilterListItem("24", FilterType.CULTURE, "Chinese", "Chinese", 8500),
        FilterListItem("25", FilterType.CULTURE, "Italian", "Italian", 7000),
        FilterListItem("26", FilterType.CULTURE, "German", "German", 6000),
        FilterListItem("27", FilterType.CULTURE, "Dutch", "Dutch", 4000),
        FilterListItem("28", FilterType.CULTURE, "Spanish", "Spanish", 3000),
    )
}
