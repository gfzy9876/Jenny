package pers.zy.jenny

import android.app.Application
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * @author: zy
 * @date: 2024/4/2
 */
class MyApp : Application() {

  companion object {
    lateinit var INSTANCE: MyApp
    lateinit var firebaseAnalytics: FirebaseAnalytics
  }

  override fun onCreate() {
    super.onCreate()
    INSTANCE = this
    firebaseAnalytics = Firebase.analytics
  }

}