package com.lootspy.screens.login

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lootspy.LootSpyActivity
import com.lootspy.util.UserStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

class LootSpyLoginActivity : ComponentActivity() {
  companion object {
    private const val OAUTH_CLIENT_ID = 44724
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val userStore = UserStore(applicationContext)
    val token = runBlocking { userStore.tokenFlow.first() }

    if (token.isEmpty()) {
      setContent {
        LootSpyLoginPrompt {

        }
      }
    } else {
      val mainAppIntent = Intent(this, LootSpyActivity::class.java)
      mainAppIntent.putExtra("ACCESS_TOKEN", token)
      startActivity(mainAppIntent)
      finish()
    }
  }

  fun doAuth() {
    val authRequest = AuthorizationRequest.Builder(
      AppAuthConfigProvider.SERVICE_CONFIG,
      OAUTH_CLIENT_ID.toString(),
      ResponseTypeValues.CODE,
      Uri.parse("https://api.lootspy.app/oauth")
    ).build()
    val authService = AuthorizationService(this)
    authService.performAuthorizationRequest(
      authRequest,
      PendingIntent.getActivity(
        this,
        0,
        Intent(this, LootSpyTokenActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
      ),
      PendingIntent.getActivity(
        this,
        0,
        Intent(this, LootSpyLoginActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE
      )
    )
  }
}