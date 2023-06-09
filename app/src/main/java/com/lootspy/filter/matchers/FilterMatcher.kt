package com.lootspy.filter.matchers

import com.lootspy.data.DestinyItem

enum class MatcherType {
  NAME,
}

interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: DestinyItem): Boolean

  fun summaryString(): String
}