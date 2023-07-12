package com.lootspy

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.lootspy.LootSpyDestinationArgs.FILTER_ID_ARG
import com.lootspy.LootSpyDestinationArgs.USER_MESSAGE_ARG
import com.lootspy.screens.addeditfilter.AddEditFilterScreen
import com.lootspy.screens.filter.FilterScreen
import com.lootspy.screens.loot.LootScreen
import com.lootspy.screens.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LootSpyNavGraph(
  modifier: Modifier = Modifier,
  navController: NavHostController,
  startDestination: String = LootSpyDestinations.LOOT_ROUTE,
  navActions: LootSpyNavigationActions,
  selectedRoute: MutableState<String>,
) {
  val currentNavBackStackEntry by navController.currentBackStackEntryAsState()

  AnimatedNavHost(
    navController = navController,
    startDestination = startDestination,
    enterTransition = {
      slideIntoContainer(
        getSlideDirection(
          initial = initialState.destination.route,
          target = targetState.destination.route,
          navActions = navActions
        ),
        animationSpec = tween(700)
      )
    },
    exitTransition = {
      slideOutOfContainer(
        getSlideDirection(
          initial = initialState.destination.route,
          target = targetState.destination.route,
          navActions = navActions
        ),
        animationSpec = tween(700)
      )
    },
    popEnterTransition = {
      slideIntoContainer(
        AnimatedContentScope.SlideDirection.Right,
        animationSpec = tween(700)
      )
    },
    popExitTransition = {
      slideOutOfContainer(
        AnimatedContentScope.SlideDirection.Right,
        animationSpec = tween(700)
      )
    },
    modifier = modifier
  ) {
    composable(
      LootSpyDestinations.LOOT_ROUTE,
      arguments = listOf(navArgument(USER_MESSAGE_ARG) {
        type = NavType.IntType; defaultValue = 0
      }),
    ) {
      LootScreen()
    }
    composable(
      LootSpyDestinations.FILTERS_ROUTE,
      arguments = listOf(navArgument(USER_MESSAGE_ARG) {
        type = NavType.IntType; defaultValue = 0
      })
    ) {
      FilterScreen(
        onAddFilter = { navActions.navigateToAddEditFilter(null) },
        onClickFilter = { navActions.navigateToAddEditFilter(it.id) },
        onBack = {
          selectedRoute.value = LootSpyDestinations.LOOT_ROUTE
          navController.popBackStack()
        }
      )
    }
    composable(
      LootSpyDestinations.ADD_EDIT_FILTER_ROUTE,
      arguments = listOf(
        navArgument(FILTER_ID_ARG) {
          type = NavType.StringType; nullable = true
        },
      ),
    ) {
      AddEditFilterScreen(
        onBack = { navController.popBackStack() },
      )
    }
    composable(
      LootSpyDestinations.VENDORS_ROUTE,
      arguments = listOf(navArgument(USER_MESSAGE_ARG) {
        type = NavType.IntType; defaultValue = 0
      }),
    ) {

    }
    composable(LootSpyDestinations.SETTINGS_ROUTE,
      arguments = listOf(navArgument(USER_MESSAGE_ARG) {
        type = NavType.IntType; defaultValue = 0
      })
    ) {
      SettingsScreen()
    }
  }
}

@OptIn(ExperimentalAnimationApi::class)
private fun getSlideDirection(
  initial: String?,
  target: String?,
  navActions: LootSpyNavigationActions
): AnimatedContentScope.SlideDirection {
  val initialOrder = navActions.routeOrderingMap[initial]
  val targetOrder = navActions.routeOrderingMap[target]
  if (initialOrder == null || targetOrder == null) {
    // Not navigating from the nav bar
    return AnimatedContentScope.SlideDirection.Left
  }
  return if (initialOrder < targetOrder) {
    AnimatedContentScope.SlideDirection.Left
  } else {
    AnimatedContentScope.SlideDirection.Right
  }
}