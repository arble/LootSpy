package com.lootspy.screens.matcher

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lootspy.LootSpyDestinationArgs
import com.lootspy.filter.matcher.FilterMatcher
import com.lootspy.filter.matcher.MatcherType
import javax.inject.Inject

data class MatcherUiState(
  val selectedMatcherType: com.lootspy.filter.matcher.MatcherType = com.lootspy.filter.matcher.MatcherType.NAME,

  )

class MatcherViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
): ViewModel() {

  private val filterId: String? = savedStateHandle[LootSpyDestinationArgs.FILTER_ID_ARG]
  private val matcher: com.lootspy.filter.matcher.FilterMatcher? = savedStateHandle[LootSpyDestinationArgs.MATCHER_ARG]



}