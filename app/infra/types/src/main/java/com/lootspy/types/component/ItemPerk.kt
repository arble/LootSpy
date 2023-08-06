package com.lootspy.types.component

import kotlinx.serialization.Serializable

@Serializable
class ItemPerk(
  val hash: UInt,
  val name: String,
  val iconPath: String,
) {
  companion object {
    val DUMMY_PERK = ItemPerk(0U, "DUMMY", "INVALID")
  }
}