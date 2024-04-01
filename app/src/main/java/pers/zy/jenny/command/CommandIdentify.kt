package pers.zy.jenny.command

enum class CommandIdentify(private val content: String) {
  STRING("IDENTIFY_STRING"),
  SEND_FILE_ADB_PULL("IDENTIFY_SEND_FILE_ADB_PULL"),
  SEND_FILE_BYTE("IDENTIFY_SEND_FILE_BYTE");

  override fun toString(): String = content
}