package com.lootspy.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.lootspy.client.ApiCallback
import com.lootspy.client.ApiClient
import com.lootspy.util.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class SyncTask(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
  private val userStore = UserStore(context)

  override suspend fun doWork(): Result {
    val token = userStore.tokenFlow.first()
    val membershipId = userStore.membershipFlow.first()

    if (token.isEmpty()) {
      val builder = NotificationCompat.Builder(context, "foo")
        .setContentTitle("LootSpy sync failed")
        .setContentText("Couldn't get loot. You may need to log in again.")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setTimeoutAfter(5000)
      with(NotificationManagerCompat.from(context)) {
        if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
          ) == PackageManager.PERMISSION_GRANTED
        ) {
          notify(0, builder.build())
        }
      }
      return Result.failure()
    }
    val apiClient = ApiClient()
    apiClient.setAccessToken(token)
    apiClient.setApiKey("50ef71cc77324212886181190ea75ba7")
    val call = apiClient.buildCall(
      "https://www.bungie.net/Platform",
      "Destiny2/254/Profile/${membershipId}/LinkedProfiles",
      "GET",
      null,
      null,
      null,
      null,
      null,
      null,
      null,
      ApiCallback<>
    )
    return Result.success()
  }
}