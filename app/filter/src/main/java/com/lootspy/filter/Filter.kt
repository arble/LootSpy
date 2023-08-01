package com.lootspy.filter

import com.lootspy.types.matcher.FilterMatcher
import com.lootspy.types.item.BasicItem
import kotlinx.serialization.Serializable

@Serializable
class Filter(
  val id: String,
  val name: String,
  val matchers: List<FilterMatcher>
) {
  fun match(item: BasicItem) = matchers.any { it.match(item) }
}
