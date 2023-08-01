package com.lootspy.types.item

open class BasicItem(
  override val hash: UInt,
  val name: String,
  val tier: String,
  val type: String,
  val iconPath: String,
  val watermarkPath: String,
  val isShelved: Boolean,
  val damageType: String,
  val damageIconPath: String,
) : DestinyItem {
  override fun shortName() = name
}