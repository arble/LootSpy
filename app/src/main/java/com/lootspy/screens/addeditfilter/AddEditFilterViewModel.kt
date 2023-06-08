package com.lootspy.screens.addeditfilter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lootspy.R
import com.lootspy.LootSpyDestinationArgs
import com.lootspy.data.Filter
import com.lootspy.data.FilterRepository
import com.lootspy.filter.matchers.FilterMatcher
import com.lootspy.filter.matchers.MatcherType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddEditFilterUiState(
  val id: String = "",
  val name: String = "New Filter",
  val matchers: List<FilterMatcher> = emptyList(),
  val userMessage: Int? = null,
  val isLoading: Boolean = false,
  val isFilterSaved: Boolean = false,
)

@HiltViewModel
class AddEditFilterViewModel @Inject constructor(
  private val filterRepository: FilterRepository,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val filterId: String? = savedStateHandle[LootSpyDestinationArgs.FILTER_ID_ARG]

  private val _uiState = MutableStateFlow(AddEditFilterUiState())
  val uiState: StateFlow<AddEditFilterUiState> = _uiState.asStateFlow()

  init {
    if (filterId != null) {
      loadFilter(filterId)
    }
  }

  fun saveFilter() {
    if (uiState.value.name.isEmpty()) {
      _uiState.update {
        it.copy(userMessage = R.string.empty_filter_name_message)
        return
      }
    }
    if (uiState.value.matchers.isEmpty()) {
      _uiState.update {
        it.copy(userMessage = R.string.empty_filter_matchers_message)
        return
      }
    }

    if (filterId == null) {
      createNewFilter()
    } else {
      updateFilter()
    }
  }

  private fun createNewFilter() = viewModelScope.launch {
    filterRepository.saveFilter(uiState.value.id, uiState.value.name, uiState.value.matchers)
    _uiState.update { it.copy() }
  }

  private fun updateFilter() {
    if (filterId == null) {
      throw RuntimeException("updateFilter on null filterId")
    }
    viewModelScope.launch {
      filterRepository.updateFilter(filterId, uiState.value.name, uiState.value.matchers)
      _uiState.update {
        it.copy(isFilterSaved = true)
      }
    }
  }

  private fun loadFilter(filterId: String) {
    _uiState.update { it.copy(isLoading = true) }

    viewModelScope.launch {
      filterRepository.getFilter(filterId).let { filter: Filter? ->
        if (filter != null) {
          _uiState.update {
            it.copy(
              name = filter.name,
              matchers = filter.matchers
            )
          }
        } else {
          _uiState.update {
            it.copy(isLoading = false)
          }
        }
      }
    }
  }

  fun createBlankMatcher(type: MatcherType) {

  }
}