package com.lootspy.filter.matchers

import com.lootspy.data.DestinyItem

interface FilterMatcher {
  fun match(item: DestinyItem): Boolean

  fun summaryString(): String
}