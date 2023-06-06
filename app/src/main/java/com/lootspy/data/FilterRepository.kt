package com.lootspy.data

import com.lootspy.filter.matchers.FilterMatcher
import kotlinx.coroutines.flow.Flow

interface FilterRepository {
  fun getFiltersStream(): Flow<List<Filter>>

  suspend fun getFilters(): List<Filter>

  fun getFilterStream(filterId: String): Flow<Filter?>

  suspend fun refresh()

  suspend fun getFilter(filterId: String): Filter?

  suspend fun saveFilter(id: String, name: String, matchers: List<FilterMatcher>)

  suspend fun updateFilter(filterId: String, name: String, matchers: List<FilterMatcher>)
}