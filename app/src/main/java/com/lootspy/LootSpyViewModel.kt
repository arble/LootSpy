package com.lootspy

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lootspy.api.GetMembershipsTask
import com.lootspy.data.ProfileRepository
import com.lootspy.data.UserStore
import com.lootspy.data.source.DestinyProfile
import com.lootspy.util.WorkBuilders
import com.lootspy.util.combine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.TokenResponse
import javax.inject.Inject

data class MainUiState(
  val pendingToken: Boolean = false,
  val accessToken: String = "",
  val membershipId: String = "",
  val allMemberships: List<DestinyProfile> = listOf(),
  val activeMembership: Long = 0,
  val databaseName: String = "",
  val fetchingManifest: Boolean = false,
  val userMessage: Int? = null,
) {
  fun isLoggedOut() = accessToken.isEmpty() && membershipId.isEmpty()
}

@HiltViewModel
class LootSpyViewModel @Inject constructor(
  private val userStore: UserStore,
  profileRepository: ProfileRepository
) : ViewModel() {
  private val _isDoingToken = MutableStateFlow(false)
  private val _isFetchingManifest = MutableStateFlow(false)

  val uiState: StateFlow<MainUiState> =
    combine(
      _isDoingToken,
      _isFetchingManifest,
      userStore.authState,
      userStore.bungieMembershipId,
      profileRepository.getProfilesStream(),
      userStore.activeMembership,
      userStore.lastManifestDb,
    ) { isDoingToken, isFetchingManifest, authState, bungieMembershipId, allMemberships, activeMembership, databaseName ->
      if (isDoingToken) {
        MainUiState(pendingToken = true)
      } else {
        MainUiState(
          accessToken = authState.accessToken ?: "",
          membershipId = bungieMembershipId,
          allMemberships = allMemberships,
          activeMembership = activeMembership,
          databaseName = databaseName,
          // use population of the database name to signal the end of the fetch process to the UI
          fetchingManifest = if (databaseName.isNotEmpty()) false else isFetchingManifest
        )
      }
    }.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = MainUiState()
    )

  fun beginGetToken() {
    _isDoingToken.value = true
  }

  fun setFetchingManifest(fetching: Boolean) {
    _isFetchingManifest.update { fetching }
  }

  suspend fun saveActiveMembership(profile: DestinyProfile) {
    userStore.saveActiveMembership(profile.membershipId)
  }

  fun handleAuthResponse(
    result: ActivityResult,
    authService: AuthorizationService,
    context: Context
  ) {
    Log.d(LOG_TAG, "Got auth response")
    if (result.resultCode == Activity.RESULT_OK) {
      val intent = result.data ?: return
      val authResponse = AuthorizationResponse.fromIntent(intent)
      val authException = AuthorizationException.fromIntent(intent)
      viewModelScope.launch outer@ {
        val authState = userStore.authState.first()
        authState.update(authResponse, authException)
        userStore.saveAuthState(authState)
        if (authResponse?.authorizationCode != null) {
          authService.performTokenRequest(
            authResponse.createTokenExchangeRequest(),
            ClientSecretBasic("AWmnuaM1JS6V2lFlGLn5jLwX8KbY65c-7jIK7VWFZOw")
          ) { tokenResponse: TokenResponse?, tokenException: AuthorizationException? ->
            Log.d(LOG_TAG, "Got token response: ${tokenResponse?.jsonSerialize()}")
            authState.update(tokenResponse, tokenException)
            val token = authState.accessToken
            if (token == null) {
              Log.d(LOG_TAG, "Token response did not contain access token!")
              return@performTokenRequest
            }
            val bungieMembershipId = tokenResponse?.additionalParameters?.get("membership_id")
              ?: return@performTokenRequest
            viewModelScope.launch {
              userStore.saveAuthState(authState)
              userStore.saveBungieMembership(bungieMembershipId)
              Log.d(LOG_TAG, "Got data: $token")
              Log.d(LOG_TAG, "Full data: ${authState.jsonSerialize()}")
              WorkBuilders.dispatchUniqueWorker(
                context,
                GetMembershipsTask::class.java,
                "sync_memberships",
                mapOf("notify_channel" to "lootspyApi", "access_token" to token)
              )
            }
          }
        }
      }
    }
  }

  companion object {
    private const val LOG_TAG = "LootSpy Auth"
  }
}