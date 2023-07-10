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
import com.lootspy.client.ApiClient
import com.lootspy.client.model.UserGetMembershipDataById200Response
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
import com.lootspy.data.source.DestinyProfile
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class GetProfilesTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore,
  private val profileRepository: ProfileRepository,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    val accessToken = userStore.accessToken.first()
    val membershipId = userStore.membershipId.first()
    if (accessToken.isEmpty() || membershipId.isEmpty()) {
      return Result.failure()
    }
    val notifyChannel = inputData.getString("notify_channel") ?: return Result.failure()

    Log.d("LootSpy API Sync", "Beginning sync. Membership ID is $membershipId")
    val apiClient = ApiClient()
    apiClient.setAccessToken(accessToken)
    val call = apiClient.buildCall(
      "https://www.bungie.net/Platform",
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
    val apiResponse = apiClient.executeTyped<UserGetMembershipDataById200Response>(call)
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
    val membershipData = apiResponse.data?.response ?: return Result.failure()

    val memberships = apiResponse.data?.response?.destinyMemberships
    if (memberships != null) {
      val profiles = memberships.mapNotNull {
        Log.d("LootSpy API Sync", it.toString())
        try {
          DestinyProfile(
            it.membershipId!!,
            it.membershipType!!,
            it.displayName!!,
            it.supplementalDisplayName ?: it.displayName!!,
            it.iconPath!!,
            it.bungieGlobalDisplayName!!,
            it.bungieGlobalDisplayNameCode!!,
          )
        } catch (e: NullPointerException) {
          Log.e("LootSpy API Sync", "Received a membership lacking essential data")
          null
        }
      }
      if (profiles.isEmpty()) {
        Log.e("LootSpy API Sync", "No received profiles were valid")
        return Result.failure()
      }
      profileRepository.saveProfiles(profiles)
      Log.d("LootSpy API Sync", "Retrieved ${profiles.size} memberships from Bungie")
      val primaryMembershipId =
        membershipData.primaryMembershipId ?: profiles.getOrNull(0)?.membershipId
      if (primaryMembershipId != null) {
        // may not always be set (e.g. for non-cross save players)
        userStore.savePrimaryMembership(primaryMembershipId)
      }
    } else {
      Log.d("LootSpy API Sync", "No memberships")
    }
    userStore.saveLastSyncTime(System.currentTimeMillis())
    return Result.success()
  }
}