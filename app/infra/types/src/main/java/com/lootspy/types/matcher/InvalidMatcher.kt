package com.lootspy.types.matcher

import kotlinx.serialization.SerialName
import com.lootspy.types.item.BasicItem
import com.lootspy.types.item.DestinyItem
import kotlinx.serialization.Serializable

@Serializable
@SerialName("InvalidMatcher")
data object InvalidMatcher : FilterMatcher {
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

  override fun requiresItemDetails(): Boolean {
    TODO("Not yet implemented")
  }
}