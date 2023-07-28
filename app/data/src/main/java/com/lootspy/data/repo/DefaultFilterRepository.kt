package com.lootspy.data.repo

import com.lootspy.data.source.FilterDao
import com.lootspy.data.source.LocalFilter
import com.lootspy.di.ApplicationScope
import com.lootspy.di.DefaultDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultFilterRepository @Inject constructor(
  private val localDataSource: FilterDao,
  @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
  @ApplicationScope private val scope: CoroutineScope,
) : FilterRepository {
  override fun getFiltersStream(): Flow<List<LocalFilter>> {
    return localDataSource.observeAll()
  }

  override suspend fun getFilters(): List<LocalFilter> {
    return withContext(dispatcher) { localDataSource.getAll() }
  }

  override fun getFilterStream(filterId: String): Flow<LocalFilter> {
    return localDataSource.observeById(filterId)
  }

  override suspend fun refresh() {
    TODO("Not yet implemented")
  }

  override suspend fun getFilter(filterId: String): LocalFilter? {
    return localDataSource.getById(filterId)
  }

  override suspend fun saveNewFilter(name: String, matcherJson: String) {
    val filterId = withContext(dispatcher) { UUID.randomUUID().toString() }
    localDataSource.upsert(
      LocalFilter(filterId, name, matcherJson)
    )
  }

  override suspend fun updateFilter(filterId: String, name: String, matcherJson: String) =
    localDataSource.upsert(
      LocalFilter(filterId, name, matcherJson)
    )

  override suspend fun deleteFilter(filterId: String) {
    localDataSource.deleteById(filterId)
  }

  override suspend fun deleteAllFilters() {
    localDataSource.deleteAll()
  }

}