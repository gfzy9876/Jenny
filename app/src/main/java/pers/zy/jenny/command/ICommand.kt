package pers.zy.jenny.command

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import pers.zy.jenny.net.IApi
import pers.zy.jenny.net.Response
import pers.zy.jenny.utils.SharedPreferenceUtil
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

interface ICommand {
  val commandIdentify: CommandIdentify
  fun createRequestBody(): RequestBody

  suspend fun createSuspendFun(): Response
}

val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.BODY
    })
    .writeTimeout(3, TimeUnit.MINUTES)
    .readTimeout(3, TimeUnit.MINUTES)
    .build()

val API = Retrofit.Builder()
    .baseUrl("http://${SharedPreferenceUtil.hostName}:${SharedPreferenceUtil.port}/")
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .build()
    .create(IApi::class.java)

fun CoroutineScope.startCommand(
    command: ICommand,
    updateTvStatus: (String) -> Unit
) {
  launch(Dispatchers.IO) {
    try {
      command.createSuspendFun()
    } catch (e: Exception) {
      updateTvStatus("error: $e")
    }
  }
}