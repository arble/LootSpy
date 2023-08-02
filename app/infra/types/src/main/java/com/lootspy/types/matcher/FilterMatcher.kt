package com.lootspy.types.matcher

import com.lootspy.types.item.BasicItem
import kotlinx.serialization.Serializable


@Serializable
sealed interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: BasicItem): Boolean

  fun matcherTypeDescription(): String

  fun describeMatcherValue(): String

  /**
   * Does this filter require access to an individual item's details? Filtering by name does not,
   * but filtering by stat total or perk combinations does (e.g.). We can save some effort when
   * parsing items if this is false for all currently active filters.
   */
  fun requiresItemDetails(): Boolean
}