package com.lootspy.util

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.lootspy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LootTopAppBar(
  openDrawer: () -> Unit,
  onChangeFilter: () -> Unit,
  onRefresh: () -> Unit,
) {
  TopAppBar(
    title = { Text(text = stringResource(id = R.string.app_name)) },
    navigationIcon = {
      IconButton(onClick = openDrawer) {
        Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
      }
    },
    modifier = Modifier.fillMaxWidth(),
    colors = TopAppBarDefaults.smallTopAppBarColors(),
    actions = {
      IconButton(onClick = onChangeFilter) {
        Icon(Icons.Default.List, null)
      }
      IconButton(onClick = onRefresh) {
        Icon(Icons.Default.Refresh, null)
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterTopAppBar(
  openDrawer: () -> Unit,
  addFilter: () -> Unit,
) {
  TopAppBar(
    title = { Text(text = stringResource(id = R.string.filter_title)) },
    navigationIcon = {
      IconButton(onClick = openDrawer) {
        Icon(Icons.Filled.Menu, stringResource(id = R.string.open_drawer))
      }
    }
  )
}

