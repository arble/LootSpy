package com.lootspy

import android.content.Context
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
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lootspy.api.GetManifestTask
import com.lootspy.api.UnzipManifestTask
import com.lootspy.screens.login.AppAuthConfigProvider
import com.lootspy.screens.login.AppAuthConfigProvider.Companion.OAUTH_CLIENT_ID
import com.lootspy.ui.theme.LootSpyTheme
import com.lootspy.util.LootSpyNavBar
import com.lootspy.util.ProgressAlertDialog
import com.lootspy.util.TextAlertDialog
import com.lootspy.util.WorkBuilders
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues

@AndroidEntryPoint
class LootSpyActivity : ComponentActivity() {
  private lateinit var authService: AuthorizationService

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      MainActivityContent()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    authService.dispose()
  }

  @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
  @Composable
  private fun MainActivityContent(
    viewModel: LootSpyViewModel = hiltViewModel()
  ) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    authService = AuthorizationService(this)
    val context = LocalContext.current
    val workFlow = WorkManager.getInstance(context).getWorkInfosByTagFlow("sync_manifest")
      .collectAsStateWithLifecycle(
        initialValue = null
      )
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
          if (uiState.databaseName.isEmpty()) {
            ManifestDialogs(context, uiState, workFlow, viewModel)
          }
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

  @Composable
  private fun ManifestDialogs(
    context: Context,
    uiState: MainUiState,
    syncManifestState: State<List<WorkInfo>?>,
    viewModel: LootSpyViewModel,
  ) {
    if (uiState.databaseName.isEmpty()) {
      if (uiState.fetchingManifest) {
        val workInfo = syncManifestState.value?.find { it.state == WorkInfo.State.RUNNING }
        val (stage, progress) = if (workInfo != null) {
          val dataMap = workInfo.progress.keyValueMap
          if (dataMap.isEmpty()) {
            Pair("Setting up", 0f)
          } else {
            val stage = dataMap.keys.toList().first() as String
            val progress = dataMap.values.toList().first() as Int
            Pair(stage, progress.toFloat() / 10f)
          }
        } else {
          Pair("Setting up", 0f)
        }
        ProgressAlertDialog(
          titleText = "Getting Manifest",
          infoText = stage,
          progress = progress,
        )
      } else {
        TextAlertDialog(
          titleText = "Manifest out of date",
          messageText = stringResource(id = R.string.manifest_outdated_desc),
          ackText = "Not now",
          confirmText = "OK",
          modal = true,
          onDismiss = { finish() },
          onConfirm = {
            WorkBuilders.dispatchUniqueWorkerLinearFollowers(
              context = context,
              initialWorkerClass = GetManifestTask::class.java,
              workName = "sync_manifest",
              workData = null,
              followingJobs = listOf(UnzipManifestTask::class.java),
              tags = listOf("sync_manifest")
            )
            viewModel.setFetchingManifest(true)
          },
        )
      }
    }
  }
}