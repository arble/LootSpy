package com.lootspy.types.matcher

import com.lootspy.types.item.BasicItem
import com.lootspy.types.item.DestinyItem
import com.lootspy.types.item.VendorItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ItemMatcher")
class ItemMatcher(private val name: String, val hash: UInt) : FilterMatcher {

  override fun type() = MatcherType.NAME

  override fun match(item: VendorItem) = item.hash == hash

  override fun matcherTypeDescription() = "Match single item"

  override fun describeMatcherValue() = name

  override fun requiresItemDetails() = false
}