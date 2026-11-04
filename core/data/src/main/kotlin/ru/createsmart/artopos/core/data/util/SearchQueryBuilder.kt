package ru.createsmart.artopos.core.data.util

/**
 * Builds safe Elasticsearch query_string parameters for Harvard Art Museums API.
 * Escapes reserved ES operators to prevent 400 Bad Request errors.
 */
internal object SearchQueryBuilder {

    // Elasticsearch reserved characters: + - = && || > < ! ( ) { } [ ] ^ " ~ * ? : \ /
    private val ES_RESERVED_CHARS_REGEX = Regex("""([+\-=&|><!(){}\[\]^"~*?:\\/])""")

    /**
     * Escapes Elasticsearch query_string special characters with a backslash.
     */
    fun sanitize(input: String): String {
        return input.trim()
            .replace(ES_RESERVED_CHARS_REGEX, """\\$1""")
    }

    /**
     * Constructs a search query that supports:
     * 1. Prefix-based word searching (using the '*' wildcard): e.g.,
     *    "Mone" will match "Monet", and "The Gar" will match "The Gare".
     * 2. Phrase searching (using quotation marks): allows multi-word phrases to be searched for as a single substring.
     */
    fun buildArtworkSearchQuery(rawQuery: String): String {
        val trimmed = rawQuery.trim()
        if (trimmed.isEmpty()) return ""

        val words = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }

        // Sanitize special characters in each word
        val sanitizedWords = words.map { sanitize(it) }

        // 1. Prefix search for words
        val prefixQuery = sanitizedWords.joinToString(" AND ") { "$it*" }

        // 2. Exact phrase for multi-word query
        val isMultiWord = sanitizedWords.size > 1
        val phraseQuery = "\"${sanitizedWords.joinToString(" ")}\""

        return if (isMultiWord) {
            // Multi-word: search exact phrase OR prefix match
            "((title:$phraseQuery OR title:($prefixQuery)) OR " +
                "(people.displayname:$phraseQuery OR people.displayname:($prefixQuery)) OR " +
                "(person:$phraseQuery OR person:($prefixQuery)))"
        } else {
            // Single word: search token with prefix
            val singleWord = sanitizedWords.first()
            "((title:$singleWord* OR title:$singleWord) OR " +
                "(people.displayname:$singleWord* OR people.displayname:$singleWord) OR " +
                "(person:$singleWord* OR person:$singleWord))"
        }
    }
}
