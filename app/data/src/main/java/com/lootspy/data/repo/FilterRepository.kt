package com.lootspy.data.repo

import com.lootspy.data.source.LocalFilter
import kotlinx.coroutines.flow.Flow

interface FilterRepository {
  fun getFiltersStream(): Flow<List<LocalFilter>>

  suspend fun getFilters(): List<LocalFilter>

  fun getFilterStream(filterId: String): Flow<LocalFilter>

  suspend fun refresh()

  suspend fun getFilter(filterId: String): LocalFilter?

  suspend fun saveNewFilter(name: String, matcherJson: String)

  suspend fun updateFilter(filterId: String, name: String, matcherJson: String)

  suspend fun deleteFilter(filterId: String)

  suspend fun deleteAllFilters()
}