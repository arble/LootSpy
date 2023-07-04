package com.lootspy

import android.app.Activity
import android.content.Context
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.lootspy.api.SyncTask
import com.lootspy.util.UserStore
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
  val accessToken: String? = null,
  val membershipId: String? = null,
  val userMessage: Int? = null,
) {
  fun isLoggedOut() = accessToken == null && membershipId == null
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

  fun handleAuthResponse(
    result: ActivityResult,
    authService: AuthorizationService,
    context: Context
  ) {
    if (result.resultCode == Activity.RESULT_OK) {
      val intent = result.data ?: return
      val response = AuthorizationResponse.fromIntent(intent)
      val exception = AuthorizationException.fromIntent(intent)
      if (response?.authorizationCode != null) {
        _isDoingToken.value = true
        authService.performTokenRequest(
          response.createTokenExchangeRequest()
        ) { tokenResponse: TokenResponse?, authorizationException: AuthorizationException? ->
          if (tokenResponse != null) {
            val accessToken = tokenResponse.accessToken
            val membershipId = tokenResponse.additionalParameters["membership_id"]
            if (accessToken != null && membershipId != null) {
              viewModelScope.launch {
                userStore.saveAuthInfo(accessToken, membershipId)
                val workManager = WorkManager.getInstance(context)
                val data = Data.Builder()
                  .putString("access_token", accessToken)
                  .putString("membership_id", membershipId)
                  .putString("notify_channel", "lootspyApi")
                  .build()
                val syncRequest = OneTimeWorkRequest.Builder(SyncTask::class.java)
                  .setInputData(data)
                  .build()
                workManager.enqueue(syncRequest)
                workManager.getWorkInfoById(syncRequest.id)
              }
            }
          }
        }
      }
    }
  }
}