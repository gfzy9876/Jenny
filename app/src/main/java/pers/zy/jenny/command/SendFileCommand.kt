package pers.zy.jenny.command

import android.os.Handler
import android.os.Looper
import com.google.common.util.concurrent.AtomicDouble
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.source
import pers.zy.jenny.net.Response
import java.io.File
import java.text.DecimalFormat

/**
 * socket方式传输文件
 * */
class SendFileCommand(private val file: File, override val commandIdentify: CommandIdentify) : ICommand {

  private val mainHandler = Handler(Looper.getMainLooper())
  private val formatter = DecimalFormat("#.#")

  override suspend fun createSuspendFun(progress: (Float) -> Unit): Response {
    return if (commandIdentify == CommandIdentify.SEND_IMAGE_BYTE) {
      API.commandSendImageByte(
          MultipartBody.Part.createFormData(
              "photo",
              file.name,
              createRequestBody(file, progress)
          )
      )
    } else {
      API.commandSendVideoByte(
          MultipartBody.Part.createFormData(
              "video",
              file.name,
              createRequestBody(file, progress)
          )
      )
    }
  }

  private fun createRequestBody(file: File, progress: (Float) -> Unit): RequestBody = object : RequestBody() {
    private val lastProgress = AtomicDouble(0.0)

    override fun contentType() = "multipart/form-data".toMediaTypeOrNull()

    override fun writeTo(sink: BufferedSink) {
      file.source().use { source ->
        var read: Long
        var total = 0L
        val tmpBuffer = Buffer()
        while (source.read(tmpBuffer, 2048).also {
              read = it
              total += read
              val newProgress = total * 1f / file.length() * 100f
              if (newProgress - lastProgress.get() > 1f) {
                mainHandler.post {
                  progress(formatter.format(newProgress).toFloat())
                }
                lastProgress.set(newProgress.toDouble())
              }
            } != -1L) {
          sink.write(tmpBuffer, read)
        }
      }
    }

    override fun contentLength(): Long = file.length()
  }
}