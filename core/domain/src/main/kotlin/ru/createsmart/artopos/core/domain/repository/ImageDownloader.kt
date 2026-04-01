package ru.createsmart.artopos.core.domain.repository

interface ImageDownloader {
    suspend fun downloadImage(url: String, fileName: String): Result<String>
}
