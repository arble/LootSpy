package com.lootspy.screens.filter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lootspy.R
import com.lootspy.data.Filter
import com.lootspy.data.FilterRepository
import com.lootspy.util.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class FilterUiState(
  val items: List<Filter> = emptyList(),
  val isLoading: Boolean = false,
  val userMessage: Int? = null,
)

@HiltViewModel
class FilterViewModel @Inject constructor(
  filterRepository: FilterRepository,
) : ViewModel() {
  private val _isLoading = MutableStateFlow(false)
  private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
  private val _filtersAsync = filterRepository.getFiltersStream().map {
    Async.Success(it)
  }.catch<Async<List<Filter>>> {
    Log.e("LootSpy", "filterGet", it)
    emit(Async.Error(R.string.loading_filters_error))
  }

  val uiState: StateFlow<FilterUiState> =
    combine(_filtersAsync, _isLoading, _userMessage) { filtersAsync, isLoading, userMessage ->
      when (filtersAsync) {
        is Async.Loading -> {
          FilterUiState(isLoading = true)
        }

        is Async.Error -> {
          FilterUiState(userMessage = filtersAsync.errorMessage)
        }

        is Async.Success -> {
          FilterUiState(items = filtersAsync.data, isLoading = isLoading, userMessage = userMessage)
        }
      }
    }.stateIn(
      viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = FilterUiState(isLoading = true)
    )
}