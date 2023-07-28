package com.lootspy.screens.filter

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lootspy.R
import com.lootspy.filter.Filter
import com.lootspy.util.ScreenContentWithEmptyText

@Composable
fun FilterScreen(
  onAddFilter: () -> Unit,
  onClickFilter: (Filter) -> Unit,
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: FilterViewModel = hiltViewModel(),
) {
  val context = LocalContext.current

  BackHandler(onBack = onBack)
  Scaffold(
    topBar = {
      FilterTopAppBar(
        onDeleteAll = viewModel::deleteAll
      )
    },
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(onClick = onAddFilter) {
        Icon(Icons.Filled.Add, "Add New")
      }
    },
  ) { paddingValues ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    if (uiState.userMessage != null) {
      Toast.makeText(context, stringResource(id = uiState.userMessage!!), Toast.LENGTH_SHORT).show()
    }

    ScreenContentWithEmptyText(
      loading = uiState.isLoading,
      items = uiState.items,
      itemContent = { _, filter ->
        FilterItem(filter = filter, onClickFilter = onClickFilter)
      },
      emptyText = stringResource(id = R.string.filter_screen_empty),
      modifier = Modifier.padding(paddingValues)
    )
  }
}

@Composable
private fun FilterItem(
  filter: Filter,
  onClickFilter: (Filter) -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = dimensionResource(id = R.dimen.horizontal_margin),
          vertical = dimensionResource(id = R.dimen.loot_item_padding),
        )
        .clickable { onClickFilter(filter) }
    ) {
      Text(
        text = filter.name,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(
          start = dimensionResource(
            id = R.dimen.horizontal_margin
          )
        ),
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTopAppBar(
  onDeleteAll: () -> Unit,
) {
  TopAppBar(
    title = { Text(text = stringResource(id = R.string.filter_title)) },
    actions = {
      IconButton(onClick = onDeleteAll) {
        Icon(Icons.Default.Delete, null)
      }
    }
  )
}
