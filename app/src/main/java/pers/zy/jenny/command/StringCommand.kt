package pers.zy.jenny.command

import pers.zy.jenny.net.Response

/**
 * socket方式传输字符串
 * */
class StringCommand(val string: String) : ICommand {
  override val commandIdentify: CommandIdentify
    get() = CommandIdentify.STRING

  override suspend fun createSuspendFun(progress: (Float) -> Unit): Response {
    return API.commandString(string)
  }
}