package com.lootspy.api

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.client.ApiClient
import com.lootspy.client.model.Destiny2GetProfile200Response
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

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

    Log.d("LootSpy API Sync", "Beginning profile sync")
    val apiClient = ApiClient()
    apiClient.setAccessToken(accessToken)
    val apiPath =
      "/Destiny2/${activeMembership.membershipType}/Profile/${activeMembership.membershipId}/"
    val call = apiClient.buildBungieCall(apiPath, queryParams = listOf(Pair("components", "100")))
    val apiResponse = apiClient.executeTyped<Destiny2GetProfile200Response>(call)
    if (apiResponse.statusCode != 200) {
      notifySyncFailure(apiResponse, context, notifyChannel)
      return Result.failure()
    }

    val profileData = apiResponse.data.response?.profile?.data ?: return Result.failure()
    Log.d("LootSpy API Sync", "Retrieved profile data: $profileData")
    return Result.success()
  }
}