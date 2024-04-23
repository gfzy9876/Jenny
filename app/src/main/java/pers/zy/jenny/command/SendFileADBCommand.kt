package pers.zy.jenny.command

import java.io.File

/**
 * 通过adb命令，传输文件
 * */
class SendFileADBCommand(private val file: File) : ICommand {

  override fun getCommandStr(): ByteArray {
    return "${CommandIdentify.SEND_FILE_ADB_PULL}".toByteArray() +
        SPLIT + "adb pull ${file.absolutePath} ./translated_${file.name} && open ./translated_${file.name}".toByteArray() +
        SPLIT + COMMAND_END
  }
}