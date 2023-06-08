package com.lootspy.screens.addeditfilter

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lootspy.R
import com.lootspy.filter.matchers.FilterMatcher
import com.lootspy.util.NewMatcherDialog
import com.lootspy.util.ScreenContentWithEmptyText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFilterScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AddEditFilterViewModel = hiltViewModel()
) {
  val context = LocalContext.current
  val showNewMatcherDialog = remember { mutableStateOf(false) }
  BackHandler(onBack = onBack)
  Scaffold(
    topBar = {
      AddEditFilterTopAppBar(
        onBack = onBack,
        addFilterMatcher = {})
    },
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(onClick = { showNewMatcherDialog.value = true }) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
      }
    }
  ) { paddingValues ->
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val onMatcherClick: (FilterMatcher, Int) -> Unit = { _, index ->
//      Toast.makeText(context, "Matcher is: ${matcher.summaryString()}", Toast.LENGTH_SHORT).show()
      viewModel.updateSelectedMatcher(index)
    }
    NewMatcherDialog(show = showNewMatcherDialog) {
      showNewMatcherDialog.value = false
      viewModel.createBlankMatcher(it)
      Toast.makeText(context, "New matcher: ${it.name}", Toast.LENGTH_SHORT).show()
    }
    ScreenContentWithEmptyText(
      loading = uiState.isLoading,
      items = uiState.matchers,
      itemContent = { index, matcher ->
        FilterMatcherItem(
          matcher = matcher,
          index = index,
          selected = uiState.selectedMatcher == index,
          onMatcherClick = onMatcherClick
        )
      },
      emptyText = stringResource(id = R.string.add_edit_filter_screen_empty),
      modifier = Modifier.padding(paddingValues)
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFilterTopAppBar(
  onBack: () -> Unit,
  addFilterMatcher: () -> Unit,
) {
  TopAppBar(
    title = { Text(text = stringResource(id = R.string.add_filter_matcher_title)) },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.back))
      }
    },
    actions = {
      IconButton(onClick = addFilterMatcher) {
        Icon(Icons.Default.Add, stringResource(id = R.string.add_edit_filter_add_matcher))
      }
    }
  )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun FilterMatcherItem(
  matcher: FilterMatcher,
  index: Int,
  selected: Boolean,
  onMatcherClick: (FilterMatcher, Int) -> Unit,
) {
  val background =
    if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceTint
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = background),
    modifier = Modifier.clickable { onMatcherClick(matcher, index) }.animateContentSize(
//      animationSpec = tween(durationMillis = 300, easing = LinearEasing)
    )
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(
          horizontal = dimensionResource(id = R.dimen.horizontal_margin),
          vertical = dimensionResource(id = R.dimen.loot_item_padding),
        )
    ) {
      Text(
        text = matcher.summaryString(),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(
          start = dimensionResource(
            id = R.dimen.horizontal_margin
          )
        ),
      )
    }
    if (selected) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            horizontal = dimensionResource(id = R.dimen.horizontal_margin),
            vertical = dimensionResource(id = R.dimen.loot_item_padding),
          )
      ) {
        Text(
          text = matcher.summaryString(),
          style = MaterialTheme.typography.headlineSmall,
          modifier = Modifier.padding(
            start = dimensionResource(
              id = R.dimen.horizontal_margin
            )
          ),
        )
      }
    }
  }
}