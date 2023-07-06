package com.lootspy.data.matcher

import com.lootspy.data.DestinyItem
import com.lootspy.data.R
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