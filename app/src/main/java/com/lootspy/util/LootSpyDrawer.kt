package com.lootspy.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lootspy.LootSpyDestinations
import com.lootspy.LootSpyNavigationActions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppModalDrawer(
  drawerState: DrawerState,
  currentRoute: String,
  navigationActions: LootSpyNavigationActions,
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  content: @Composable () -> Unit
) {
  val selectedRoute = remember { mutableStateOf(currentRoute) }
  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.6f)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { coroutineScope.launch { navigationActions.navigateToLoot() } },
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.Favorite, contentDescription = null)
          Spacer(modifier = Modifier.width(16.dp))
          Text(text = "LootSpy")
        }
        Divider(modifier = Modifier.padding(top = 6.dp, bottom = 6.dp))
        NavigationDrawerItem(
          icon = { Icon(Icons.Default.Home, null) },
          label = { Text("Loot For You") },
          selected = selectedRoute.value == LootSpyDestinations.LOOT_ROUTE,
          onClick = {
            coroutineScope.launch {
              drawerState.close()
              navigationActions.navigateToLoot()
              selectedRoute.value = LootSpyDestinations.LOOT_ROUTE
            }
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
          icon = { Icon(Icons.Default.Add, null) },
          label = { Text("Filters") },
          selected = selectedRoute.value == LootSpyDestinations.FILTERS_ROUTE,
          onClick = {
            coroutineScope.launch {
              drawerState.close()
              navigationActions.navigateToFilters()
              selectedRoute.value = LootSpyDestinations.FILTERS_ROUTE
            }
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
        NavigationDrawerItem(
          icon = { Icon(Icons.Default.ArrowForward, null) },
          label = { Text("Vendors") },
          selected = selectedRoute.value == LootSpyDestinations.VENDORS_ROUTE,
          onClick = {
            coroutineScope.launch {
              drawerState.close()
              navigationActions.navigateToVendors()
              selectedRoute.value = LootSpyDestinations.VENDORS_ROUTE
            }
          },
          modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
        )
      }
    }) {
    content()
  }
}