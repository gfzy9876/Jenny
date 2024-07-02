package pers.zy.jenny

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pers.zy.jenny.command.*
import pers.zy.jenny.databinding.ActivityJennyBinding
import pers.zy.jenny.utils.DeviceUtil
import pers.zy.jenny.utils.FileUtil
import pers.zy.jenny.utils.SharedPreferenceUtil
import pers.zy.jenny.utils.Track


class JennyActivity : AppCompatActivity() {

  private lateinit var binding: ActivityJennyBinding
  private val logs = mutableListOf<String>()
  private val logAdapter = LogAdapter(logs)

  private val imageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
    it?.let { uri -> launchImageUri(uri) }
  }

  private val videoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
    it?.let { uri -> launchVideoUri(uri) }
  }

  private val fileLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) {
    it?.let { uri -> launchFileUri(uri) }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityJennyBinding.inflate(layoutInflater)
    setContentView(binding.root)
    initHostAndPort()
    initView()
  }

  @SuppressLint("SetTextI18n")
  private fun initView() {
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
      Track.firebaseLog("selectType" to "string")
      DeviceUtil.hideKeyboard(this@JennyActivity)
      binding.etString.clearFocus()
      binding.etString.text?.toString()?.takeIf { it.isNotBlank() }?.let { str ->
        launchString(str)
      }
      binding.etString.setText("")
    }

    binding.btnImage.setOnClickListener {
      Track.firebaseLog("selectType" to "image")
      imageLauncher.launch("image/*")
    }
    binding.btnVideo.setOnClickListener {
      Track.firebaseLog("selectType" to "video")
      videoLauncher.launch("video/*")
    }
    binding.btnFileADB.setOnClickListener {
      Track.firebaseLog("selectType" to "fileAdb")
      fileLauncher.launch("*/*")
    }
    binding.btnSendFileToPhone.setOnClickListener {
      Track.firebaseLog("selectType" to "sendFileToPhone")
      lifecycleScope.launch {
        try {
          val filePath = API.sendFileToPhone().filePath
          Toast.makeText(this@JennyActivity, "文件获取完成，来源：$filePath", Toast.LENGTH_LONG).show()
          startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))
        } catch (e: Exception) {
          Toast.makeText(this@JennyActivity, "文件获取失败", Toast.LENGTH_LONG).show()
        }
      }
    }
    binding.rvLogs.let {
      it.layoutManager = LinearLayoutManager(this)
      it.adapter = logAdapter
      it.itemAnimator = null
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    Log.d("GFZY", "onNewIntent: ${intent.extras}")
    setIntent(intent)
    initHostAndPort()
    intent.extras?.clear()
  }

  private fun launchString(content: String) {
    lifecycleScope.launch {
      startCommand(StringCommand(content))
    }
  }

  private fun launchImageUri(uri: Uri) {
    lifecycleScope.launch {
      FileUtil.saveUriToFile(
          uri,
          FileUtil.createFile("create_${System.currentTimeMillis()}${FileUtil.getMimeTypeFromUri(uri)}")
      )?.let {
        startCommand(SendFileCommand(it, CommandIdentify.SEND_IMAGE_BYTE))
      }
    }
  }

  private fun launchVideoUri(uri: Uri) {
    lifecycleScope.launch {
      FileUtil.saveUriToFile(
          uri,
          FileUtil.createFile("create_${System.currentTimeMillis()}${FileUtil.getMimeTypeFromUri(uri)}")
      )?.let {
        startCommand(SendFileCommand(it, CommandIdentify.SEND_VIDEO_BYTE))
      }
    }
  }

  private fun launchFileUri(uri: Uri) {
    lifecycleScope.launch {
      FileUtil.saveUriToFile(
          uri,
          FileUtil.createFile("create_${System.currentTimeMillis()}${FileUtil.getMimeTypeFromUri(uri)}")
      )?.let {
        Log.d("GFZY", "okay ${it.absolutePath}")
        startCommand(SendFileADBCommand(it))
      }
    }
  }

  private fun initHostAndPort() {
    (intent.getStringExtra(EXTRA_HOST_NAME) ?: SharedPreferenceUtil.SP_HOST_NAME).let {
      Log.d("GFZY", "initHostAndPort: ${it}")
      SharedPreferenceUtil.hostName = it
      SharedPreferenceUtil.saveString(MyApp.INSTANCE, EXTRA_HOST_NAME, it)
      binding.tvCurrentHost.text = "host = $it"
    }
    intent.getIntExtra(EXTRA_PORT, SharedPreferenceUtil.SP_PORT).takeIf { it > 0 }?.let {
      SharedPreferenceUtil.port = it
      SharedPreferenceUtil.saveInt(MyApp.INSTANCE, EXTRA_PORT, it)
      binding.tvCurrentPort.text = "port = $it"
    }
  }

  private suspend fun startCommand(command: ICommand) {
    clearLog()
    updateTvStatus("上传中")
    try {
      val result = withContext(Dispatchers.IO) {
        command.createSuspendFun {
          updateTvStatus("progress: ${it}%")
        }
      }
      updateTvStatus("上传完成 $result")
    } catch (e: Exception) {
      updateTvStatus("error: $e")
    }
  }

  private fun updateTvStatus(msg: String) {
    Log.d("GFZY", "updateTvStatus: ${msg}")
    logs.add(msg)
    logs.indexOf(msg).also {
      logAdapter.notifyItemInserted(it)
      binding.rvLogs.scrollToPosition(it)
    }
  }

  private fun clearLog() {
    logs.clear()
    logAdapter.notifyDataSetChanged()
  }

  companion object {
    const val EXTRA_HOST_NAME = "local_trans_host_name"
    const val EXTRA_PORT = "local_trans_port"
  }
}