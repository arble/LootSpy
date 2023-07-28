package com.lootspy.filter.matcher

import com.lootspy.data.DestinyItem
import kotlinx.serialization.Serializable


@Serializable
sealed interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: DestinyItem): Boolean

  fun matcherTypeDescription(): String

  fun describeMatcherValue(): String
}