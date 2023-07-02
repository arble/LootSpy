package com.lootspy.screens.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.lootspy.LootSpyActivity
import com.lootspy.util.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.TokenResponse
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LootSpyTokenActivity : ComponentActivity() {
  private val executor: ExecutorService = Executors.newSingleThreadExecutor()
  private lateinit var authService: AuthorizationService
  private lateinit var userStore: UserStore

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    authService = AuthorizationService(this)
    userStore = UserStore(this)
  }

  override fun onStart() {
    super.onStart()
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
            lifecycleScope.launch(Dispatchers.IO) {
              userStore.saveToken(accessToken)
              userStore.saveMembershipId(membershipId)
            }
            val mainAppIntent = Intent(this, LootSpyActivity::class.java)
            mainAppIntent.putExtra("ACCESS_TOKEN", accessToken)
            startActivity(mainAppIntent)
            finish()
          }
        }
      }
    }
  }
}