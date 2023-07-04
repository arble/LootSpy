package com.lootspy

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.lootspy.R
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LootSpyApplication : Application(), Configuration.Provider {
  @Inject private lateinit var workerFactory: HiltWorkerFactory

  override fun onCreate() {
    super.onCreate()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val name = getString(R.string.notify_channel_api_failure)
      val descriptionText = getString(R.string.notify_channel_api_failure_desc)
      val importance = NotificationManager.IMPORTANCE_DEFAULT
      val channel =
        NotificationChannel(getString(R.string.notify_channel_api_id), name, importance).apply {
          description = descriptionText
        }
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(channel)
    }
  }

  override fun getWorkManagerConfiguration(): Configuration {
    TODO("Not yet implemented")
  }
}