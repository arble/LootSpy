package com.lootspy.api.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.api.R
import com.lootspy.manifest.ManifestManager
import com.lootspy.client.api.Destiny2Api
import com.lootspy.client.model.Destiny2GetVendor200Response
import com.lootspy.data.repo.CharacterRepository
import com.lootspy.data.repo.FilterRepository
import com.lootspy.data.UserStore
import com.lootspy.data.repo.LootRepository
import com.lootspy.filter.toExternal
import com.lootspy.types.item.BasicItem
import com.lootspy.types.item.LootEntry
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import org.openapitools.client.infrastructure.ApiClient

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
    val bansheeResponse = apiClient.destiny2GetVendor(
      character.characterId,
      character.membershipId,
      character.membershipType,
      672118013, // Banshee-44
      listOf(402) // VendorSales
    )
    val xurResponse = apiClient.destiny2GetVendor(
      character.characterId,
      character.membershipId,
      character.membershipType,
      2190858386, // Xur
      listOf(402) // VendorSales
    )
    val itemsForSale = mutableListOf<UInt>()
    if (
      !processVendor200Response(bansheeResponse, itemsForSale) ||
      !processVendor200Response(xurResponse, itemsForSale)
    ) {
      return Result.failure()
    }
    val itemData = manifestManager.resolveItems(itemsForSale)
    val matchedLoot = mutableMapOf<BasicItem, MutableList<String>>()
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
    for (entry in matchedLoot.entries) {
      lootRepository.saveLootEntry(LootEntry(entry.key, entry.value))
    }
    return Result.success()
  }

  private fun processVendor200Response(
    response: Destiny2GetVendor200Response,
    items: MutableList<UInt>
  ): Boolean {
    val sales = response.response?.sales?.data?.values ?: return false
    sales.forEach {
      val hash = it.itemHash
      if (hash != null) {
        items.add(hash.toUInt())
      }
    }
    return true
  }

  companion object {
    const val LOG_TAG = "LootSpy Vendor Sync"
  }
}