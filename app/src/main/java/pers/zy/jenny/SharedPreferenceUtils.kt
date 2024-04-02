package pers.zy.jenny

import android.content.Context

object SharedPreferenceUtils {
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
