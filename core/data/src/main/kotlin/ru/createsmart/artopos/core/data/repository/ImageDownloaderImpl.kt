package ru.createsmart.artopos.core.data.repository

import ru.createsmart.artopos.core.common.util.AndroidImageDownloader
import ru.createsmart.artopos.core.domain.repository.ImageDownloader
import javax.inject.Inject

class ImageDownloaderImpl @Inject constructor(
    private val androidDownloader: AndroidImageDownloader,
) : ImageDownloader {

    override suspend fun downloadImage(url: String, fileName: String): Result<String> {
        return androidDownloader.downloadImage(url, fileName)
    }
}
