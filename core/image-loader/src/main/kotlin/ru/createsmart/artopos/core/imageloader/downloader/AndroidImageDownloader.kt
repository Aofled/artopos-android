package ru.createsmart.artopos.core.imageloader.downloader

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import coil3.ImageLoader
import coil3.Uri
import coil3.disk.DiskCache
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject

private const val BITMAP_QUALITY = 100

class AndroidImageDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
) {

    suspend fun downloadImage(url: String, fileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            var imageUri: Uri? = null

            try {
                // 1. Getting information about the file
                val (fullFileName, mimeType) = getFileInfo(url, fileName)

                // 2. Coil 3 ImageLoader & Request
                val request = ImageRequest.Builder(context)
                    .data(url)
                    // Disable hardware bitmaps so we can compress and save bytes to storage.
                    .allowHardware(false)
                    .build()

                val result = imageLoader.execute(request)
                if (result !is SuccessResult) {
                    return@withContext Result.failure(IOException("Failed to load image from Coil"))
                }

                // 3. Gallery Metadata
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fullFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/Artopos",
                        )
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                val imageUri = resolver.insert(collection, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

                // 4. Recording
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    writeImageToStream(
                        result = result,
                        diskCache = imageLoader.diskCache,
                        mimeType = mimeType,
                        outputStream = outputStream,
                    )
                } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

                // 5. Unlock the file
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    // Clear pending flag to make image visible in gallery.
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }

                val displayPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    "Pictures/Artopos/$fullFileName"
                } else {
                    "Gallery"
                }

                Result.success(displayPath)
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: SecurityException) {
                Result.failure(e)
            } catch (e: IllegalArgumentException) {
                Result.failure(e)
            }
        }

    private fun getFileInfo(url: String, fileName: String): Pair<String, String> {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(url)
            ?.takeIf { it.isNotBlank() } ?: "jpg"
        val mimeType = MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(fileExtension.lowercase()) ?: "image/jpeg"

        val cleanFileName = fileName.replace(Regex("[^a-zA-Z0-9.\\-]"), "_")
        return Pair("$cleanFileName.$fileExtension", mimeType)
    }

    private fun writeImageToStream(
        result: SuccessResult,
        diskCache: DiskCache?,
        mimeType: String,
        outputStream: OutputStream,
    ) {
        val snapshot = result.diskCacheKey?.let { diskCache?.openSnapshot(it) }

        if (snapshot != null) {
            // Fast path: copy disk cache file directly to stream.
            snapshot.use { snap ->
                File(snap.data.toString()).inputStream().use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } else {
            // Slow path: decode image to bitmap and re-encode.
            val bitmap = result.image.toBitmap()
            val compressFormat = if (mimeType.contains("png", ignoreCase = true)) {
                Bitmap.CompressFormat.PNG
            } else {
                Bitmap.CompressFormat.JPEG
            }
            bitmap.compress(compressFormat, BITMAP_QUALITY, outputStream)
        }
    }
}
