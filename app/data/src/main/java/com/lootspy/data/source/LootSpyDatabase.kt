package com.lootspy.data.source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [LocalLootEntry::class, LocalFilter::class, DestinyProfile::class, DestinyCharacter::class],
  version = 5,
  exportSchema = false
)
abstract class LootSpyDatabase : RoomDatabase() {

  abstract fun lootDao(): LootDao

  abstract fun filterDao(): FilterDao

  abstract fun profileDao(): ProfileDao

  abstract fun characterDao(): CharacterDao

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