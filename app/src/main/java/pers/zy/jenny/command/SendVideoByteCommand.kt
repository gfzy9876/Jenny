package pers.zy.jenny.command

/**
 * @author: zy
 * @date: 2024/4/23
 * socket方式传输视频
 */

class SendVideoByteCommand(private val byteArray: ByteArray) : ICommand {
  override fun getCommandStr(): ByteArray {
    return "${CommandIdentify.SEND_VIDEO_BYTE}".toByteArray() +
        SPLIT + byteArray +
        SPLIT + COMMAND_END
  }
}