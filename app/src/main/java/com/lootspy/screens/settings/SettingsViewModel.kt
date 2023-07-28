package com.lootspy.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lootspy.manifest.AutocompleteHelper
import com.lootspy.manifest.BasicItem
import com.lootspy.manifest.ManifestManager
import com.lootspy.data.repo.ProfileRepository
import com.lootspy.data.UserStore
import com.lootspy.data.source.DestinyProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class SettingsUiState(
  val selectedMembership: DestinyProfile? = null,
  val allProfiles: List<DestinyProfile> = listOf()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val userStore: UserStore,
  private val profileRepository: ProfileRepository,
  private val manifestManager: ManifestManager,
  private val autocompleteHelper: AutocompleteHelper,
) : ViewModel() {
  val uiState: StateFlow<SettingsUiState> =
    combine(
      userStore.activeMembership,
      profileRepository.getProfilesStream()
    ) { membership, profiles ->
      SettingsUiState(
        selectedMembership = profiles.filter { it.membershipId == membership }
          .getOrNull(0),
        allProfiles = profiles
      )
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = SettingsUiState()
    )

  private val _suggestions = MutableStateFlow(listOf<BasicItem>())
  val suggestions = _suggestions.asStateFlow()

  suspend fun saveActiveMembership(profile: DestinyProfile) {
    userStore.saveActiveMembership(profile.membershipId)
  }

  suspend fun clearDb(): Boolean {
    return withContext(Dispatchers.IO) {
      val result = manifestManager.deleteManifestDb()
      if (result) {
        userStore.saveLastManifest("")
        userStore.saveLastManifestDb("")
      }
      return@withContext result
    }
  }

  fun getSuggestions(text: String, limit: Int = 3) {
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

  suspend fun dropAutocompleteTable() =
    withContext(Dispatchers.IO) { manifestManager.dropAutocompleteTable() }
}