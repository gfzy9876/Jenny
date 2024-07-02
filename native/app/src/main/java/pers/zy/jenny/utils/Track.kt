package pers.zy.jenny.utils

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import pers.zy.jenny.MyApp

/**
 * @author: zy
 * @date: 2024/7/2
 */
object Track {

  fun firebaseLog(vararg params: Pair<String, String>) {
    MyApp.firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_ITEM) {
      params.forEach {
        param(it.first, it.second)
      }
    }
  }

}