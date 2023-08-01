package com.lootspy.types.item

interface DestinyItem {
  val hash: UInt

  fun shortName(): String
}