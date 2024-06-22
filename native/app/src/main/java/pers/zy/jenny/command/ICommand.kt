package pers.zy.jenny.command

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pers.zy.jenny.net.IApi
import pers.zy.jenny.net.Response
import pers.zy.jenny.utils.SharedPreferenceUtil
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

interface ICommand {
  val commandIdentify: CommandIdentify

  suspend fun createSuspendFun(progress: (Float) -> Unit): Response
}

val client = OkHttpClient.Builder()
    .addInterceptor(HttpLoggingInterceptor().apply {
      level = HttpLoggingInterceptor.Level.HEADERS
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
