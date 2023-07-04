package com.lootspy.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LocalLootEntry::class, LocalFilter::class], version = 2, exportSchema = false)
abstract class LootSpyDatabase : RoomDatabase() {

  abstract fun lootEntryDao(): LootEntryDao

  abstract fun filterDao(): FilterDao

  companion object {
    @Volatile
    private var instance: LootSpyDatabase? = null

    fun getInstance(context: Context): LootSpyDatabase {
      return instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(context, LootSpyDatabase::class.java, "matched_loot.db")
          .fallbackToDestructiveMigration().build()
      }
    }
  }
}