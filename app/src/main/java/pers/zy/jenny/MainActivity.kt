package pers.zy.jenny

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.*
import pers.zy.jenny.command.ICommand
import pers.zy.jenny.command.SendFileByteCommand
import pers.zy.jenny.command.StringCommand
import pers.zy.jenny.databinding.ActivityMainBinding
import java.net.Socket


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

  private lateinit var binding: ActivityMainBinding

  private val fileLaunch = registerForActivityResult(ActivityResultContracts.GetContent()) {
    it?.let { uri ->
      val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return@let
      sendTest(SendFileByteCommand(bytes))
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)
    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    binding.btnTest.setOnClickListener {
      binding.etString.text?.toString()?.let { str ->
        sendTest(StringCommand(str))
      }
    }

    binding.btnImage.setOnClickListener {
      fileLaunch.launch("image/*")
    }
  }

  private fun sendTest(command: ICommand) {
    launch(Dispatchers.IO) {
      val socket = try {
//        Socket("127.0.0.1", 40006)
        Socket("10.0.10.65", 40007)
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          "exception: ${e.fillInStackTrace()}".let {
            updateTvStatus(it)
            Log.d("GFZY", it)
            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
          }
        }
        return@launch
      }

      val bw = socket.getOutputStream()
      val br = socket.getInputStream()

      val commandStr = command.getCommandStr()
      updateTvStatus("send bytes:")
      bw.write(commandStr)
      bw.flush()

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

  private suspend fun updateTvStatus(msg: String) {
    withContext(Dispatchers.Main) {
      Log.d("GFZY", "updateTvStatus: ${msg}")
      binding.tvStatus.text = msg
    }
  }
}