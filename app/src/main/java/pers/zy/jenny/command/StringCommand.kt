package pers.zy.jenny.command

/**
 * socket方式传输字符串
 * */
class StringCommand(private val string: String) : ICommand {
  override fun getCommandStr(): ByteArray {
    return "${CommandIdentify.STRING}".toByteArray() +
            SPLIT + string.toByteArray() +
            SPLIT + COMMAND_END
  }
}