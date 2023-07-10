package com.lootspy

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lootspy.api.GetProfilesTask
import com.lootspy.data.UserStore
import com.lootspy.util.WorkBuilders
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenResponse
import javax.inject.Inject

data class MainUiState(
  val pendingToken: Boolean = false,
  val accessToken: String = "",
  val membershipId: String = "",
  val userMessage: Int? = null,
) {
  fun isLoggedOut() = accessToken.isEmpty() && membershipId.isEmpty()
}

@HiltViewModel
class LootSpyViewModel @Inject constructor(
  private val userStore: UserStore,
) : ViewModel() {
  private val _isDoingToken = MutableStateFlow(false)

  val uiState: StateFlow<MainUiState> =
    combine(
      _isDoingToken,
      userStore.accessToken,
      userStore.membershipId
    ) { isDoingToken, token, membershipId ->
      if (isDoingToken) {
        MainUiState(pendingToken = true)
      } else {
        MainUiState(accessToken = token, membershipId = membershipId)
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = MainUiState()
    )

  fun beginGetToken() {
    _isDoingToken.value = true
  }

  fun handleAuthResponse(
    result: ActivityResult,
    authService: AuthorizationService,
    context: Context
  ) {
    Log.d("LootSpyAuth", "Got auth response")
    if (result.resultCode == Activity.RESULT_OK) {
      val intent = result.data ?: return
      val response = AuthorizationResponse.fromIntent(intent)
      val exception = AuthorizationException.fromIntent(intent)
      if (response?.authorizationCode != null) {
        authService.performTokenRequest(
          response.createTokenExchangeRequest()
        ) { tokenResponse: TokenResponse?, authorizationException: AuthorizationException? ->
          if (tokenResponse != null) {
            val accessToken = tokenResponse.accessToken
            val membershipId = tokenResponse.additionalParameters["membership_id"]
            if (accessToken != null && membershipId != null) {
              Log.d("LootSpyAuth", "Got data: $accessToken and $membershipId")
              viewModelScope.launch {
                userStore.saveAuthInfo(accessToken, membershipId)
                WorkBuilders.dispatchUniqueWorker(
                  context,
                  GetProfilesTask::class.java,
                  "sync_loot",
                  mapOf("notify_channel" to "lootspyApi")
                )
              }
            }
          }
        }
      }
    }
  }
}