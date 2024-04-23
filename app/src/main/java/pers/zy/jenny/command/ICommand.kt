package pers.zy.jenny.command

import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pers.zy.jenny.MyApp
import pers.zy.jenny.utils.SharedPreferenceUtil
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket

val COMMAND_END = "<<<END".toByteArray()
val SPLIT = ">>>\n<<<".toByteArray()

interface ICommand {
  fun getCommandStr(): ByteArray
}

fun CoroutineScope.startCommand(
    command: ICommand,
    updateTvStatus: suspend (String) -> Unit
) {
  launch(Dispatchers.IO) {
    val socket = try {
      Socket(SharedPreferenceUtil.hostName, SharedPreferenceUtil.port)
    } catch (e: Exception) {
      withContext(Dispatchers.Main) {
        "exception: ${e.fillInStackTrace()}".let {
          updateTvStatus(it)
          Log.d("GFZY", it)
          Toast.makeText(MyApp.INSTANCE, it, Toast.LENGTH_SHORT).show()
        }
      }
      return@launch
    }

    val bw = BufferedOutputStream(socket.getOutputStream())
    val br = BufferedInputStream(socket.getInputStream())

    val commandBytes = command.getCommandStr()

    updateTvStatus("sending bytes... command size = ${commandBytes.size}")

    val chunkSize = 2048
    var offset = 0

    while (offset < commandBytes.size) {
      val length = minOf(chunkSize, commandBytes.size - offset)
      updateTvStatus("sending bytes... progress = $offset / ${commandBytes.size} length = $length")
      bw.write(commandBytes, offset, length)
      bw.flush()
      offset += length
    }
    updateTvStatus("sending is okay, waiting for response...")

    br.bufferedReader().readText().let {
      Log.d("GFZY", "response = $it")
      updateTvStatus("response = $it")
    }

    bw.close()
    br.close()
    socket.close()
    updateTvStatus("socket close")
  }
}