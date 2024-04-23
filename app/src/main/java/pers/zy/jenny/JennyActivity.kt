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
import pers.zy.jenny.command.*
import pers.zy.jenny.databinding.ActivityJennyBinding
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
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
        startCommand(StringCommand(str))
      }
      binding.etString.setText("")
    }

    binding.btnImage.setOnClickListener {
      startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "image/*"
      }, REQUEST_CODE_OPEN_IMAGE)
    }
    binding.btnVideo.setOnClickListener {
      startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "video/*"
      }, REQUEST_CODE_OPEN_VIDEO)
    }
    binding.btnFileADB.setOnClickListener {
      startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
        type = "*/*"
      }, REQUEST_CODE_OPEN_FILE)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (requestCode == REQUEST_CODE_OPEN_IMAGE && resultCode == RESULT_OK) {
      data?.data?.let { uri ->
        val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return@let
        startCommand(SendFileByteCommand(bytes))
      }
    } else if (requestCode == REQUEST_CODE_OPEN_VIDEO && resultCode == RESULT_OK) {
      data?.data?.let { uri ->
        val bytes = contentResolver.openInputStream(uri)?.readBytes() ?: return@let
        startCommand(SendVideoByteCommand(bytes))
      }
    } else if (requestCode == REQUEST_CODE_OPEN_FILE && resultCode == RESULT_OK) {
      data?.data?.let { uri ->
        launch(Dispatchers.IO) {
          FileUtils.saveUriToFile(
              uri,
              FileUtils.createFile("create_${System.currentTimeMillis()}${FileUtils.getMimeTypeFromUri(uri)}"),
              loading = {
                Log.d("GFZY", "loading ${it}")
              },
              {
                Log.d("GFZY", "okay ${it.absolutePath}")
                startCommand(SendFileADBCommand(it))
              }
          )
        }
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

  private fun startCommand(command: ICommand) {
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

      val bw = BufferedOutputStream(socket.getOutputStream())
      val br = BufferedInputStream(socket.getInputStream())

      val commandBytes = command.getCommandStr()

      updateTvStatus("sending bytes... command size = ${commandBytes.size}")

      val chunkSize = 1024
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

  private suspend fun updateTvStatus(msg: String) {
    withContext(Dispatchers.Main) {
      Log.d("GFZY", "updateTvStatus: ${msg}")
      binding.tvStatus.text = msg
    }
  }

  companion object {
    private const val REQUEST_CODE_OPEN_IMAGE = 100000
    private const val REQUEST_CODE_OPEN_VIDEO = 100001
    private const val REQUEST_CODE_OPEN_FILE = 100002
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