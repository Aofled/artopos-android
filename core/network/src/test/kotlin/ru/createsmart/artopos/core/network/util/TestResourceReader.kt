package ru.createsmart.artopos.core.network.util

import java.io.File

internal object TestResourceReader {
    fun loadJson(fileName: String): String {
        val classLoader = javaClass.classLoader
        val resource = classLoader?.getResource(fileName)
            ?: throw IllegalArgumentException("Test file not found: $fileName. Make sure it is in src/test/resources/")
        return File(resource.path).readText()
    }
}
