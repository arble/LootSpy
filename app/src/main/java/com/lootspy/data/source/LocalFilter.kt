package com.lootspy.data.source

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
  tableName = "filters"
)
data class LocalFilter(
  @PrimaryKey val id: String,
  var name: String,
  var filterData: String,
)