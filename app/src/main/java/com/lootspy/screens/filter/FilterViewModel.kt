package com.lootspy.screens.filter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lootspy.R
import com.lootspy.data.UserStore
import com.lootspy.filter.Filter
import com.lootspy.data.repo.FilterRepository
import com.lootspy.data.source.LocalFilter
import com.lootspy.filter.toExternal
import com.lootspy.util.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FilterUiState(
  val items: List<Filter> = emptyList(),
  val alwaysPatterns: Boolean = false,
  val isLoading: Boolean = false,
  val userMessage: Int? = null,
)

@HiltViewModel
class FilterViewModel @Inject constructor(
  private val filterRepository: FilterRepository,
  private val userStore: UserStore,
) : ViewModel() {
  private val _isLoading = MutableStateFlow(false)
  private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
  private val _filtersAsync = filterRepository.getFiltersStream().map {
    Async.Success(it)
  }.catch<Async<List<LocalFilter>>> {
    Log.e("LootSpy", "filterGet", it)
    emit(Async.Error(R.string.loading_filters_error))
  }

  val uiState: StateFlow<FilterUiState> =
    combine(
      _filtersAsync,
      _isLoading,
      _userMessage,
      userStore.alwaysPatterns
    ) { filtersAsync, isLoading, userMessage, alwaysPatterns ->
      when (filtersAsync) {
        is Async.Loading -> {
          FilterUiState(isLoading = true)
        }

        is Async.Error -> {
          FilterUiState(userMessage = filtersAsync.errorMessage)
        }

        is Async.Success -> {
          FilterUiState(
            items = filtersAsync.data.toExternal(),
            alwaysPatterns = alwaysPatterns,
            isLoading = isLoading,
            userMessage = userMessage
          )
        }
      }
    }.stateIn(
      viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = FilterUiState(isLoading = true)
    )

  fun deleteAll() {
    viewModelScope.launch { filterRepository.deleteAllFilters() }
  }

  fun saveAlwaysPatterns(patterns: Boolean) {
    viewModelScope.launch { userStore.saveAlwaysPatterns(patterns) }
  }
}