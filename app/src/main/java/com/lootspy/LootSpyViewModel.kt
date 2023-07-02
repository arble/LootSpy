package com.lootspy

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.lootspy.R
import com.lootspy.util.Async
import com.lootspy.util.UserStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenResponse
import javax.inject.Inject

data class MainUiState(
  val pendingAuth: Boolean = false,
  val pendingToken: Boolean = false,
  val accessToken: String? = null,
  val membershipId: String? = null,
  val userMessage: Int? = null,
) {
  fun isLoggedOut() = !pendingAuth && accessToken == null && membershipId == null
}

@HiltViewModel
class LootSpyViewModel @Inject constructor(
  private val savedStateHandle: SavedStateHandle,
  private val userStore: UserStore,
) : ViewModel() {
  //  private val accessToken: String? = savedStateHandle["access_token"]
//  private val membershipId: String? = savedStateHandle["membership_id"]
  private val _isDoingAuth = MutableStateFlow(false)
  private val _isDoingToken = MutableStateFlow(false)
  private val _tokenAsync = userStore.tokenFlow.map {
    Async.Success(it)
  }.catch<Async<String>> {
    Log.e("LootSpy", "tokenGet", it)
    emit(Async.Error(R.string.loading_token_error))
  }
  private val _membershipAsync = userStore.membershipFlow.map {
    Async.Success(it)
  }.catch<Async<String>> {
    Log.e("LootSpy", "membershipGet", it)
    emit(Async.Error(R.string.loading_membership_error))
  }

  val uiState: StateFlow<MainUiState> =
    combine(
      _isDoingToken,
      _isDoingAuth,
      _tokenAsync,
      _membershipAsync
    ) { isDoingToken, isDoingAuth, tokenAsync, membershipAsync ->
      if (isDoingToken) {
        return@combine MainUiState(pendingToken = true)
      } else if (isDoingAuth) {
        return@combine MainUiState(pendingAuth = true)
      }
      when (tokenAsync) {
        is Async.Loading -> {
          MainUiState(pendingAuth = true)
        }

        is Async.Error -> {
          MainUiState(userMessage = tokenAsync.errorMessage)
        }

        is Async.Success -> {
          // This pair of values is written atomically to DataStore
          val assertedMembershipAsync = membershipAsync as Async.Success
          MainUiState(
            pendingAuth = false,
            accessToken = tokenAsync.data,
            membershipId = assertedMembershipAsync.data
          )
        }
      }
    }.stateIn(
      viewModelScope, started = SharingStarted.WhileSubscribed(5000),
      initialValue = MainUiState()
    )

  fun beginPendingAuth(pendingAuth: Boolean) {
    _isDoingAuth.value = true
  }

  fun saveAuthInfo(accessToken: String, membershipId: String) {

  }

  fun handleAuthResponse(result: ActivityResult, authService: AuthorizationService) {
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
              }
            }
          }
        }
      }
    }
  }
}