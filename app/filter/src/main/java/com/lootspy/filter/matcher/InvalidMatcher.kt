package com.lootspy.filter.matcher

import kotlinx.serialization.SerialName
import com.lootspy.manifest.BasicItem
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InvalidMatcher")
object InvalidMatcher : FilterMatcher {
  override fun type(): MatcherType {
    TODO("Not yet implemented")
  }

  override fun match(item: BasicItem): Boolean {
    TODO("Not yet implemented")
  }

  override fun matcherTypeDescription(): String {
    TODO("Not yet implemented")
  }

  override fun describeMatcherValue(): String {
    TODO("Not yet implemented")
  }
}