package pers.zy.jenny.command

import java.io.File

interface ICommand {
    val commandIdentify: CommandIdentify

    fun getCommandStr(): String
}

//const val COMMAND_START = ">>>START"
const val COMMAND_END = "<<<END"

enum class CommandIdentify(val content: String) {
    STRING("IDENTIFY_STRING"),
    SEND_FILE("IDENTIFY_SEND_FILE");

    override fun toString(): String = content
}

class StringCommand(private val string: String) : ICommand {
    override val commandIdentify: CommandIdentify
        get() = CommandIdentify.STRING

    /**
     * IDENTIFY_STRING
     * string
     * <<<END
     * */
    override fun getCommandStr(): String {
        return "$commandIdentify\n" +
                "$string\n" +
                COMMAND_END
    }
}

class SendFileCommand(private val file: File) : ICommand {
    override val commandIdentify: CommandIdentify
        get() = CommandIdentify.SEND_FILE

    /**
     * IDENTIFY_SEND_FILE
     * "adb pull ${file.absolutePath} ./translated_${file.name} && open ./translated_${file.name}"
     * <<<END
     * */
    override fun getCommandStr(): String {
        return "${commandIdentify}\n" +
                "adb pull ${file.absolutePath} ./translated_${file.name} && open ./translated_${file.name}\n" +
                COMMAND_END
    }
}
