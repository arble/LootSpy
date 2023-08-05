package com.lootspy.types.item

import com.lootspy.types.component.ItemPerk
import kotlinx.serialization.Serializable

@Serializable
class VendorItem(
  val basicItem: BasicItem,
  val statsMap: Map<String, Int>,
  val perkArray: List<List<ItemPerk>>,
  val sockets: List<UInt>
) {}