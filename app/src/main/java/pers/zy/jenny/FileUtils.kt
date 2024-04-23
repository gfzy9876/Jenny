package pers.zy.jenny

import android.content.ContentResolver
import android.net.Uri
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

/**
 * @author: zy
 * @date: 2024/4/23
 */
object FileUtils {

  suspend fun saveUriToFile(
      uri: Uri,
      outputFile: File,
      loading: (progress: Float) -> Unit,
      success: (file: File) -> Unit
  ) {
    val contentResolver: ContentResolver = MyApp.INSTANCE.contentResolver
    val inputStream = contentResolver.openInputStream(uri)
    inputStream?.use { input ->
      val outputStream = FileOutputStream(outputFile)
      outputStream.use { output ->
        val bufferedInputStream = BufferedInputStream(input)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (bufferedInputStream.read(buffer).also {
              bytesRead = it
              loading(min(it * 1f / input.available(), 1f))
            } != -1) {
          output.write(buffer, 0, bytesRead)
        }
        output.flush()
      }
      success(outputFile)
    }
  }

  suspend fun getMimeTypeFromUri(uri: Uri): String? {
    val contentResolver: ContentResolver = MyApp.INSTANCE.contentResolver
    val type = contentResolver.getType(uri) ?: return ""
    return try {
      if (type.startsWith("image")) {
        ".png"
      } else if (type.startsWith("video")) {
        return ".mp4"
      } else if (type.startsWith("text")) {
        ".txt"
      } else if (type.startsWith("application/pdf")) {
        ".pdf"
      } else {
        ""
      }
    } catch (e: IOException) {
      e.printStackTrace()
      ""
    }
  }

  fun createFile(name: String): File {
    return File(MyApp.INSTANCE.getExternalFilesDir("GFZY"), name).also {
      if (it.parentFile != null && !it.parentFile!!.exists()) {
        it.parentFile!!.mkdirs()
      }
      if (!it.exists()) {
        it.createNewFile()
      }
    }
  }
}