package pers.zy.jenny.command

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pers.zy.jenny.net.Response

/**
 * socket方式传输字符串
 * */
class StringCommand(val string: String) : ICommand {
  override val commandIdentify: CommandIdentify
    get() = CommandIdentify.STRING

  override fun createRequestBody(): RequestBody {
    return string.toRequestBody("application/octet-stream".toMediaType())
  }

  override suspend fun createSuspendFun(): Response {
    return API.commandString(string)
  }
}