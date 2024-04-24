package pers.zy.jenny.command

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import pers.zy.jenny.net.Response
import java.io.File

/**
 * socket方式传输文件
 * */
class SendFileCommand(val file: File, override val commandIdentify: CommandIdentify) : ICommand {
  override fun createRequestBody(): RequestBody {
    return file.asRequestBody()
  }

  override suspend fun createSuspendFun(): Response {
    return if (commandIdentify == CommandIdentify.SEND_IMAGE_BYTE) {
      API.commandSendImageByte(
          MultipartBody.Part.createFormData(
              "photo",
              file.name,
              file.asRequestBody("image/*".toMediaType()
              )
          )
      )
    } else {
      API.commandSendVideoByte(
          MultipartBody.Part.createFormData(
              "video",
              file.name,
              file.asRequestBody("video/*".toMediaType())
          )
      )
    }
  }
}