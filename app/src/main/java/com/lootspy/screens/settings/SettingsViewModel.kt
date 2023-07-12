package com.lootspy.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
import com.lootspy.data.source.DestinyProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingsUiState(
  val selectedMembership: DestinyProfile? = null,
  val allProfiles: List<DestinyProfile> = listOf()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val userStore: UserStore,
  private val profileRepository: ProfileRepository
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

  suspend fun saveActiveMembership(profile: DestinyProfile) {
    userStore.saveActiveMembership(profile.membershipId)
  }
}