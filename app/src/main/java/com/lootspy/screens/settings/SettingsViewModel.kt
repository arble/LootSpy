package com.lootspy.screens.settings

import androidx.lifecycle.ViewModel
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val userStore: UserStore,
  private val profileRepository: ProfileRepository
) : ViewModel() {
}