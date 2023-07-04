package com.lootspy.data

import com.lootspy.data.matcher.FilterMatcher
import kotlinx.coroutines.flow.Flow

interface FilterRepository {
  fun getFiltersStream(): Flow<List<Filter>>

  suspend fun getFilters(): List<Filter>

  fun getFilterStream(filterId: String): Flow<Filter?>

  suspend fun refresh()

  suspend fun getFilter(filterId: String): Filter?

  suspend fun saveNewFilter(name: String, matchers: List<FilterMatcher>)

  suspend fun updateFilter(filterId: String, name: String, matchers: List<FilterMatcher>)
}