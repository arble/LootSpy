package com.lootspy.filter.matchers

import android.os.Parcelable
import com.lootspy.data.DestinyItem
import kotlinx.parcelize.Parcelize

@Parcelize
class NameMatcher(var name: String) : FilterMatcher, Parcelable {

  override fun type() = MatcherType.NAME

  override fun match(item: DestinyItem) = item.name == name

  override fun summaryString() = "Name matcher: ${name.ifEmpty { "<blank>" }}"
}