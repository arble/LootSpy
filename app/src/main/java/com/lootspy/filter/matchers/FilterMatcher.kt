package com.lootspy.filter.matchers

import com.example.lootspy.R
import com.lootspy.data.DestinyItem
import kotlinx.serialization.Serializable

enum class MatcherType {
  NAME {
    override fun printableName() = "Name"
    override fun descriptionResource() = R.string.matcher_type_desc_name
  };

  abstract fun printableName(): String

  abstract fun descriptionResource(): Int
}

//@Serializable(with = MatcherSerializer::class)
@Serializable
sealed interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: DestinyItem): Boolean

  fun summaryString(): String
}