package com.lootspy.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lootspy.LootSpyDestinations
import com.lootspy.LootSpyNavigationActions
import com.lootspy.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun LootSpyNavBar(
  selectedRoute: MutableState<String>,
  navController: NavHostController,
  navigationActions: LootSpyNavigationActions,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  NavigationBar {
    NavigationBarItem(
      icon = { Icon(Icons.Default.Home, null) },
      label = { Text(stringResource(id = R.string.nav_bar_loot)) },
      selected = selectedRoute.value == LootSpyDestinations.LOOT_ROUTE,
      onClick = {
        if (navBackStackEntry?.destination?.route == LootSpyDestinations.LOOT_ROUTE) {
          return@NavigationBarItem
        }
        coroutineScope.launch {
          navigationActions.navigateToLoot()
          selectedRoute.value = LootSpyDestinations.LOOT_ROUTE
        }
      },
    )
    NavigationBarItem(
      icon = { Icon(Icons.Default.Favorite, null) },
      label = { Text(stringResource(id = R.string.nav_bar_filters)) },
      selected = selectedRoute.value == LootSpyDestinations.FILTERS_ROUTE,
      onClick = {
        if (navBackStackEntry?.destination?.route == LootSpyDestinations.FILTERS_ROUTE) {
          return@NavigationBarItem
        }
        coroutineScope.launch {
          navigationActions.navigateToFilters()
          selectedRoute.value = LootSpyDestinations.FILTERS_ROUTE
        }
      },
    )
    NavigationBarItem(
      icon = { Icon(Icons.Default.Star, null) },
      label = { Text(stringResource(id = R.string.nav_bar_vendors)) },
      selected = selectedRoute.value == LootSpyDestinations.VENDORS_ROUTE,
      onClick = {
        if (navBackStackEntry?.destination?.route == LootSpyDestinations.VENDORS_ROUTE) {
          return@NavigationBarItem
        }
        coroutineScope.launch {
          navigationActions.navigateToVendors()
          selectedRoute.value = LootSpyDestinations.VENDORS_ROUTE
        }
      },
    )
    NavigationBarItem(
      icon = { Icon(Icons.Default.Settings, null) },
      label = { Text(text = stringResource(id = R.string.nav_bar_settings)) },
      selected = selectedRoute.value == LootSpyDestinations.SETTINGS_ROUTE,
      onClick = {
        if (navBackStackEntry?.destination?.route == LootSpyDestinations.SETTINGS_ROUTE) {
          return@NavigationBarItem
        }
        coroutineScope.launch {
          navigationActions.navigateToSettings()
          selectedRoute.value = LootSpyDestinations.SETTINGS_ROUTE
        }
      })
  }
}