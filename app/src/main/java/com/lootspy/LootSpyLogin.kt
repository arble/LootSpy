package com.lootspy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LootSpyLoginPrompt(
  loginAction: () -> Unit,
) {
  Scaffold(
    modifier = Modifier.fillMaxSize()
  ) {
    Column(
      modifier = Modifier.padding(it),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(text = stringResource(id = R.string.login_prompt))
      TextButton(onClick = loginAction) {
        Text(text = stringResource(id = R.string.login_button))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LootSpyTokenPlaceholder() {
  Scaffold(
    modifier = Modifier.fillMaxSize()
  ) {
    Column(
      modifier = Modifier.padding(it),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(text = stringResource(id = R.string.getting_access_token))
      CircularProgressIndicator()
    }
  }
}