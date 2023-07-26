package com.lootspy.screens.login

import android.net.Uri
import net.openid.appauth.AuthorizationServiceConfiguration

class AppAuthProvider {
  companion object {
    val SERVICE_CONFIG = AuthorizationServiceConfiguration(
      Uri.parse("https://www.bungie.net/en/oauth/authorize"),
      Uri.parse("https://www.bungie.net/platform/app/oauth/token/")
    )
    const val OAUTH_CLIENT_ID = 44724
  }
}