package com.lootspy

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lootspy.screens.login.AppAuthConfigProvider
import com.lootspy.screens.login.AppAuthConfigProvider.Companion.OAUTH_CLIENT_ID
import com.lootspy.ui.theme.LootSpyTheme
import com.lootspy.util.LootSpyNavBar
import dagger.hilt.android.AndroidEntryPoint
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues

@AndroidEntryPoint
class LootSpyActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MainActivityContent()
    }
  }

  @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
  @Composable
  private fun MainActivityContent(
    viewModel: LootSpyViewModel = hiltViewModel()
  ) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authRequest = AuthorizationRequest.Builder(
      AppAuthConfigProvider.SERVICE_CONFIG,
      OAUTH_CLIENT_ID.toString(),
      ResponseTypeValues.CODE,
      Uri.parse("https://api.lootspy.app/oauth")
    ).build()
    val authService = AuthorizationService(this)
    val launcherForResult = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
      viewModel.handleAuthResponse(result, authService)
    }
    LootSpyTheme {
//        LootSpyNavGraph()
      val navController = rememberAnimatedNavController()
      val navActions = remember(navController) {
        LootSpyNavigationActions(navController)
      }
      val selectedRoute = remember { mutableStateOf(LootSpyDestinations.LOOT_ROUTE) }
      if (uiState.isLoggedOut()) {
        LootSpyLoginPrompt {
          launcherForResult.launch(authService.getAuthorizationRequestIntent(authRequest))
        }
        return@LootSpyTheme
      }
      Scaffold(bottomBar = {
        LootSpyNavBar(
          selectedRoute = selectedRoute,
          navController = navController,
          navigationActions = remember(navController) {
            LootSpyNavigationActions(navController)
          }
        )
      }) { paddingValues ->
        LootSpyNavGraph(
          navController = navController,
          modifier = Modifier.padding(paddingValues),
          navActions = navActions,
          selectedRoute = selectedRoute,
        )
      }
    }
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