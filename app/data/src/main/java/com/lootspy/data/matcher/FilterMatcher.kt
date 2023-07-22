package com.lootspy.data.matcher

import com.lootspy.data.DestinyItem
import com.lootspy.data.R
import kotlinx.serialization.Serializable

enum class MatcherType {
  INVALID {
    override fun printableName() = "INVALID"

    override fun descriptionResource() = R.string.matcher_type_desc_invalid

  },
  NAME {
    override fun printableName() = "Name"
    override fun descriptionResource() = R.string.matcher_type_desc_name
  };

  abstract fun printableName(): String

  abstract fun descriptionResource(): Int

  companion object {
    fun displayValues() = values().filter { it != INVALID }
  }
}

//@Serializable(with = MatcherSerializer::class)
@Serializable
sealed interface FilterMatcher {

  fun type(): MatcherType

  fun match(item: DestinyItem): Boolean

  fun summaryString(): String
}