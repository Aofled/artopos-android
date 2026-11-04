package ru.createsmart.artopos.core.data.util

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchQueryBuilderTest {

    @Test
    fun `buildArtworkSearchQuery returns empty string when query is blank`() {
        assertEquals("", SearchQueryBuilder.buildArtworkSearchQuery(""))
        assertEquals("", SearchQueryBuilder.buildArtworkSearchQuery("   "))
    }

    @Test
    fun `buildArtworkSearchQuery generates wildcard and exact match for single word`() {
        val result = SearchQueryBuilder.buildArtworkSearchQuery("Monet")

        val expected = "((title:Monet* OR title:Monet) OR " +
            "(people.displayname:Monet* OR people.displayname:Monet) OR " +
            "(person:Monet* OR person:Monet))"

        assertEquals(expected, result)
    }

    @Test
    fun `buildArtworkSearchQuery generates phrase and prefix query for multiple words`() {
        val result = SearchQueryBuilder.buildArtworkSearchQuery("The Gare")

        val expected = "((title:\"The Gare\" OR title:(The* AND Gare*)) OR " +
            "(people.displayname:\"The Gare\" OR people.displayname:(The* AND Gare*)) OR " +
            "(person:\"The Gare\" OR person:(The* AND Gare*)))"

        assertEquals(expected, result)
    }

    @Test
    fun `buildArtworkSearchQuery normalizes multiple spaces between words`() {
        val result = SearchQueryBuilder.buildArtworkSearchQuery("   Claude    Monet   ")

        val expected = "((title:\"Claude Monet\" OR title:(Claude* AND Monet*)) OR " +
            "(people.displayname:\"Claude Monet\" OR people.displayname:(Claude* AND Monet*)) OR " +
            "(person:\"Claude Monet\" OR person:(Claude* AND Monet*)))"

        assertEquals(expected, result)
    }

    @Test
    fun `buildArtworkSearchQuery escapes reserved elasticsearch characters in single word`() {
        val result = SearchQueryBuilder.buildArtworkSearchQuery("cat:")

        val expected = """((title:cat\:* OR title:cat\:) OR """ +
            """(people.displayname:cat\:* OR people.displayname:cat\:) OR """ +
            """(person:cat\:* OR person:cat\:))"""

        assertEquals(expected, result)
    }

    @Test
    fun `buildArtworkSearchQuery escapes reserved characters in multi-word query`() {
        val result = SearchQueryBuilder.buildArtworkSearchQuery("oil: (canvas) + 1880")

        val expected = """((title:"oil\: \(canvas\) \+ 1880" OR """ +
            """title:(oil\:* AND \(canvas\)* AND \+* AND 1880*)) OR """ +
            """(people.displayname:"oil\: \(canvas\) \+ 1880" OR """ +
            """people.displayname:(oil\:* AND \(canvas\)* AND \+* AND 1880*)) OR """ +
            """(person:"oil\: \(canvas\) \+ 1880" OR """ +
            """person:(oil\:* AND \(canvas\)* AND \+* AND 1880*)))"""

        assertEquals(expected, result)
    }
}
