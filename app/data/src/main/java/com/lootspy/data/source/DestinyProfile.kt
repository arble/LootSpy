package com.lootspy.data.source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class DestinyProfile(
  @PrimaryKey val membershipId: Long,
  val membershipType: Int,
  val displayName: String,
  val platformDisplayName: String,
  val iconPath: String,
  val bungieDisplayName: String,
  val bungieDisplayNameCode: Int,
)