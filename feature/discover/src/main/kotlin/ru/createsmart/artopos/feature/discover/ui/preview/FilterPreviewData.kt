package ru.createsmart.artopos.feature.discover.ui.preview

import ru.createsmart.artopos.core.model.FilterType
import ru.createsmart.artopos.feature.discover.model.FilterListItem

object FilterPreviewData {

    val classifications = listOf(
        FilterListItem("1", 26L, FilterType.CLASSIFICATION, "Paintings", "Paintings", 12500, isSelected = true),
        FilterListItem("2", 23L, FilterType.CLASSIFICATION, "Prints", "Prints", 8400),
        FilterListItem("3", 21L, FilterType.CLASSIFICATION, "Drawings", "Drawings", 6200),
        FilterListItem("4", 17L, FilterType.CLASSIFICATION, "Photographs", "Photographs", 4100),
        FilterListItem("5", 30L, FilterType.CLASSIFICATION, "Sculpture", "Sculpture", 2300),
        FilterListItem("6", 50L, FilterType.CLASSIFICATION, "Coins", "Coins", 1200),
        FilterListItem("7", 57L, FilterType.CLASSIFICATION, "Vessels", "Vessels", 800),
        FilterListItem("8", 62L, FilterType.CLASSIFICATION, "Textile", "Textile", 450),
        FilterListItem("9", 19L, FilterType.CLASSIFICATION, "Jewelry", "Jewelry", 120),
    )

    val centuries = listOf(
        FilterListItem("10", 37525806L, FilterType.CENTURY, "19th century", "19th century", 53000),
        FilterListItem("11", 37525815L, FilterType.CENTURY, "20th century", "20th century", 48000),
        FilterListItem("12", 37525797L, FilterType.CENTURY, "18th century", "18th century", 21000, isSelected = true),
        FilterListItem("13", 37525788L, FilterType.CENTURY, "17th century", "17th century", 15000),
        FilterListItem("14", 37525779L, FilterType.CENTURY, "16th century", "16th century", 8000),
        FilterListItem("15", 37525770L, FilterType.CENTURY, "15th century", "15th century", 5000),
        FilterListItem("16", 37525761L, FilterType.CENTURY, "14th century", "14th century", 2000),
        FilterListItem("17", 37525752L, FilterType.CENTURY, "13th century", "13th century", 1000),
        FilterListItem("18", 37525743L, FilterType.CENTURY, "12th century", "12th century", 500),
    )

    val cultures = listOf(
        FilterListItem("20", 37526778L, FilterType.CULTURE, "American", "American", 25000),
        FilterListItem("21", 37527426L, FilterType.CULTURE, "French", "French", 18000, isSelected = true),
        FilterListItem("22", 37527039L, FilterType.CULTURE, "British", "British", 12000),
        FilterListItem("23", 37527795L, FilterType.CULTURE, "Japanese", "Japanese", 9000),
        FilterListItem("24", 37527174L, FilterType.CULTURE, "Chinese", "Chinese", 8500),
        FilterListItem("25", 37527759L, FilterType.CULTURE, "Italian", "Italian", 7000),
        FilterListItem("26", 37527453L, FilterType.CULTURE, "German", "German", 6000),
        FilterListItem("27", 37527300L, FilterType.CULTURE, "Dutch", "Dutch", 4000),
        FilterListItem("28", 37528659L, FilterType.CULTURE, "Spanish", "Spanish", 3000),
    )
}
