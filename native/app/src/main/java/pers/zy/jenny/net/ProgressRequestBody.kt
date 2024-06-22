package pers.zy.jenny.net

import android.util.Log
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.ForwardingSink
import okio.IOException
import okio.buffer

/**
 * @author: zy
 * @date: 2024/4/24
 */
class ProgressRequestBody(
    private val requestBody: RequestBody,
    private val progressCall: (bytesWritten: Long, contentLength: Long) -> Unit
) : RequestBody() {

  override fun contentType(): MediaType? = requestBody.contentType()

  override fun contentLength(): Long = try {
    requestBody.contentLength()
  } catch (e: IOException) {
    -1  // Total size isn't known
  }

  override fun writeTo(sink: BufferedSink) {
    val countingSink = ProgressSink(sink).buffer()
    requestBody.writeTo(countingSink)
    countingSink.flush()
  }

  private inner class ProgressSink(delegate: BufferedSink) : ForwardingSink(delegate) {
    private var bytesWritten = 0L
    private val totalLength: Long by lazy {
      contentLength()
    }

    override fun write(source: okio.Buffer, byteCount: Long) {
      bytesWritten += byteCount
      Log.d("GFZY", "write: ${bytesWritten}, ${totalLength}")
      progressCall(bytesWritten, totalLength)
      super.write(source, byteCount)
    }
  }
}