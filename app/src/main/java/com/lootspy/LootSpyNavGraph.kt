package com.lootspy

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import com.lootspy.LootSpyDestinationArgs.FILTER_ID_ARG
import com.lootspy.LootSpyDestinationArgs.USER_MESSAGE_ARG
import com.lootspy.addeditfilter.AddEditFilterScreen
import com.lootspy.filter.FilterScreen
import com.lootspy.loot.LootScreen
import com.lootspy.util.AppModalDrawer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun LootSpyNavGraph(
  modifier: Modifier = Modifier,
  navController: NavHostController = rememberAnimatedNavController(),
  coroutineScope: CoroutineScope = rememberCoroutineScope(),
  drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
  startDestination: String = LootSpyDestinations.LOOT_ROUTE,
  navActions: LootSpyNavigationActions = remember(navController) {
    LootSpyNavigationActions(navController)
  }
) {
  val currentNavBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = currentNavBackStackEntry?.destination?.route ?: startDestination

  AnimatedNavHost(
//  NavHost(
    navController = navController,
    startDestination = startDestination,
    enterTransition = {
//      when (initialState.destination.route) {
//        LootSpyDestinations.LOOT_ROUTE -> EnterTransition.None
//        else -> {
          slideIntoContainer(
            AnimatedContentScope.SlideDirection.Left,
            animationSpec = tween(700)
          )
//        }
//      }
    },
    exitTransition = {
      slideOutOfContainer(
        AnimatedContentScope.SlideDirection.Left,
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
      AppModalDrawer(drawerState, currentRoute, navActions) {
        LootScreen(openDrawer = { coroutineScope.launch { drawerState.open() } })
      }
    }
    composable(
      LootSpyDestinations.FILTERS_ROUTE,
      arguments = listOf(navArgument(USER_MESSAGE_ARG) {
        type = NavType.IntType; defaultValue = 0
      })
    ) {
      AppModalDrawer(drawerState, currentRoute, navActions) {
        FilterScreen(
          openDrawer = { coroutineScope.launch { drawerState.open() } },
          onAddFilter = { navActions.navigateToAddEditFilter(null) },
          onClickFilter = { navActions.navigateToAddEditFilter(it.id) }
        )
      }
    }
    composable(
      LootSpyDestinations.ADD_EDIT_FILTER_ROUTE,
      arguments = listOf(
        navArgument(FILTER_ID_ARG) {
          type = NavType.StringType; nullable = true
        },
      )
    ) {
      AppModalDrawer(drawerState, currentRoute, navActions) {
        AddEditFilterScreen(
          onBack = { navController.popBackStack() },
        )
      }
    }
  }

}