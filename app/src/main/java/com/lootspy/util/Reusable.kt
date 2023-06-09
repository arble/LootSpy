package com.lootspy.util

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * Display one of three things: a loading indicator; a list of items generated by itemContent; or
 * a supplied composable to represent the lack of any items.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ScreenContent(
  loading: Boolean,
  items: List<T>,
  itemContent: @Composable (Int, T) -> Unit,
  modifier: Modifier = Modifier,
  emptyComposable: @Composable () -> Unit,
) {
  if (loading) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Center) {
      CircularProgressIndicator(modifier = modifier)
    }
  } else if (items.isNotEmpty()) {
    LazyColumn(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
      itemsIndexed(items = items) {index, item ->
//        Row(Modifier.animateItemPlacement(
//          tween(durationMillis = 500, easing = LinearEasing)
//        )) {
          itemContent(index, item)
//        }
      }
    }
  } else {
    Box(modifier = modifier.fillMaxHeight(), contentAlignment = CenterStart) {
      emptyComposable()
    }
  }
}

@Composable
fun <T> ScreenContentWithEmptyText(
  loading: Boolean,
  items: List<T>,
  itemContent: @Composable (Int, T) -> Unit,
  emptyText: String,
  modifier: Modifier = Modifier,
  textStyle: TextStyle = MaterialTheme.typography.headlineMedium
) {
  ScreenContent(loading = loading, items = items, itemContent = itemContent, modifier = modifier) {
    Text(
      text = emptyText,
      style = textStyle
    )
  }
}