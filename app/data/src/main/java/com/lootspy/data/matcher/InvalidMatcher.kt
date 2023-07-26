package com.lootspy.data.matcher

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.lootspy.data.DestinyItem

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