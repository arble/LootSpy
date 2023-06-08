package com.lootspy.filter.matchers

import androidx.compose.runtime.Composable
import com.lootspy.data.DestinyItem

enum class MatcherType {
  NAME,
}

interface FilterMatcher {
  fun match(item: DestinyItem): Boolean

  fun summaryString(): String
}