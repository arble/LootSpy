package com.lootspy.types.item

import com.lootspy.types.component.ItemPerk
import kotlinx.serialization.Serializable

@Serializable
class VendorItem(
  override val hash: UInt,
  val basicItem: BasicItem,
  val statsMap: Map<String, Pair<Int, String>>,
  val perkArray: List<List<ItemPerk>>,
) : DestinyItem {

  override fun basicItem() = basicItem

  class Builder {
    private lateinit var statHashesMap: Map<UInt, Int>
    private lateinit var perkHashes: List<List<UInt>>

    fun statHashes(statHashesMap: Map<UInt, Int>) = apply { this.statHashesMap = statHashesMap }

    fun perkHashes(perkHashes: List<List<UInt>>) = apply { this.perkHashes = perkHashes }

    fun build(
      item: BasicItem,
      stats: Map<UInt, Pair<String, String>>,
      perks: Map<UInt, ItemPerk>
    ): VendorItem {
      return VendorItem(
        item.hash,
        item,
        statHashesMap.mapValues { Pair(it.value, stats[it.key]!!.second) }
          .mapKeys { stats[it.key]?.first ?: INVALID_STAT },
        perkHashes.map { perkColumn -> perkColumn.map { perks[it] ?: ItemPerk.DUMMY_PERK } },
      )
    }
  }

  companion object {
    const val INVALID_STAT = "INVALID_STAT"
  }

  override fun shortName(): String {
    TODO("Not yet implemented")
  }
}