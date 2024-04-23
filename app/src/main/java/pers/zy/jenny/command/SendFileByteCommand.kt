package pers.zy.jenny.command

/**
 * socket方式传输文件
 * */
class SendFileByteCommand(private val byteArray: ByteArray) : ICommand {
  override fun getCommandStr(): ByteArray {
    return "${CommandIdentify.SEND_IMAGE_BYTE}".toByteArray() +
            SPLIT + byteArray +
            SPLIT + COMMAND_END
  }
}