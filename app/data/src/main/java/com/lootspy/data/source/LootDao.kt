package com.lootspy.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LootDao {
  @Query("SELECT * FROM matched_loot")
  fun observeAll(): Flow<List<LocalLootEntry>>

  @Query("SELECT * FROM matched_loot WHERE hash = :lootEntryId")
  fun observeById(lootEntryId: Long): Flow<LocalLootEntry>

  @Query("SELECT * FROM matched_loot")
  fun getAll(): List<LocalLootEntry>

  @Query("SELECT * FROM matched_loot WHERE hash = :lootEntryId")
  fun getById(lootEntryId: Long): LocalLootEntry?

  @Upsert
  suspend fun upsert(lootEntry: LocalLootEntry)

  @Upsert
  suspend fun upsertAll(loot: List<LocalLootEntry>)

  @Query("DELETE FROM matched_loot WHERE hash = :lootEntryId")
  suspend fun deleteById(lootEntryId: Long)

  @Query("DELETE FROM matched_loot")
  suspend fun deleteAll()
}