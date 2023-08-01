package com.lootspy.types.matcher

import com.lootspy.types.item.BasicItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ItemMatcher")
class ItemMatcher(private val name: String, val hash: UInt) : FilterMatcher {

  override fun type() = MatcherType.NAME

  override fun match(item: BasicItem) = item.hash == hash

  override fun matcherTypeDescription() = "Match single item"
  override fun describeMatcherValue() = name
}