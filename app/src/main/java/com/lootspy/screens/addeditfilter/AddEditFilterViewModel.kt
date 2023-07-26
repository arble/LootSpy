package com.lootspy.screens.addeditfilter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lootspy.LootSpyDestinationArgs
import com.lootspy.R
import com.lootspy.api.AutocompleteHelper
import com.lootspy.api.AutocompleteItem
import com.lootspy.api.ManifestManager
import com.lootspy.data.Filter
import com.lootspy.data.FilterRepository
import com.lootspy.data.matcher.FilterMatcher
import com.lootspy.data.matcher.InvalidMatcher
import com.lootspy.data.matcher.MatcherType
import com.lootspy.data.matcher.ItemMatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AddEditFilterUiState(
  val id: String = "",
  val name: String = "",
  val filter: Filter? = null,
  val matchers: List<FilterMatcher> = listOf(),
  val selectedMatcher: Int? = null,
  val selectedMatcherFields: Map<String, String>? = null,
  val removedMatchers: Int? = null,
  val userMessage: Int? = null,
  val isLoading: Boolean = false,
  val isFilterSaved: Boolean = false,
)

@HiltViewModel
class AddEditFilterViewModel @Inject constructor(
  private val filterRepository: FilterRepository,
  private val manifestManager: ManifestManager,
  private val autocompleteHelper: AutocompleteHelper,
  savedStateHandle: SavedStateHandle
) : ViewModel() {

  private val filterId: String? = savedStateHandle[LootSpyDestinationArgs.FILTER_ID_ARG]

  private val _uiState = MutableStateFlow(AddEditFilterUiState())
  val uiState = _uiState.asStateFlow()
  private val _suggestions = MutableStateFlow(listOf<AutocompleteItem>())
  val suggestions = _suggestions.asStateFlow()
  private val _activeMatcher = MutableStateFlow<Pair<FilterMatcher?, Int?>>(Pair(null, null))
  val activeMatcher = _activeMatcher.asStateFlow()

  init {
    if (filterId != null) {
      loadFilter(filterId)
    }
  }

  fun createNewFilter(name: String) = viewModelScope.launch {
    filterRepository.saveNewFilter(name, uiState.value.matchers)
    _uiState.update { it.copy() }
  }

  fun updateFilter() {
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

  fun changeFilterName(name: String) {
    _uiState.update { it.copy(name = name) }
  }

  private fun loadFilter(filterId: String) {
    _uiState.update { it.copy(isLoading = true) }

    viewModelScope.launch {
      filterRepository.getFilter(filterId).let { filter: Filter? ->
        if (filter != null) {
          _uiState.update {
            it.copy(
              name = filter.name,
              filter = filter,
              matchers = filter.matchers,
              isLoading = false
            )
          }
        } else {
          _uiState.update {
            it.copy(isLoading = false, userMessage = R.string.loading_filters_error)
          }
        }
      }
    }
  }

  fun setActiveMatcher(
    matcher: FilterMatcher? = null,
    index: Int? = null,
    type: MatcherType = MatcherType.INVALID
  ) {
    if (matcher != null && index != null) {
      _activeMatcher.update { Pair(matcher, index) }
    } else {
      val newMatcher = when (type) {
        MatcherType.NAME -> ItemMatcher("", 0U)
        else -> InvalidMatcher
      }
      _activeMatcher.update { Pair(newMatcher, index) }
    }
  }

  fun saveItemMatcher(index: Int?, item: AutocompleteItem): Boolean {
    uiState.value.matchers.forEachIndexed { oldIndex, matcher ->
      if (matcher is ItemMatcher && matcher.hash == item.hash && oldIndex != index) {
        return false
      }
    }
    val newMatcher = ItemMatcher(item.name, item.hash)
    val newMatchers = uiState.value.matchers.toMutableList()
    if (index != null) {
      newMatchers[index] = newMatcher
    } else {
      newMatchers.add(newMatcher)
    }
    _uiState.update { it.copy(matchers = newMatchers) }
    return true
  }

  fun clearActiveFilter() {
    _suggestions.update { emptyList() }
    _activeMatcher.update { Pair(null, null) }
  }

  fun isItemAlreadyMatched(item: AutocompleteItem): Boolean {
    return uiState.value.matchers.find { it is ItemMatcher && it.hash == item.hash } != null
  }

  fun deleteSelectedMatcher() {
    _uiState.update {
      it.copy(
        matchers = it.matchers.filterIndexed { index, _ -> index != it.selectedMatcher },
        selectedMatcher = null,
      )
    }
  }

  fun getSuggestions(text: String, limit: Int = 5) {
    viewModelScope.launch {
      if (text.isEmpty()) {
        _suggestions.update { emptyList() }
      } else {
        withContext(Dispatchers.IO) {
          if (autocompleteHelper.items.isEmpty()) {
            manifestManager.populateItemAutocomplete()
          }
          _suggestions.update { autocompleteHelper.suggest(text, limit) }
        }
      }
    }
  }

  fun onRedundantMatcherSnackbarDismiss() {
    _uiState.update { it.copy(removedMatchers = null) }
  }

  fun checkModifiedFilter(): Boolean {
    if (filterId == null && uiState.value.matchers.isNotEmpty()) {
      return true
    }
    return uiState.value.matchers.isNotEmpty() &&
        uiState.value.filter?.matchers != uiState.value.matchers
  }
}