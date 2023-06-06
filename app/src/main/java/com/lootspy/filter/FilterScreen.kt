package com.lootspy.filter

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lootspy.R
import com.lootspy.data.Filter
import com.lootspy.util.FilterTopAppBar
import com.lootspy.util.ScreenContentWithEmptyText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
  onAddFilter: () -> Unit,
  onClickFilter: (Filter) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: FilterViewModel = hiltViewModel(),
) {
  val context = LocalContext.current

  Scaffold(
    topBar = {
      FilterTopAppBar(
        addFilter = {},
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
    val onFilterClick: (Filter) -> Unit = {
      Toast.makeText(context, "Filter is: $it", Toast.LENGTH_SHORT).show()
    }

    ScreenContentWithEmptyText(
      loading = uiState.isLoading,
      items = uiState.items,
      itemContent = {
        FilterItem(filter = it, onFilterClick = onFilterClick)
      },
      emptyText = stringResource(id = R.string.filter_screen_empty),
      modifier = Modifier.padding(paddingValues)
    )
  }
}

@Composable
private fun FilterItem(
  filter: Filter,
  onFilterClick: (Filter) -> Unit,
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
      .padding(
        horizontal = dimensionResource(id = R.dimen.horizontal_margin),
        vertical = dimensionResource(id = R.dimen.loot_item_padding),
      )
      .clickable { onFilterClick(filter) }
  ) {
    Text(
      text = filter.name ?: filter.id,
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.padding(
        start = dimensionResource(
          id = R.dimen.horizontal_margin
        )
      ),
    )
  }
}
