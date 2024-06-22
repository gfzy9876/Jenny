package pers.zy.jenny.utils

import android.content.Context
import pers.zy.jenny.JennyActivity
import pers.zy.jenny.MyApp

object SharedPreferenceUtil {
  val SP_HOST_NAME: String
    get() = getString(MyApp.INSTANCE, JennyActivity.EXTRA_HOST_NAME, "")
  val SP_PORT: Int
    get() = getInt(MyApp.INSTANCE, JennyActivity.EXTRA_PORT, -1)

  var hostName: String = ""
  var port: Int = 0

  fun getString(context: Context, key: String, defValue: String = ""): String {
    val sp = context.getSharedPreferences("jenny", Context.MODE_PRIVATE)
    return sp.getString(key, defValue) ?: defValue
  }

  fun getInt(context: Context, key: String, defValue: Int = 0): Int {
    val sp = context.getSharedPreferences("jenny", Context.MODE_PRIVATE)
    return sp.getInt(key, defValue)
  }

  fun saveString(context: Context, key: String, value: String) {
    val sp = context.getSharedPreferences("jenny", Context.MODE_PRIVATE)
    sp.edit().putString(key, value).apply()
  }

  fun saveInt(context: Context, key: String, value: Int) {
    val sp = context.getSharedPreferences("jenny", Context.MODE_PRIVATE)
    sp.edit().putInt(key, value).apply()
  }
}
