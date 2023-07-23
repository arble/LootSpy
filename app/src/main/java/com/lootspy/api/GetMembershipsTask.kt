package com.lootspy.api

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.client.api.UserApi
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
import com.lootspy.data.source.DestinyProfile
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.openapitools.client.infrastructure.ApiClient

@HiltWorker
class GetMembershipsTask @AssistedInject constructor(
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

    Log.d("LootSpy API Sync", "Beginning membership sync")
    val apiClient = UserApi()
//    ApiClient.accessToken = accessToken
    ApiClient.apiKey["X-API-Key"] = "50ef71cc77324212886181190ea75ba7"

//    val apiResponse = apiClient.executeTyped<UserGetMembershipDataById200Response>(call)
    val apiResponse = apiClient.userGetMembershipDataById(membershipId.toLong(), 254)
    Log.d("LootSpy API Sync", "Executed API call")
    if (apiResponse.errorCode != null && apiResponse.errorCode != 1) {
//      notifySyncFailure(apiResponse, context, notifyChannel)
//      userStore.deleteAuthInfo()
      return Result.failure()
    }
    val membershipData = apiResponse.response ?: return Result.failure()
    val memberships = membershipData.destinyMemberships
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