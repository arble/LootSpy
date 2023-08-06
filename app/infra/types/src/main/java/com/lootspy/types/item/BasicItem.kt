package com.lootspy.types.item

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class BasicItem(
  override val hash: UInt,
  val name: String,
  val tier: String,
  @SerialName("itemType") val type: String,
  val iconPath: String,
  val watermarkPath: String,
  val isShelved: Boolean,
  val damageType: String,
  val damageIconPath: String,
) : DestinyItem {

  override fun basicItem() = this

  override fun shortName() = name
}