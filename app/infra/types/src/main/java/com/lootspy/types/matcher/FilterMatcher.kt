package com.lootspy.types.matcher

import com.lootspy.types.item.BasicItem
import kotlinx.serialization.Serializable


@Serializable
sealed interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: BasicItem): Boolean

  fun matcherTypeDescription(): String

  fun describeMatcherValue(): String
}