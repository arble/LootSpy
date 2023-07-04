package com.lootspy.data

import com.lootspy.di.ApplicationScope
import com.lootspy.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultLootRepository @Inject constructor(
  private val localDataSource: com.lootspy.data.source.LootEntryDao,
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

  override fun getLootEntryStream(lootEntryId: String): Flow<LootEntry?> {
    return localDataSource.observeById(lootEntryId).map { it.toExternal() }
  }

  override suspend fun refresh() {
    TODO("Not yet implemented")
  }

  override suspend fun getLootEntry(lootEntryId: String): LootEntry? {
    return localDataSource.getById(lootEntryId)?.toExternal()
  }

  override suspend fun createLootEntry(name: String): String {
    val lootEntryId = withContext(dispatcher) {
      UUID.randomUUID().toString()
    }
    val lootEntry = LootEntry(lootEntryId, name)
    localDataSource.upsert(lootEntry.toLocal())
    return lootEntryId
  }
}