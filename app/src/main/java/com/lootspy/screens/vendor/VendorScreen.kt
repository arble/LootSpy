package com.lootspy.screens.vendor

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lootspy.R
import com.lootspy.util.ScreenContentWithEmptyText
import com.lootspy.util.VendorTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorScreen(
  onClickVendor: () -> Unit,
  onBack: () -> Unit,
  viewModel: VendorViewModel = hiltViewModel()
) {
  BackHandler(onBack = onBack)
  Scaffold(
    topBar = {
      VendorTopAppBar()
    }
  ) { paddingValues ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ScreenContentWithEmptyText(
      loading = uiState.isLoading,
      items = uiState.vendors,
      itemContent = { _, _ ->  },
      emptyText = stringResource(id = R.string.vendor_screen_no_vendors),
      modifier = Modifier.padding(paddingValues),
    )
  }
}