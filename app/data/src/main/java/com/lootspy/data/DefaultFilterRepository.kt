package com.lootspy.data

import com.lootspy.di.ApplicationScope
import com.lootspy.di.DefaultDispatcher
import com.lootspy.data.matcher.FilterMatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultFilterRepository @Inject constructor(
  private val localDataSource: com.lootspy.data.source.FilterDao,
  @DefaultDispatcher private val dispatcher: CoroutineDispatcher,
  @ApplicationScope private val scope: CoroutineScope,
) : FilterRepository {
  override fun getFiltersStream(): Flow<List<Filter>> {
    return localDataSource.observeAll().map { withContext(dispatcher) { it.toExternal() } }
  }

  override suspend fun getFilters(): List<Filter> {
    return withContext(dispatcher) {
      localDataSource.getAll().toExternal()
    }
  }

  override fun getFilterStream(filterId: String): Flow<Filter?> {
    return localDataSource.observeById(filterId).map { it.toExternal() }
  }

  override suspend fun refresh() {
    TODO("Not yet implemented")
  }

  override suspend fun getFilter(filterId: String): Filter? {
    return localDataSource.getById(filterId)?.toExternal()
  }

  override suspend fun saveNewFilter(name: String, matchers: List<FilterMatcher>) {
    val filterId = withContext(dispatcher) { UUID.randomUUID().toString() }
    localDataSource.upsert(
      com.lootspy.data.source.LocalFilter(filterId, name, Json.encodeToString(matchers))
    )
  }

  override suspend fun updateFilter(filterId: String, name: String, matchers: List<FilterMatcher>) =
    localDataSource.upsert(
      com.lootspy.data.source.LocalFilter(filterId, name, Json.encodeToString(matchers))
    )

}