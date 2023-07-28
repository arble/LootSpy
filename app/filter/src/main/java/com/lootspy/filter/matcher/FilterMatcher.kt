package com.lootspy.filter.matcher

import com.lootspy.manifest.BasicItem
import kotlinx.serialization.Serializable


@Serializable
sealed interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: BasicItem): Boolean

  fun matcherTypeDescription(): String

  fun describeMatcherValue(): String
}