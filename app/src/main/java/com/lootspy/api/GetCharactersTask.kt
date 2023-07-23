package com.lootspy.api

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.client.api.Destiny2Api
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.openapitools.client.infrastructure.ApiClient

@HiltWorker
class GetCharactersTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore,
  private val profileRepository: ProfileRepository,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val accessToken = userStore.accessToken.first()
    val activeMembership = profileRepository.getProfile(userStore.activeMembership.first())
      ?: return Result.failure()
    val notifyChannel = inputData.getString("notify_channel") ?: return Result.failure()

    Log.d(LOG_TAG, "Beginning profile sync")

    val apiClient = Destiny2Api()
    ApiClient.accessToken = accessToken
    ApiClient.apiKey["X-API-Key"] = "50ef71cc77324212886181190ea75ba7"
    val apiResponse = apiClient.destiny2GetProfile(
      activeMembership.membershipId,
      activeMembership.membershipType,
      listOf(100)
    )
    if (apiResponse.errorCode != null && apiResponse.errorCode != 1) {
//      notifySyncFailure(apiResponse, context, notifyChannel)
      Log.d(LOG_TAG, "Error fetching character data. Raw response: $apiResponse")
      return Result.failure()
    }
    val profileData = apiResponse.response?.profile?.data

    Log.d(LOG_TAG, "Retrieved profile data: $profileData")
    return Result.success()
  }

  companion object {
    private const val LOG_TAG = "LootSpy API Sync"
  }
}