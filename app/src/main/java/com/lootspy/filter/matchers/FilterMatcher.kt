package com.lootspy.filter.matchers

import com.lootspy.data.DestinyItem

enum class MatcherType {
  NAME {
    override fun printableName() = "Name"
  };

  abstract fun printableName(): String
}

interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: DestinyItem): Boolean

  fun summaryString(): String
}