package com.lootspy.filter.matchers

import com.lootspy.data.DestinyItem
import kotlinx.serialization.Serializable

enum class MatcherType {
  NAME {
    override fun printableName() = "Name"
  };

  abstract fun printableName(): String
}

//@Serializable(with = MatcherSerializer::class)
@Serializable
sealed interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: DestinyItem): Boolean

  fun summaryString(): String
}