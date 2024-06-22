package pers.zy.jenny

import android.app.Application

/**
 * @author: zy
 * @date: 2024/4/2
 */
class MyApp : Application() {

  companion object {
    lateinit var INSTANCE: MyApp
  }

  override fun onCreate() {
    super.onCreate()
    INSTANCE = this
  }

}