package pers.zy.jenny.utils

import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import pers.zy.jenny.MyApp
import java.io.*

/**
 * @author: zy
 * @date: 2024/4/23
 */
object FileUtil {

  private val EXTERNAL_FILES_DIR: File?
    get() = MyApp.INSTANCE.getExternalFilesDir("GFZY")

  init {
    Log.d("GFZY", "clear")
    EXTERNAL_FILES_DIR?.deleteRecursively()
  }

  suspend fun saveUriToFile(
      uri: Uri,
      outputFile: File,
      success: suspend (file: File) -> Unit
  ) {
    val contentResolver: ContentResolver = MyApp.INSTANCE.contentResolver
    saveUriToFile(contentResolver.openInputStream(uri), outputFile, success)
  }

  suspend fun saveUriToFile(
      inputStream: InputStream?,
      outputFile: File,
      success: suspend (file: File) -> Unit
  ) {
    inputStream?.use { input ->
      val outputStream = FileOutputStream(outputFile)
      outputStream.use { output ->
        val bufferedInputStream = BufferedInputStream(input)
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (bufferedInputStream.read(buffer).also { bytesRead = it } != -1) {
          output.write(buffer, 0, bytesRead)
        }
        output.flush()
      }
      success(outputFile)
    }
  }

  fun getMimeTypeFromUri(uri: Uri): String? {
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
    return File(EXTERNAL_FILES_DIR, name).also {
      if (it.parentFile != null && !it.parentFile!!.exists()) {
        it.parentFile!!.mkdirs()
      }
      if (!it.exists()) {
        it.createNewFile()
      }
    }
  }
}