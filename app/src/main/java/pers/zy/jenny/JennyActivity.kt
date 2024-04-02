package pers.zy.jenny

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import kotlinx.coroutines.*
import pers.zy.jenny.command.ICommand
import pers.zy.jenny.command.SendFileByteCommand
import pers.zy.jenny.command.StringCommand
import pers.zy.jenny.databinding.ActivityJennyBinding
import java.net.Socket


class JennyActivity : AppCompatActivity(), CoroutineScope by MainScope() {

  private lateinit var binding: ActivityJennyBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityJennyBinding.inflate(layoutInflater)
    setContentView(binding.root)

    initHostAndPort()

    ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
      val systemBars = insets.systemWindowInsets
      v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
      insets
    }

    binding.etString.addTextChangedListener(object : TextWatcher {
      override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

      override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

      override fun afterTextChanged(s: Editable?) {
        binding.btnString.isEnabled = s?.isNotEmpty() == true
      }
    })

    binding.btnString.setOnClickListener {
      hideKeyboard(this@JennyActivity)
      binding.etString.text?.toString()?.takeIf { it.isNotBlank() }?.let { str ->
        sendTest(StringCommand(str))
      }
      binding.etString.setText("")
    }

    binding.btnImage.setOnClickListener {
      startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "image/*"
      }, 123)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == 123 && resultCode == RESULT_OK) {
      data?.data?.let { uri ->
        val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return@let
        sendTest(SendFileByteCommand(bytes))
      }
    }
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    Log.d("GFZY", "onNewIntent: ${intent?.extras}")
    setIntent(intent)
    initHostAndPort()
    intent?.extras?.clear()
  }

  private fun initHostAndPort() {
    var host = intent.getStringExtra(EXTRA_HOST_NAME)
    if (host.isNullOrEmpty()) {
      host = SP_HOST_NAME
    }
    host.let {
      hostName = it
      SharedPreferenceUtils.saveString(MyApp.INSTANCE, EXTRA_HOST_NAME, it)
      binding.tvCurrentHost.text = "host = $it"
    }

    intent.getIntExtra(EXTRA_PORT, SP_PORT).takeIf { it > 0 }?.let {
      port = it
      SharedPreferenceUtils.saveInt(MyApp.INSTANCE, EXTRA_PORT, it)
      binding.tvCurrentPort.text = "port = $it"
    }
  }

  private fun sendTest(command: ICommand) {
    launch(Dispatchers.IO) {
      val socket = try {
        Socket(hostName, port)
      } catch (e: Exception) {
        withContext(Dispatchers.Main) {
          "exception: ${e.fillInStackTrace()}".let {
            updateTvStatus(it)
            Log.d("GFZY", it)
            Toast.makeText(this@JennyActivity, it, Toast.LENGTH_SHORT).show()
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

  companion object {
    const val EXTRA_HOST_NAME = "local_trans_host_name"
    const val EXTRA_PORT = "local_trans_port"
    val SP_HOST_NAME: String
      get() = SharedPreferenceUtils.getString(MyApp.INSTANCE, EXTRA_HOST_NAME, "")
    val SP_PORT: Int
      get() = SharedPreferenceUtils.getInt(MyApp.INSTANCE, EXTRA_PORT, -1)
    var hostName: String = ""
    var port: Int = 0

    fun hideKeyboard(activity: Activity) {
      val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
      imm.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }
  }
}