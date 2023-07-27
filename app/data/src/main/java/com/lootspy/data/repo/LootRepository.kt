package com.lootspy.data.repo

import com.lootspy.data.LootEntry
import kotlinx.coroutines.flow.Flow

interface LootRepository {
  fun getLootStream(): Flow<List<LootEntry>>

  suspend fun getLoot(): List<LootEntry>

  fun getLootEntryStream(lootEntryId: String): Flow<LootEntry?>

  suspend fun refresh()

  suspend fun getLootEntry(lootEntryId: String): LootEntry?

  suspend fun createLootEntry(name: String): String
}