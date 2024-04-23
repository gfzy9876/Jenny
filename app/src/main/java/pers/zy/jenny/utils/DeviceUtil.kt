package pers.zy.jenny.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

object DeviceUtil {
  fun hideKeyboard(context: Activity) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(context.currentFocus?.windowToken, 0)
  }
}