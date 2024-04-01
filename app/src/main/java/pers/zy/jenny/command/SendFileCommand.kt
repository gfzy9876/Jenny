package pers.zy.jenny.command

import java.io.File

class SendFileCommand(private val file: File) : ICommand {

  override fun getCommandStr(): ByteArray {
    return "${CommandIdentify.SEND_FILE_ADB_PULL}".toByteArray() +
            SPLIT + "adb pull ${file.absolutePath} ./translated_${file.name} && open ./translated_${file.name}".toByteArray() +
            SPLIT + COMMAND_END
  }
}