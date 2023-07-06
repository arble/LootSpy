package com.lootspy.api

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.client.ApiCallback
import com.lootspy.client.ApiClient
import com.lootspy.client.ApiException
import com.lootspy.client.model.ConfigUserTheme
import com.lootspy.client.model.DestinyResponsesDestinyLinkedProfilesResponse
import com.lootspy.client.model.UserGetAvailableThemes200Response
import com.lootspy.client.model.UserGetMembershipDataById200Response
import com.lootspy.client.model.UserUserMembershipData
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class SyncTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore,
  private val profileRepository: ProfileRepository,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    val accessToken = userStore.accessToken.first()
    val membershipId = userStore.membershipId.first()
    Log.d("LootSpy API Sync", "Beginning sync. Membership ID is $membershipId")
    val notifyChannel = inputData.getString("notify_channel") ?: return Result.failure()

    if (accessToken.isEmpty()) {

    }
    val apiClient = ApiClient()
    apiClient.setAccessToken(accessToken)
//    apiClient.setApiKey("50ef71cc77324212886181190ea75ba7")
    val call = apiClient.buildCall(
      "https://www.bungie.net/Platform",
//      "/Destiny2/254/Profile/${membershipId}/LinkedProfiles",
      "/User/GetMembershipsById/$membershipId/254",
      "GET",
      emptyList(),
      emptyList(),
      null,
      mapOf("X-API-Key" to "50ef71cc77324212886181190ea75ba7"),
      emptyMap(),
      emptyMap(),
      emptyArray(),
      null,
    )
//    val apiResponse = apiClient.execute<DestinyResponsesDestinyLinkedProfilesResponse>(call)
    val apiResponse = apiClient.execute<UserGetMembershipDataById200Response>(
      call,
      UserGetMembershipDataById200Response::class.java
    )
    Log.d("LootSpy API Sync", "Executed API call")
    if (apiResponse.statusCode != 200) {
      Log.d(
        "LootSpy API Sync",
        "Sync failed due to error code from server: ${apiResponse.statusCode}"
      )
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
//      userStore.deleteAuthInfo()
      return Result.failure()
    }
    val memberships = apiResponse.data?.response?.destinyMemberships
    Log.d("LootSpy API Sync", "Response was ${apiResponse.data}")
    if (memberships != null) {
      memberships.forEach { Log.d("LootSpy API Sync", it.toString()) }
    } else {
      Log.d("LootSpy API Sync", "No memberships")
    }
//    val profiles = apiResponse?.data?.profiles ?: return Result.failure()
//    Log.d("LootSpy API Sync", "Received the following linked profiles:")
//    profiles.forEach { profile -> profile.displayName?.let { Log.i("LootSpy API Sync", it) } }
//    profileRepository.saveProfiles(profiles)
    userStore.saveLastSyncTime(System.currentTimeMillis())
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