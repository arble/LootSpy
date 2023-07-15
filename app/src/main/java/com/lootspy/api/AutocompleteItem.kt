package com.lootspy.api

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

data class AutocompleteItem(
  val name: String,
  val iconPath: String,
  val watermarkPath: String,
  val damageType: String,
  val damageIconPath: String,
) {

  @Composable
  fun AutoCompleteItemRow(
    modifier: Modifier = Modifier,
    dp: Dp = 32.dp
  ) {
    Row(modifier = modifier.height(dp)) {
      Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        AsyncImage(model = iconPath, contentDescription = null)
        AsyncImage(model = watermarkPath, contentDescription = null)
      }
    }
  }
}