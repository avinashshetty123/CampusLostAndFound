package com.example.lostandfoundfrontend.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    private const val MAX_DIMENSION = 1600

    /**
     * Copies a picked image into a cache file ready for upload, off the main thread.
     * On Android 9+ it is downscaled and re-encoded as JPEG (ImageDecoder also applies
     * EXIF rotation), which keeps phone photos well under the server's 10MB limit.
     */
    suspend fun prepareForUpload(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
        try {
            val out = File.createTempFile("upload_", ".jpg", context.cacheDir)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                    val longest = maxOf(info.size.width, info.size.height)
                    if (longest > MAX_DIMENSION) {
                        val scale = MAX_DIMENSION.toFloat() / longest
                        decoder.setTargetSize(
                            (info.size.width * scale).toInt().coerceAtLeast(1),
                            (info.size.height * scale).toInt().coerceAtLeast(1)
                        )
                    }
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
                FileOutputStream(out).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 85, it) }
                bitmap.recycle()
            } else {
                val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
                input.use { stream -> FileOutputStream(out).use { stream.copyTo(it) } }
            }
            out
        } catch (e: Exception) {
            null
        }
    }
}
