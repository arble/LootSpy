package com.lootspy.types.item

import com.lootspy.types.component.ItemPerk
import kotlinx.serialization.Serializable

@Serializable
class VendorItem(
  // === essential properties ===
  val hash: UInt,
  val name: String,
  val tier: String,
  val itemType: String,
  val iconPath: String,
  val watermarkPath: String,
  val isShelved: Boolean,

  // === weapon properties ===
  val damageType: String?,
  val damageIconPath: String?,
  val statsMap: Map<String, Int>?,
  val perkArray: List<List<ItemPerk>>?,
) {

  // autocomplete "item" ctor
  constructor(
    hash: UInt,
    name: String,
    tier: String,
    itemType: String,
    iconPath: String,
    watermarkPath: String,
    isShelved: Boolean,
    damageType: String?,
    damageIconPath: String?,
  ) :
      this(
        hash,
        name,
        tier,
        itemType,
        iconPath,
        watermarkPath,
        isShelved,
        damageType,
        damageIconPath,
        null,
        null
      )

  private fun statNames() = when (itemType) {
    "Fusion Rifle" -> listOf(
      "Charge Time",
      "Impact",
      "Range",
      "Stability",
      "Handling",
      "Reload Speed"
    )
    "Bow" -> listOf("Draw Time", "Impact", "Accuracy", "Stability", "Handling", "Reload Speed")
    "Glaive" -> listOf(
      "Rounds Per Minute",
      "Impact",
      "Range",
      "Shield Duration",
      "Handling",
      "Reload Speed"
    )
    else -> listOf("Rounds Per Minute", "Impact", "Range", "Stability", "Handling", "Reload Speed")
  }

  fun itemStatMap(): Map<String, Int>? {
    if (statsMap == null) {
      return null
    }
    val result = mutableMapOf<String, Int>()
    for (stat in statNames()) {
      result[stat] = statsMap[stat]!!
    }
    return result
  }

  class Builder(

  ) {
    private var damageType: String? = null
    private var damageIconPath: String? = null
    private var statHashesMap: Map<UInt, Int>? = null
    private var perkHashes: List<List<UInt>>? = null
    private lateinit var coreProperties: VendorItemCoreProperties

    fun coreProperties(coreProperties: VendorItemCoreProperties) =
      apply { this.coreProperties = coreProperties }

    fun damageType(damageType: String?) = apply { this.damageType = damageType }

    fun damageIconPath(damageIconPath: String?) = apply { this.damageIconPath = damageIconPath }

    fun statHashes(statHashesMap: Map<UInt, Int>?) = apply { this.statHashesMap = statHashesMap }

    fun perkHashes(perkHashes: List<List<UInt>>?) = apply { this.perkHashes = perkHashes }

    fun build(
      stats: Map<UInt, Pair<String, String>>,
      perks: Map<UInt, ItemPerk>
    ): VendorItem {
      return VendorItem(
        coreProperties.hash,
        coreProperties.name,
        coreProperties.tier,
        coreProperties.itemType,
        coreProperties.iconPath,
        coreProperties.watermarkPath,
        coreProperties.isShelved,
        damageType,
        damageIconPath,
        statHashesMap?.mapKeys { stats[it.key]?.first ?: INVALID_STAT },
        perkHashes?.map { perkColumn -> perkColumn.map { perks[it] ?: ItemPerk.DUMMY_PERK } },
      )
    }
  }

  companion object {
    const val INVALID_STAT = "INVALID_STAT"
  }
}