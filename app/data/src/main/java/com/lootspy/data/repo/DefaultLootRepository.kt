package com.lootspy.data.repo

import com.lootspy.data.LootEntry
import com.lootspy.data.toExternal
import com.lootspy.data.toLocal
import com.lootspy.di.ApplicationScope
import com.lootspy.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultLootRepository @Inject constructor(
  private val localDataSource: com.lootspy.data.source.LootDao,
  @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
  @ApplicationScope private val scope: CoroutineScope,
) : LootRepository {

  override fun getLootStream(): Flow<List<LootEntry>> {
    return localDataSource.observeAll().map { withContext(dispatcher) { it.toExternal() } }
  }

  override suspend fun getLoot(): List<LootEntry> {
    return withContext(dispatcher) {
      localDataSource.getAll().toExternal()
    }
  }

  override fun getLootEntryStream(lootEntryId: Long): Flow<LootEntry?> {
    return localDataSource.observeById(lootEntryId).map { it.toExternal() }
  }

  override suspend fun refresh() {
    TODO("Not yet implemented")
  }

  override suspend fun getLootEntry(lootEntryId: Long): LootEntry? {
    return localDataSource.getById(lootEntryId)?.toExternal()
  }

  override suspend fun saveLootEntry(item: LootEntry) {
    localDataSource.upsert(item.toLocal())
  }

  override suspend fun clearLoot() {
    localDataSource.deleteAll()
  }
}