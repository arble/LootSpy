package com.lootspy

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
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
import com.lootspy.addeditfilter.AddEditFilterScreen
import com.lootspy.filter.FilterScreen
import com.lootspy.loot.LootScreen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LootSpyNavGraph(
  modifier: Modifier = Modifier,
  navController: NavHostController,
  startDestination: String = LootSpyDestinations.LOOT_ROUTE,
  navActions: LootSpyNavigationActions,
) {
  val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

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
        onClickFilter = { navActions.navigateToAddEditFilter(it.id) }
      )
    }
    composable(
      LootSpyDestinations.ADD_EDIT_FILTER_ROUTE,
      arguments = listOf(
        navArgument(FILTER_ID_ARG) {
          type = NavType.StringType; nullable = true
        },
      ),
      enterTransition = {
        slideIntoContainer(
          AnimatedContentScope.SlideDirection.Up,
          animationSpec = tween(700)
        )
      },
      exitTransition = {
        slideOutOfContainer(
          AnimatedContentScope.SlideDirection.Down,
          animationSpec = tween(700)
        )
      },
      popExitTransition = {
        slideOutOfContainer(
          AnimatedContentScope.SlideDirection.Down,
          animationSpec = tween(700)
        )
      }
    ) {
      AddEditFilterScreen(
        onBack = { navController.popBackStack() },
      )
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