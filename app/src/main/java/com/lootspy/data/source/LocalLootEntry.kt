package com.lootspy.data.source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
  tableName = "matched_loot"
)
data class LocalLootEntry (
  @PrimaryKey val id: String,
  var name: String,
)