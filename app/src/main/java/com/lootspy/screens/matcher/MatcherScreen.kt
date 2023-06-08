package com.lootspy.screens.matcher

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatcherScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MatcherViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  BackHandler(onBack = onBack)
  Scaffold(
    topBar = {

    },
    modifier = modifier.fillMaxSize()
  ) {
    it
  }
}