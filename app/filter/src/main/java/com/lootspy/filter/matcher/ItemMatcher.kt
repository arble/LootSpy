package com.lootspy.filter.matcher

import com.lootspy.data.DestinyItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ItemMatcher")
class ItemMatcher(private val name: String, val hash: UInt) : FilterMatcher {

  override fun type() = MatcherType.NAME

  override fun match(item: DestinyItem) = item.name == name

  override fun matcherTypeDescription() = "Match single item"
  override fun describeMatcherValue() = name
}