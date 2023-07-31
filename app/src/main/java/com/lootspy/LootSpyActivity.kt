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
import com.lootspy.api.workers.GetManifestWorker
import com.lootspy.api.workers.AddShortcutTablesWorker
import com.lootspy.api.workers.UnzipManifestWorker
import com.lootspy.screens.login.AppAuthProvider
import com.lootspy.screens.login.AppAuthProvider.Companion.OAUTH_CLIENT_ID
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
  private val authService by lazy { AuthorizationService(this) }

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

  @OptIn(ExperimentalAnimationApi::class)
  @Composable
  private fun MainActivityContent(
    viewModel: LootSpyViewModel = hiltViewModel()
  ) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

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
                AppAuthProvider.SERVICE_CONFIG,
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
          ManifestDialogs(context, uiState, workFlow, viewModel)
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
    val activeManifestJob = syncManifestState.value?.find { it.state == WorkInfo.State.RUNNING }
    // use the enqueued job to keep the progress popup from flickering
    val pendingManifestJob = syncManifestState.value?.find { it.state == WorkInfo.State.ENQUEUED }
    if (uiState.databaseName.isEmpty() && activeManifestJob == null && pendingManifestJob == null) {
      TextAlertDialog(
        titleText = stringResource(id = R.string.manifest_outdated_title),
        messageText = stringResource(id = R.string.manifest_outdated_desc),
        ackText = "Not now",
        confirmText = "OK",
        modal = true,
        onDismiss = { finish() },
        onConfirm = {
          WorkBuilders.dispatchUniqueWorkWithTokens(
            context = context,
            workName = "sync_manifest",
            workData = mapOf("notify_channel" to "lootspyApi"),
            jobs = listOf(
              GetManifestWorker::class.java,
              UnzipManifestWorker::class.java,
              AddShortcutTablesWorker::class.java
            ),
            tags = listOf("sync_manifest")
          )
          viewModel.setFetchingManifest(true)
        },
      )
    } else if (activeManifestJob != null || pendingManifestJob != null) {
      val dataMap = activeManifestJob?.progress?.keyValueMap
      val progressInfo = if (dataMap.isNullOrEmpty()) {
        Pair("Working", 0f)
      } else {
        val stage = dataMap.keys.toList().first() as String
        val progress = dataMap.values.toList().first() as Int
        Pair(stage, progress.toFloat() / 10f)
      }
      ProgressAlertDialog(
        titleText = stringResource(id = R.string.manifest_updating_title),
        infoText = progressInfo.first,
        progress = progressInfo.second,
      )
    }
  }
}