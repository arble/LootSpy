package com.lootspy.types.item

import kotlinx.serialization.Serializable

@Serializable
sealed interface DestinyItem {
  val hash: UInt

  fun shortName(): String

  fun basicItem(): BasicItem
}