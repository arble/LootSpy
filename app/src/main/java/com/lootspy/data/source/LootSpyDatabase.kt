package com.lootspy.data.source

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LocalLootEntry::class, LocalFilter::class], version = 2, exportSchema = false)
abstract class LootSpyDatabase : RoomDatabase() {

  abstract fun lootEntryDao(): LootEntryDao

  abstract fun filterDao(): FilterDao
}