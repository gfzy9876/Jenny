package pers.zy.jenny.command

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pers.zy.jenny.net.Response
import java.io.File

/**
 * 通过adb命令，传输文件
 * */
class SendFileADBCommand(val file: File) : ICommand {
  override val commandIdentify: CommandIdentify
    get() = CommandIdentify.SEND_FILE_ADB_PULL

  override fun createRequestBody(): RequestBody {
    return "adb pull ${file.absolutePath} ./translated_${file.name} && open ./translated_${file.name}"
        .toRequestBody("application/octet-stream".toMediaType())
  }

  override suspend fun createSuspendFun(): Response {
    return API.commandSendFileAdbPull("adb pull ${file.absolutePath} ./translated_${file.name} && open ./translated_${file.name}")
  }
}