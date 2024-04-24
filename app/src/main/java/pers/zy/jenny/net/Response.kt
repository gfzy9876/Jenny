package pers.zy.jenny.net

import com.google.gson.Gson

/**
 * @author: zy
 * @date: 2024/4/24
 */
class Response {
  var msg: String? = ""
  override fun toString(): String {
    return Gson().toJson(this)
  }
}