package com.lootspy.data

import com.lootspy.filter.matchers.FilterMatcher
import kotlinx.serialization.Serializable

@Serializable
class Filter(
  val id: String,
  val name: String,
  val matchers: List<FilterMatcher>
) {

  fun match (item: DestinyItem): Boolean = matchers.any { it.match(item) }
}
