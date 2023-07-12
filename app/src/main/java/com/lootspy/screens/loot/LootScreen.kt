package com.lootspy.screens.loot

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.lootspy.R
import com.lootspy.api.GetMembershipsTask
import com.lootspy.data.LootEntry
import com.lootspy.util.LootTopAppBar
import com.lootspy.util.ScreenContentWithEmptyText
import com.lootspy.util.WorkBuilders

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LootScreen(
  modifier: Modifier = Modifier,
  viewModel: LootViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val syncWorkInfo = WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData("sync_loot")
    .observeAsState()
  Scaffold(
    topBar = {
      LootTopAppBar(
        isSyncing = syncWorkInfo.value?.any { it.state == WorkInfo.State.RUNNING } ?: false,
        onChangeFilter = {},
        onRefresh = {
          WorkBuilders.dispatchUniqueWorker(
            context,
            GetMembershipsTask::class.java,
            "sync_loot",
            mapOf("notify_channel" to "lootspyApi")
          )
        },
      )
    },
    modifier = modifier.fillMaxSize(),
    floatingActionButtonPosition = FabPosition.End
  ) { paddingValues ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onLootClick: (LootEntry) -> Unit = {
      Toast.makeText(context, "Loot item is: $it", Toast.LENGTH_SHORT).show()
    }

    ScreenContentWithEmptyText(
      loading = uiState.isLoading,
      items = uiState.items,
      itemContent = { _, entry ->
        LootItem(entry = entry, onLootClick = onLootClick)
      },
      emptyText = stringResource(id = R.string.loot_screen_empty),
      modifier = Modifier.padding(paddingValues)
    )
  }
}

@Composable
private fun LootItem(
  entry: LootEntry,
  onLootClick: (LootEntry) -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        horizontal = dimensionResource(id = R.dimen.horizontal_margin),
        vertical = dimensionResource(id = R.dimen.loot_item_padding),
      )
      .clickable { onLootClick(entry) }
  ) {
    Text(
      text = entry.name,
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.padding(
        start = dimensionResource(
          id = R.dimen.horizontal_margin
        )
      ),
    )
  }
}