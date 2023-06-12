package com.lootspy.screens.vendor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.lootspy.data.Vendor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class VendorUiState(
  val vendors: List<Vendor> = emptyList(),
  val isLoading: Boolean = false,
  val userMessage: Int? = null,
)

@HiltViewModel
class VendorViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
): ViewModel() {
  private val _uiState = MutableStateFlow(VendorUiState())
  val uiState = _uiState.asStateFlow()

  init {
    _uiState.update { it.copy(isLoading = true) }
  }
}