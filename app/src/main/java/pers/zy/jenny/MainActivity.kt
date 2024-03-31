package pers.zy.jenny

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import pers.zy.jenny.command.SendFileCommand
import pers.zy.jenny.command.StringCommand
import pers.zy.jenny.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.Socket

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private lateinit var binding: ActivityMainBinding

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
            sendTest()
        }

        binding.btnTestCommand.setOnClickListener {
            Log.d("GFZY", StringCommand("asdsadsd").getCommandStr())
            Log.d("GFZY", SendFileCommand(this@MainActivity.externalCacheDir!!).getCommandStr())
        }
    }

    private fun sendTest() {
        launch(Dispatchers.IO) {
            val socket = Socket("127.0.0.1", 40006)
            val bw = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
            val br = BufferedReader(InputStreamReader(socket.getInputStream()))

            bw.write(StringCommand("我是从android传过来的字符串").getCommandStr())
            bw.flush()

            br.readText().takeIf { it.isNotBlank() }?.let {
                Log.d("GFZY", "response = $it")
            }

            bw.close()
            br.close()
            socket.close()
        }
    }
}