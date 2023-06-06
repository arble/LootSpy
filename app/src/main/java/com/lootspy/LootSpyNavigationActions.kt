package com.lootspy

import androidx.navigation.NavHostController
import com.lootspy.LootSpyDestinationArgs.FILTER_ID_ARG
import com.lootspy.LootSpyScreens.ADD_EDIT_FILTER_SCREEN
import com.lootspy.LootSpyScreens.FILTERS_SCREEN
import com.lootspy.LootSpyScreens.LOOT_SCREEN
import com.lootspy.LootSpyScreens.SETTINGS_SCREEN
import com.lootspy.LootSpyScreens.VENDORS_SCREEN
import com.lootspy.LootSpyScreens.VIEW_FILTER_SCREEN

private object LootSpyScreens {
  const val LOOT_SCREEN = "loot"
  const val FILTERS_SCREEN = "filters"
  const val VENDORS_SCREEN = "vendors"
  const val SETTINGS_SCREEN = "settings"
  const val VIEW_FILTER_SCREEN = "viewFilter"
  const val ADD_EDIT_FILTER_SCREEN = "addEditFilter"
}

object LootSpyDestinationArgs {
  const val FILTER_ID_ARG = "filterId"
  const val VENDOR_ARG = "vendor"
  const val USER_MESSAGE_ARG = "userMessage"
}

object LootSpyDestinations {
  const val LOOT_ROUTE = LOOT_SCREEN
  const val FILTERS_ROUTE = FILTERS_SCREEN
  const val VENDORS_ROUTE = VENDORS_SCREEN
  const val SETTINGS_ROUTE = SETTINGS_SCREEN
  const val VIEW_FILTER_ROUTE = "$VIEW_FILTER_SCREEN/$FILTER_ID_ARG"
  const val ADD_EDIT_FILTER_ROUTE = "$ADD_EDIT_FILTER_SCREEN/?$FILTER_ID_ARG={$FILTER_ID_ARG}"
}

class LootSpyNavigationActions(private val navController: NavHostController) {

  val routeOrderingMap = hashMapOf<String, Int>(
    LootSpyDestinations.LOOT_ROUTE to 0,
    LootSpyDestinations.FILTERS_ROUTE to 1,
    LootSpyDestinations.VENDORS_ROUTE to 2,
    LootSpyDestinations.SETTINGS_ROUTE to 3,
  )

  fun navigateToLoot() = navController.navigate(LOOT_SCREEN)

  fun navigateToVendors() = navController.navigate(VENDORS_SCREEN)

  fun navigateToFilters() = navController.navigate(FILTERS_SCREEN)

  fun navigateToSettings() = navController.navigate(SETTINGS_SCREEN)

  fun navigateToAddEditFilter(filterId: String?) =
    navController.navigate("$ADD_EDIT_FILTER_SCREEN/${filterId ?: ""}")
}