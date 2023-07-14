package com.lootspy

import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lootspy.screens.login.AppAuthConfigProvider
import com.lootspy.screens.login.AppAuthConfigProvider.Companion.OAUTH_CLIENT_ID
import com.lootspy.ui.theme.LootSpyTheme
import com.lootspy.util.AlertDialog
import com.lootspy.util.LootSpyNavBar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    var manifestDialogVisible by remember { mutableStateOf(false) }

    val authService = AuthorizationService(this)
    val context = LocalContext.current
    val launcherForResult = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
      viewModel.handleAuthResponse(result, authService, context)
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
          Log.d("LootSpyMain", "Launching oauth flow")
          launcherForResult.launch(
            authService.getAuthorizationRequestIntent(
              AuthorizationRequest.Builder(
                AppAuthConfigProvider.SERVICE_CONFIG,
                OAUTH_CLIENT_ID.toString(),
                ResponseTypeValues.CODE,
                Uri.parse("dummy.lootspy.app://oauth")
              ).build()
            )
          )
          viewModel.beginGetToken()
        }
      } else if (uiState.pendingToken) {
        LootSpyTokenPlaceholder()
      } else if (uiState.allMemberships.isNotEmpty() && uiState.activeMembership == 0L) {
        LootSpyProfilePrompt(profiles = uiState.allMemberships) {
          scope.launch { viewModel.saveActiveMembership(it) }
        }
      } else if (uiState.databaseName.isEmpty()) {
        manifestDialogVisible = true
      } else {
        Scaffold(bottomBar = {
          LootSpyNavBar(
            selectedRoute = selectedRoute,
            navController = navController,
            navigationActions = remember(navController) {
              LootSpyNavigationActions(navController)
            }
          )
        }) { paddingValues ->
          AlertDialog(
            titleText = "Manifest out of date",
            messageText = stringResource(id = R.string.manifest_outdated_desc),
            ackText = "Cancel",
            confirmText = "OK",
            onDismiss = { /*TODO*/ },
            onConfirm = { /*TODO*/ },
          )
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