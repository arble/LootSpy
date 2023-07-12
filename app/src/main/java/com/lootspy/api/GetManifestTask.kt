package com.lootspy.api

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.client.ApiClient
import com.lootspy.client.model.Destiny2GetDestinyManifest200Response
import com.lootspy.data.UserStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class GetManifestTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val accessToken = userStore.accessToken.first()
    val apiClient = ApiClient()
    apiClient.setAccessToken(accessToken)
    val apiPath = "/Destiny2/Manifest/"
    val call = apiClient.buildBungieCall(apiPath)
    val apiResponse = apiClient.executeTyped<Destiny2GetDestinyManifest200Response>(call)
    if (apiResponse.statusCode != 200) {
      return Result.failure()
    }
    val data = apiResponse.data.response?.mobileWorldContentPaths
    Log.d("LootSpy API Sync", "Retrieved profile data: $data")
    return Result.success()
  }
}