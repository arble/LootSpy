package com.lootspy.data.matcher

import com.lootspy.data.DestinyItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("ItemMatcher")
class ItemMatcher(val name: String, val hash: UInt) : FilterMatcher {

  override fun type() = MatcherType.NAME

  override fun match(item: DestinyItem) = item.name == name

  override fun summaryString() = "Match single item: ${name.ifEmpty { "" }}"
}