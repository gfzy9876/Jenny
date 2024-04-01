package pers.zy.jenny.command

val COMMAND_END = "<<<END".toByteArray()
val SPLIT = ">>>\n<<<".toByteArray()

interface ICommand {
  fun getCommandStr(): ByteArray
}
