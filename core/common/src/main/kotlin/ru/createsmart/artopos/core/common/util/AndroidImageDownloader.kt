package ru.createsmart.artopos.core.common.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.graphics.drawable.toBitmap
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

private const val BITMAP_QUALITY = 100

class AndroidImageDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun downloadImage(url: String, fileName: String): Result<String> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                // 1. COIL gives us a picture (from cache or network)
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .allowHardware(false)
                    .build()

                val result = context.imageLoader.execute(request)

                if (result !is SuccessResult) {
                    return@withContext Result.failure(Exception("Failed to load image from Coil"))
                }

                val bitmap = result.drawable.toBitmap()

                // 2. Preparing metadata for Android MediaStore (Galleries)
                val resolver = context.contentResolver

                // Clear the name from invalid characters
                val safeFileName = fileName.replace(Regex("[^a-zA-Z0-9.\\-]"), "_")

                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$safeFileName.jpg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")

                    // Starting with Android 10 (API 29), we required use MediaStore to write to shared folders
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/Artopos",
                        )
                        /**
                         * The IS_PENDING flag tells the system that the file is still being written,
                         * and should not be shown to other applications.
                         * */
                        put(MediaStore.MediaColumns.IS_PENDING, 1)
                    }
                }

                val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

                // 3. Create an "empty" entry in the Gallery
                val imageUri = resolver.insert(collection, contentValues)
                    ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

                // 4. Open a stream to this URI and write our downloaded bytes there.
                resolver.openOutputStream(imageUri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, BITMAP_QUALITY, outputStream)
                } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

                // 5. If we set IS_PENDING = 1, now we remove it (the file is ready)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(imageUri, contentValues, null, null)
                }

                val displayPath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    "Pictures/Artopos/$safeFileName.jpg"
                } else {
                    "Gallery"
                }

                Result.success(displayPath)
            } catch (e: IOException) {
                Result.failure(e)
            } catch (e: IllegalArgumentException) {
                Result.failure(e)
            } catch (e: IllegalStateException) {
                Result.failure(e)
            }
        }
}
