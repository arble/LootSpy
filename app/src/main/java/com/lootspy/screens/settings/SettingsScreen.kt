package com.lootspy.screens.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.lootspy.R
import com.lootspy.api.GetCharactersTask
import com.lootspy.api.GetManifestTask
import com.lootspy.data.source.DestinyProfile
import com.lootspy.util.BungiePathHelper
import com.lootspy.util.WorkBuilders
import com.lootspy.util.popup.LootSpyPopup
import com.lootspy.util.popup.PopupState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  modifier: Modifier = Modifier,
  viewModel: SettingsViewModel = hiltViewModel()
) {
  val uiState = viewModel.uiState.collectAsState()
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        modifier = Modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.smallTopAppBarColors(),
        actions = {
          IconButton(onClick = {
            WorkBuilders.dispatchUniqueWorker(
              context,
              GetManifestTask::class.java,
              "sync_manifest",
              mapOf("notify_channel" to "lootspyApi")
            )
          }) {
            Icon(Icons.Default.Search, null)
          }

          IconButton(onClick = {
            scope.launch {
              if (viewModel.clearDb()) {
                Toast.makeText(context, "Cleared DB", Toast.LENGTH_SHORT).show()
              }
            }
//            WorkBuilders.dispatchUniqueWorker(
//              context,
//              GetCharactersTask::class.java,
//              "sync_chars",
//              mapOf("notify_channel" to "lootspyApi")
//            )
          }) {
            Icon(Icons.Default.MoreVert, null)
          }

        }
      )
    },
    modifier = Modifier.fillMaxSize()
  ) {
    Column(modifier = modifier.padding(it), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      val selectedMembership = uiState.value.selectedMembership
      if (selectedMembership != null) {
        SettingsCard(modifier = modifier.height(64.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
              .fillMaxWidth()
              .fillMaxHeight()
          ) {
            Text(
              text = stringResource(id = R.string.settings_active_profile),
              style = MaterialTheme.typography.headlineSmall,
              modifier = modifier.fillMaxWidth(0.5f)
            )
            val errorPainter =
              painterResource(id = com.google.android.material.R.drawable.mtrl_ic_cancel)
            val placeholderPainter = painterResource(id = R.drawable.ic_launcher_foreground)
            var expanded by remember { mutableStateOf(false) }
            val popupState = remember { PopupState(false) }
            AsyncImage(
              model = BungiePathHelper.getFullUrlForPath(uiState.value.selectedMembership?.iconPath),
              placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
              error = errorPainter,
              fallback = errorPainter,
              contentDescription = null,
              modifier = modifier
                .fillMaxHeight(0.5f)
                .fillMaxWidth(0.25f)
            )
            Text(
              text = selectedMembership.platformDisplayName,
              modifier = modifier.weight(1f)
            )
            IconButton(
              onClick = { popupState.isVisible = true },
              modifier = modifier.width(32.dp)
            ) {
              Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
            LootSpyPopup(
              popupState = popupState,
              onDismissRequest = { popupState.isVisible = false },
              modifier = modifier.fillMaxWidth()
            ) {
              uiState.value.allProfiles.forEach { profile ->
                ProfileSelectorEntry(
                  iconPath = BungiePathHelper.getFullUrlForPath(profile.iconPath),
                  displayName = profile.platformDisplayName,
                  placeholderPainter = placeholderPainter,
                  errorPainter = errorPainter,
                  onClickProfile = {
                    popupState.isVisible = false
                    scope.launch { viewModel.saveActiveMembership(profile) }
                  }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ProfileSelectorEntry(
  iconPath: String?,
  displayName: String,
  placeholderPainter: Painter,
  errorPainter: Painter,
  onClickProfile: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .height(32.dp)
      .clickable(onClick = onClickProfile)
  ) {
    Box(modifier = modifier.fillMaxWidth(0.5f))
    AsyncImage(
      model = iconPath,
      placeholder = placeholderPainter,
      error = errorPainter,
      fallback = errorPainter,
      contentDescription = null,
      modifier = modifier
        .fillMaxHeight(0.5f)
        .fillMaxWidth(0.25f)
    )
    Text(
      text = displayName,
      modifier = modifier.weight(1f)
    )
  }
}

@Composable
private fun SettingsCard(
  modifier: Modifier = Modifier,
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    content = content,
    modifier = modifier,
  )
}