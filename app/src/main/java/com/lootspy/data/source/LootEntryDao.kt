package com.lootspy.data.source

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LootEntryDao {
  @Query("SELECT * FROM matched_loot")
  fun observeAll(): Flow<List<LocalLootEntry>>

  @Query("SELECT * FROM matched_loot WHERE id = :lootEntryId")
  fun observeById(lootEntryId: String): Flow<LocalLootEntry>

  @Query("SELECT * FROM matched_loot")
  fun getAll(): List<LocalLootEntry>

  @Query("SELECT * FROM matched_loot WHERE id = :lootEntryId")
  fun getById(lootEntryId: String): LocalLootEntry?

  @Upsert
  suspend fun upsert(lootEntry: LocalLootEntry)

  @Upsert
  suspend fun upsertAll(loot: List<LocalLootEntry>)

  @Query("DELETE FROM matched_loot WHERE id = :lootEntryId")
  suspend fun deleteById(lootEntryId: String): Int

  @Query("DELETE FROM matched_loot")
  suspend fun deleteAll()
}