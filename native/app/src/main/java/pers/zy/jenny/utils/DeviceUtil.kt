package pers.zy.jenny.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import pers.zy.jenny.MyApp

object DeviceUtil {
  fun hideKeyboard(context: Activity) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(context.currentFocus?.windowToken, 0)
  }

  /**
   * 复制字符串到剪贴板
   */
  fun copyToClipboard(content: String) {
    val cm = MyApp.INSTANCE.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    cm.text = content
  }
}