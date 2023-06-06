package com.lootspy

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import com.lootspy.ui.theme.LootSpyTheme
import dagger.hilt.android.AndroidEntryPoint
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues

@AndroidEntryPoint
class LootSpyActivity : ComponentActivity() {
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      LootSpyTheme {
        LootSpyNavGraph()
      }
    }
  }

  private fun beginAuth() {
    val serviceConfig = AuthorizationServiceConfiguration(
      Uri.parse("https://www.bungie.net/en/oauth/authorize"),
      Uri.parse("https://www.bungie.net/platform/app/oauth/token/")
    )
    val authRequestBuilder =
      AuthorizationRequest.Builder(serviceConfig, "foo", ResponseTypeValues.CODE, Uri.parse("bar"))

    val authRequest = authRequestBuilder.build()
    val authorizationService = AuthorizationService(this)
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }.launch(
      authorizationService.getAuthorizationRequestIntent(authRequest)
    )
  }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//  LootSpyTheme {
//    val context = LocalContext.current
//    Greeting("Android") {
//      Toast.makeText(context, "bar", Toast.LENGTH_SHORT).show()
//    }
//  }
//}