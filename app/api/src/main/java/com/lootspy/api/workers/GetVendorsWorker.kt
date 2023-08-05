package com.lootspy.api.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.api.R
import com.lootspy.client.api.Destiny2Api
import com.lootspy.client.model.Destiny2GetVendor200Response
import com.lootspy.client.model.DestinyEntitiesItemsDestinyItemPerksComponent
import com.lootspy.client.model.DestinyEntitiesItemsDestinyItemSocketsComponent
import com.lootspy.client.model.DestinyEntitiesItemsDestinyItemStatsComponent
import com.lootspy.data.UserStore
import com.lootspy.data.repo.CharacterRepository
import com.lootspy.data.repo.FilterRepository
import com.lootspy.data.repo.LootRepository
import com.lootspy.data.source.DestinyCharacter
import com.lootspy.filter.toExternal
import com.lootspy.manifest.ItemComponents
import com.lootspy.manifest.ManifestManager
import com.lootspy.types.item.BasicItem
import com.lootspy.types.item.LootEntry
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import org.openapitools.client.infrastructure.ApiClient
import org.openapitools.client.infrastructure.ServerError
import org.openapitools.client.infrastructure.ServerException

@HiltWorker
class GetVendorsWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore,
  private val manifestManager: ManifestManager,
  private val filterRepository: FilterRepository,
  private val characterRepository: CharacterRepository,
  private val lootRepository: LootRepository,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    lootRepository.clearLoot()
    val allFilters = filterRepository.getFilters().map { it.toExternal() }
    val alwaysPatterns = userStore.alwaysPatterns.first()
    if (allFilters.isEmpty() && !alwaysPatterns) {
      return Result.success()
    }
    val activeCharacterId = userStore.activeCharacter.first()
    if (activeCharacterId == 0L) {
      return Result.failure()
    }
    val character = characterRepository.getCharacter(activeCharacterId) ?: return Result.failure()
    val craftableRecordMap = manifestManager.getCraftableRecords()

    val apiClient = Destiny2Api()
    ApiClient.accessToken = inputData.getString("access_token") ?: return Result.failure()
    Log.d(LOG_TAG, ApiClient.accessToken!!)
    ApiClient.apiKey["X-API-Key"] = "50ef71cc77324212886181190ea75ba7"

    val bansheeResponse = getVendorSafe(
      apiClient,
      character,
      672118013, // Banshee-44
      listOf(302, 304, 310, 402) // ItemPerks, ItemStats, ItemReusablePlugs, VendorSales
    )
    val xurResponse = getVendorSafe(
      apiClient,
      character,
      2190858386, // Xur
      listOf(302, 304, 310, 402) // ItemPerks, ItemStats, ItemReusablePlugs, VendorSales
    )
    val bansheeItems = mutableMapOf<UInt, ItemComponents>()
    val xurItems = mutableMapOf<UInt, ItemComponents>()
    if (
      !processVendor200Response(bansheeResponse, bansheeItems) ||
      !processVendor200Response(xurResponse, xurItems)
    ) {
      return Result.failure()
    }
    val basicItemData = manifestManager.resolveVendorItems(xurItems)
    val matchedLoot = mutableMapOf<BasicItem, MutableList<String>>()
    val itemData = basicItemData.values
    val needDetails = allFilters.any { it.requiresItemDetails() }
    for (item in itemData) {
      for (filter in allFilters) {
        if (filter.match(item)) {
          var itemFilters = matchedLoot[item]
          if (itemFilters == null) {
            itemFilters = ArrayList()
            matchedLoot[item] = itemFilters
          }
          itemFilters.add(filter.name)
//          matchedLoot[filter] = item
        }
      }
    }
    if (alwaysPatterns) {
      // Fetch character weapon crafting progress
      val craftingProgressResponse = apiClient.destiny2GetProfile(
        character.membershipId,
        character.membershipType,
        listOf(900) // Records
      )
      val records = craftingProgressResponse.response?.profileRecords?.data?.records
      if (records == null) {
        Log.e(LOG_TAG, "Crafting progress requested, but triumph response was missing.")
        return Result.failure()
      }
      for (item in itemData) {
        val craftableRecordHash = craftableRecordMap[item.hash] ?: continue
        val recordComponent = records[craftableRecordHash.toString()]
        if (recordComponent == null) {
          Log.e(
            LOG_TAG,
            "Item ${item.hash} (mapped to ${craftableRecordHash}) had no crafting triumph."
          )
          return Result.failure()
        }
        var itemFilters = matchedLoot[item]
        if (itemFilters == null) {
          itemFilters = ArrayList()
          matchedLoot[item] = itemFilters
        }
        itemFilters.add(context.resources.getString(R.string.incomplete_pattern_filter))
//        lootRepository.saveLootEntry(item)
      }
//      if (records != null) {
//        Log.d(LOG_TAG, "Records response: $records")
//      }
    }
    Log.d(LOG_TAG, "$matchedLoot")
    for (entry in matchedLoot.entries) {
      lootRepository.saveLootEntry(LootEntry(entry.key, entry.value))
    }
    return Result.success()
  }

  private fun getVendorSafe(
    apiClient: Destiny2Api,
    character: DestinyCharacter,
    vendorHash: Long,
    components: List<Int>
  ): Destiny2GetVendor200Response? {
    return try {
      apiClient.destiny2GetVendor(
        character.characterId,
        character.membershipId,
        character.membershipType,
        vendorHash,
        components,
      )
    } catch (e: ServerException) {
      // If this is a ServerError AND contains a JSON response AND the response contains error code
      // 1627, this is a normal occurrence because Xur sometimes doesn't exist in-game. In all other
      // cases, rethrow whatever got us here.
      val body = when (val errorResponse = e.response) {
        is ServerError<*> -> errorResponse.body as? String
        else -> null
      } ?: throw e
      val responseObject = Json.decodeFromString<JsonObject>(body)
      when (responseObject["ErrorCode"]?.jsonPrimitive?.int) {
        1627 -> null
        null -> throw e
        else -> throw e
      }
    }
  }

  private fun processVendor200Response(
    response: Destiny2GetVendor200Response?,
    vendorItems: MutableMap<UInt, ItemComponents>,
  ): Boolean {
    if (response == null) {
      // Treat a null response as OK. Xur sometimes doesn't exist.
      return true
    }
    val salesMap = response.response?.sales?.data ?: return false
    val itemComponents = response.response?.itemComponents ?: return false
    val statsMap = itemComponents.stats?.data ?: return false
    val socketsMap = itemComponents.sockets?.data ?: return false
    val perksMap = itemComponents.perks?.data ?: return false
    for (entry in salesMap) {
      val vendorItemIndex = entry.key
      val hash = entry.value.itemHash ?: return false
      val statsComponent = statsMap[vendorItemIndex] ?: return false
      val socketsComponent = socketsMap[vendorItemIndex] ?: return false
      val perksComponent = perksMap[vendorItemIndex] ?: return false
      vendorItems[hash.toUInt()] = ItemComponents(statsComponent, socketsComponent, perksComponent)
    }
    return true
  }

  companion object {
    const val LOG_TAG = "LootSpy Vendor Sync"
  }
}