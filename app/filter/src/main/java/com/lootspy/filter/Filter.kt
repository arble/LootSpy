package com.lootspy.filter

import com.lootspy.types.matcher.FilterMatcher
import com.lootspy.types.item.BasicItem
import com.lootspy.types.item.DestinyItem
import kotlinx.serialization.Serializable

@Serializable
class Filter(
  val id: String,
  val name: String,
  val matchers: List<FilterMatcher>
) {
  fun match(item: DestinyItem) = matchers.any { it.match(item) }

  fun requiresItemDetails() = matchers.any { it.requiresItemDetails() }
}
