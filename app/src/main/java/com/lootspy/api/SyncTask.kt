package com.lootspy.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.preferences.core.edit
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.client.ApiCallback
import com.lootspy.client.ApiClient
import com.lootspy.client.ApiException
import com.lootspy.client.model.DestinyResponsesDestinyLinkedProfilesResponse
import com.lootspy.util.UserStore
import com.lootspy.util.UserStore.Companion.dataStore
import java.time.LocalDate

class SyncTask(private val context: Context, params: WorkerParameters) :
  CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    val notifyChannel = inputData.getString("notify_channel") ?: return Result.failure()
    val token = inputData.getString("access_token") ?: return Result.failure()
    val membershipId = inputData.getString("membership_id") ?: return Result.failure()

    inputData.keyValueMap

    if (token.isEmpty()) {
      val builder = NotificationCompat.Builder(context, notifyChannel)
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
      null,
    )
    val apiResponse = apiClient.execute<DestinyResponsesDestinyLinkedProfilesResponse>(call)
    if (apiResponse.statusCode != 200) {
    }
    val profiles = apiResponse?.data?.profiles ?: return Result.failure()
    profiles.forEach { profile -> profile.displayName?.let { Log.i("SyncTask", it) } }
    context.dataStore.edit { it[UserStore.LAST_SYNC_TIME] = System.currentTimeMillis() }
    return Result.success()
  }

  private fun getLinkedProfilesCallback(): ApiCallback<DestinyResponsesDestinyLinkedProfilesResponse> {
    return object : ApiCallback<DestinyResponsesDestinyLinkedProfilesResponse> {
      override fun onFailure(
        exception: ApiException?,
        statusCode: Int,
        responseHeaders: MutableMap<String, MutableList<String>>?
      ) {
      }

      override fun onUploadProgress(p0: Long, p1: Long, p2: Boolean) = Unit

      override fun onDownloadProgress(p0: Long, p1: Long, p2: Boolean) = Unit

      override fun onSuccess(
        response: DestinyResponsesDestinyLinkedProfilesResponse?,
        status: Int,
        headers: MutableMap<String, MutableList<String>>?
      ) {
        val profiles = response?.profiles
        if (profiles != null) {

        }
      }
    }
  }
}