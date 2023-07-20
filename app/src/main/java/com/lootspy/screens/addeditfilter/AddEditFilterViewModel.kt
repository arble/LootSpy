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
import com.lootspy.data.matcher.MatcherType
import com.lootspy.data.matcher.NameMatcher
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
  val name: String = "New Filter",
  val filter: Filter? = null,
  val matchers: List<FilterMatcher> = mutableListOf(),
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
  private val _suggestions = MutableStateFlow(Pair("", listOf<AutocompleteItem>()))
  val suggestions = _suggestions.asStateFlow()

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

  private fun buildMatcherFieldMap(matcher: FilterMatcher): Map<String, String> {
    val result = HashMap<String, String>()
    when (matcher) {
      is NameMatcher -> {
        result["name"] = matcher.name
      }
    }
    return result
  }

  fun createBlankMatcher(type: MatcherType) {
    _uiState.update {
      val matchers = it.matchers.toMutableList()
      if (type == MatcherType.NAME) {
        matchers.add(NameMatcher(""))
      }
      val index = matchers.size - 1
      it.copy(
        matchers = matchers,
        selectedMatcher = index,
        selectedMatcherFields = buildMatcherFieldMap(matchers[index]),
      )
    }
  }

  fun updateSelectedMatcher(index: Int) {
    _uiState.update {
      it.copy(
        selectedMatcher = index,
        selectedMatcherFields = buildMatcherFieldMap(it.matchers[index]),
      )
    }
  }

  private fun checkAlreadyMatchedName(
    newMatcher: NameMatcher,
    matchers: List<NameMatcher>
  ): Pair<Boolean, Set<NameMatcher>> {
    val redundantMatchers = HashSet<NameMatcher>()
    for (matcher in matchers) {
      if (matcher.name.contains(newMatcher.name)) {
        return Pair(true, redundantMatchers)
      }
      if (newMatcher.name.contains(matcher.name)) {
        redundantMatchers.add(matcher)
      }
    }
    return Pair(false, redundantMatchers)
  }

  private fun checkAlreadyMatched(
    newMatcher: FilterMatcher,
    matchers: List<FilterMatcher>
  ): Pair<Boolean, Set<FilterMatcher>> {
    when (newMatcher.type()) {
      MatcherType.NAME -> {
        return checkAlreadyMatchedName(
          newMatcher as NameMatcher,
          matchers.filter { it !== newMatcher }.filterIsInstance(NameMatcher::class.java)
        )
      }
    }
  }

  fun updateMatcherFields(fields: Map<String, String>, matchers: List<FilterMatcher>): Boolean {
    var matcher: FilterMatcher? = null

    when (fields["MATCHER_TYPE"]) {
      MatcherType.NAME.name -> {
        matcher = NameMatcher(fields["name"]!!)
      }
    }
    if (matcher == null) {
      return true
    }
    val newMatchers = List(matchers.size) { index ->
      // replace the appropriate Matcher with our new one
      if (index != uiState.value.selectedMatcher) {
        return@List uiState.value.matchers[index]
      } else {
        return@List matcher
      }
    }
    val (alreadyMatched, redundantMatchers) = checkAlreadyMatched(matcher, newMatchers)
    if (alreadyMatched) {
      return false
    }
    val (resultMatchers, removedMatchers) =
      if (redundantMatchers.isEmpty()) Pair(newMatchers, null) else Pair(
        newMatchers.subtract(redundantMatchers).toList(),
        redundantMatchers.size
      )
    _uiState.update { state ->
      state.copy(
        matchers = resultMatchers,
        removedMatchers = removedMatchers,
        selectedMatcher = null,
      )
    }
    _suggestions.update { Pair("", emptyList()) }
    return true
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
        _suggestions.update { Pair("", emptyList()) }
      } else {
        withContext(Dispatchers.IO) {
          if (autocompleteHelper.items.isEmpty()) {
            manifestManager.populateItemAutocomplete()
          }
          _suggestions.update { Pair(text, autocompleteHelper.suggest(text, limit)) }
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