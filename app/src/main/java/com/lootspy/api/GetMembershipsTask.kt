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
    val membershipId = userStore.bungieMembershipId.first()
    val notifyChannel = inputData.getString("notify_channel") ?: return Result.failure()

    Log.d(LOG_TAG, "Beginning membership sync")
    val apiClient = UserApi()
    ApiClient.accessToken = inputData.getString("access_token") ?: return Result.failure()
    ApiClient.apiKey["X-API-Key"] = "50ef71cc77324212886181190ea75ba7"
    val apiResponse = apiClient.userGetMembershipDataById(membershipId.toLong(), 254)
    if (apiResponse.errorCode != null && apiResponse.errorCode != 1) {
//      notifySyncFailure(apiResponse, context, notifyChannel)
//      userStore.deleteAuthInfo()
      return Result.failure()
    }
    val membershipData = apiResponse.response ?: return Result.failure()
    val memberships = membershipData.destinyMemberships
    if (memberships != null) {
      val profiles = memberships.mapNotNull {
        Log.d(LOG_TAG, it.toString())
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
          Log.e(LOG_TAG, "Received a membership lacking essential data")
          null
        }
      }
      if (profiles.isEmpty()) {
        Log.e(LOG_TAG, "No received profiles were valid")
        return Result.failure()
      }
      profileRepository.saveProfiles(profiles)
      Log.d(LOG_TAG, "Retrieved ${profiles.size} memberships from Bungie")
      val primaryMembershipId =
        membershipData.primaryMembershipId ?: profiles.getOrNull(0)?.membershipId
      if (primaryMembershipId != null) {
        // may not always be set (e.g. for non-cross save players)
        userStore.savePrimaryMembership(primaryMembershipId)
      }
    } else {
      Log.d(LOG_TAG, "No memberships")
    }
    userStore.saveLastSyncTime(System.currentTimeMillis())
    return Result.success()
  }

  companion object {
    private const val LOG_TAG = "LootSpy API Sync"
  }
}