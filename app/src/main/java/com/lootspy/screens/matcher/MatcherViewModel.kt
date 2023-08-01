package com.lootspy.screens.matcher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lootspy.LootSpyDestinationArgs
import com.lootspy.types.matcher.FilterMatcher
import com.lootspy.types.matcher.MatcherType
import javax.inject.Inject

data class MatcherUiState(
  val selectedMatcherType: MatcherType = MatcherType.NAME,

  )

class MatcherViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
) : ViewModel() {

  private val filterId: String? = savedStateHandle[LootSpyDestinationArgs.FILTER_ID_ARG]
  private val matcher: FilterMatcher? = savedStateHandle[LootSpyDestinationArgs.MATCHER_ARG]

}