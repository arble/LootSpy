package com.lootspy.api.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.manifest.ManifestManager
import com.lootspy.client.api.Destiny2Api
import com.lootspy.data.repo.CharacterRepository
import com.lootspy.data.repo.ProfileRepository
import com.lootspy.data.UserStore
import com.lootspy.data.source.DestinyCharacter
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.openapitools.client.infrastructure.ApiClient

@HiltWorker
class GetCharactersWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore,
  private val manifestManager: ManifestManager,
  private val profileRepository: ProfileRepository,
  private val characterRepository: CharacterRepository,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val activeMembership = profileRepository.getProfile(userStore.activeMembership.first())
      ?: return Result.failure()
    val notifyChannel = inputData.getString("notify_channel") ?: return Result.failure()

    Log.d(LOG_TAG, "Beginning character sync")

    val apiClient = Destiny2Api()
    ApiClient.accessToken = inputData.getString("access_token") ?: return Result.failure()
    ApiClient.apiKey["X-API-Key"] = "50ef71cc77324212886181190ea75ba7"
    val apiResponse = apiClient.destiny2GetProfile(
      activeMembership.membershipId,
      activeMembership.membershipType,
      listOf(200) // Characters
    )
    if (apiResponse.errorCode != null && apiResponse.errorCode != 1) {
//      notifySyncFailure(apiResponse, context, notifyChannel)
      Log.d(LOG_TAG, "Error fetching character data. Raw response: $apiResponse")
      return Result.failure()
    }
    val characterComponents = apiResponse.response?.characters?.data ?: return Result.failure()

    val characterBuilders = mutableMapOf<Long, DestinyCharacter.Builder>()
    val (raceMap, classMap) = manifestManager.getCharacterDefinitions() // only three rows each
    val emblemHashes = mutableMapOf<UInt, Long>()
    characterComponents.forEach {
      try {
        // emblem path is the only field we need to look up specifically
        val id = it.key.toLong()
        val character = it.value
        characterBuilders[id] = DestinyCharacter.Builder(
          id,
          character.membershipId!!,
          character.membershipType!!,
          character.light!!,
          raceMap[character.raceHash!!.toUInt()]!!,
          classMap[character.classHash!!.toUInt()]!!,
          character.emblemColor!!
        )
        emblemHashes[character.emblemHash!!.toUInt()] = id
      } catch (e: NullPointerException) {
        Log.e(LOG_TAG, "A character response did not have all required properties")
        return Result.failure()
      }
    }
    val emblemPaths = manifestManager.getEmblemPaths(emblemHashes)
    try {
      val characters = characterBuilders.map { it.value.emblemPath(emblemPaths[it.key]!!).build() }
      Log.d(LOG_TAG, "Retrieved characters: $characters")
      characterRepository.saveCharacters(characters)
    } catch (e: NullPointerException) {
      Log.e(LOG_TAG, "A character response did not have an emblem path")
      return Result.failure()
    }

    return Result.success()
  }

  companion object {
    private const val LOG_TAG = "LootSpy API Sync"
  }
}