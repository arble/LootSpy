package com.lootspy.api.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.lootspy.api.ManifestManager
import com.lootspy.client.api.Destiny2Api
import com.lootspy.client.model.Destiny2GetVendor200Response
import com.lootspy.data.repo.CharacterRepository
import com.lootspy.data.DestinyItem
import com.lootspy.data.Filter
import com.lootspy.data.repo.FilterRepository
import com.lootspy.data.UserStore
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
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val allFilters = filterRepository.getFilters()
    if (allFilters.isEmpty()) {
      return Result.success()
    }
    val activeCharacterId = userStore.activeCharacter.first()
    if (activeCharacterId == 0L) {
      return Result.failure()
    }
    val character = characterRepository.getCharacter(activeCharacterId) ?: return Result.failure()

    val apiClient = Destiny2Api()
    ApiClient.accessToken = inputData.getString("access_token") ?: return Result.failure()
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
      !process200Response(bansheeResponse, itemsForSale) ||
      !process200Response(xurResponse, itemsForSale)
    ) {
      return Result.failure()
    }
    val itemData = manifestManager.lookupItemData(itemsForSale)
    val matchedLoot = mutableMapOf<Filter, DestinyItem>()
    for (item in itemData) {
      for (filter in allFilters) {
        if (filter.match(item)) {
          matchedLoot[filter] = item
          break
        }
      }
    }
    return Result.success()
  }

  private fun process200Response(
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
}