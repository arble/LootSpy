package com.lootspy.filter

import com.lootspy.data.DestinyItem
import com.lootspy.filter.matcher.FilterMatcher
import com.lootspy.manifest.BasicItem
import kotlinx.serialization.Serializable

@Serializable
class Filter(
  val id: String,
  val name: String,
  val matchers: List<FilterMatcher>
) {

  fun match(item: DestinyItem): Boolean = matchers.any { it.match(item) }

  fun match(item: BasicItem): Boolean = true
}
