package com.lootspy.data.matcher

import com.lootspy.data.DestinyItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("NameMatcher")
class NameMatcher(var name: String) : FilterMatcher {

  override fun type() = MatcherType.NAME

  override fun match(item: DestinyItem) = item.name == name

  override fun summaryString() = "Name matcher: ${name.ifEmpty { "<blank>" }}"
}