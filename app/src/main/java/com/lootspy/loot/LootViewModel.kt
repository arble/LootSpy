package com.lootspy.loot

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lootspy.R
import com.lootspy.data.LootEntry
import com.lootspy.data.LootRepository
import com.lootspy.util.Async
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LootUiState(
  val items: List<LootEntry> = emptyList(),
  val isLoading: Boolean = false,
  val filteringUiInfo: FilteringUiInfo = FilteringUiInfo(),
  val userMessage: Int? = null
)

data class FilteringUiInfo(
  val currentFilteringLabel: Int = R.string.label_all_filter,
  val vendorLabel: Int = R.string.label_vendor_filter
)

@HiltViewModel
class LootViewModel @Inject constructor(
  private val lootRepository: LootRepository,
  private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

  companion object {
    private const val LOOT_FILTER_SAVED_STATE_KEY = "LOOT_FILTER_SAVED_STATE_KEY"
  }

  private val savedFilterType = savedStateHandle.getStateFlow(
    LOOT_FILTER_SAVED_STATE_KEY,
    LootFilterType.ALL_LOOT
  )
  private val _filterUiInfo = savedFilterType.map { getFilterUiInfo(it) }.distinctUntilChanged()
  private val _userMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
  private val _isLoading = MutableStateFlow(false)
  private val _filteredLootEntriesAsync =
    combine(lootRepository.getLootStream(), savedFilterType) { loot, type ->
      filterLoot(loot, type)
    }.map { Async.Success(it) }
      .catch<Async<List<LootEntry>>> { emit(Async.Error(R.string.loading_tasks_error)) }

  val uiState: StateFlow<LootUiState> = combine(
    _filterUiInfo,
    _isLoading,
    _userMessage,
    _filteredLootEntriesAsync
  ) { filterUiInfo, isLoading, userMessage, lootEntriesAsync ->
    when (lootEntriesAsync) {
      is Async.Loading -> {
        LootUiState(isLoading = true)
      }

      is Async.Error -> {
        LootUiState(userMessage = lootEntriesAsync.errorMessage)
      }

      is Async.Success -> {
        LootUiState(
          items = lootEntriesAsync.data,
          isLoading = isLoading,
          filteringUiInfo = filterUiInfo,
          userMessage = userMessage
        )
      }
    }
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = LootUiState(isLoading = true)
  )

  fun setFiltering(requestType: LootFilterType) {
    savedStateHandle[LOOT_FILTER_SAVED_STATE_KEY] = requestType
  }

  private fun getFilterUiInfo(requestType: LootFilterType): FilteringUiInfo {
    return FilteringUiInfo()
  }

  private fun filterLoot(loot: List<LootEntry>, type: LootFilterType): List<LootEntry> {
    return when (type) {
      LootFilterType.ALL_LOOT -> loot
      LootFilterType.VENDOR_LOOT -> loot
      LootFilterType.DAILY_ROTATION_LOOT -> loot
    }
  }

  fun saveLoot(name: String) {
    viewModelScope.launch {
      lootRepository.createLootEntry(name)
    }
  }
}