package pers.zy.jenny.command

import pers.zy.jenny.net.Response
import java.io.File

/**
 * 通过adb命令，传输文件
 * */
class SendFileADBCommand(private val file: File) : ICommand {
  override val commandIdentify: CommandIdentify
    get() = CommandIdentify.SEND_FILE_ADB_PULL

  override suspend fun createSuspendFun(progress: (Float) -> Unit): Response {
    return API.commandSendFileAdbPull("adb pull ${file.absolutePath} ./translated_${file.name} && open -R ./translated_${file.name}")
  }
}