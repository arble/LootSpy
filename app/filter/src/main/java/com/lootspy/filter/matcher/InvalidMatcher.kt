package com.lootspy.filter.matcher

import kotlinx.serialization.SerialName
import com.lootspy.data.DestinyItem
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InvalidMatcher")
object InvalidMatcher : FilterMatcher {
  override fun type(): MatcherType {
    TODO("Not yet implemented")
  }

  override fun match(item: DestinyItem): Boolean {
    TODO("Not yet implemented")
  }

  override fun matcherTypeDescription(): String {
    TODO("Not yet implemented")
  }

  override fun describeMatcherValue(): String {
    TODO("Not yet implemented")
  }
}