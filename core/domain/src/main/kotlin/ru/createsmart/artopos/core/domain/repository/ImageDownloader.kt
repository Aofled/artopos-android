package ru.createsmart.artopos.core.domain.repository

public interface ImageDownloader {
    public suspend fun downloadImage(url: String, fileName: String): Result<String>
}
