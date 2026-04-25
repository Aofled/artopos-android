package ru.createsmart.artopos.core.network.util

internal object TestResourceReader {
    fun loadJson(fileName: String): String {
        val inputStream = javaClass.classLoader?.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("Test file not found: $fileName. Make sure it is in src/test/resources/")
        // `use` ensures that the stream is automatically closed (prevents memory leaks).
        return inputStream.bufferedReader().use { it.readText() }
    }
}
