package com.lootspy.screens.addeditfilter

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lootspy.R
import com.lootspy.filter.matchers.FilterMatcher
import com.lootspy.filter.matchers.MatcherType
import com.lootspy.filter.matchers.NameMatcher
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
          details = uiState.selectedMatcherFields,
          onMatcherClick = onMatcherClick,
          onSaveMatcher = { viewModel.updateMatcherFields(it) }
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

@Composable
private fun FilterMatcherItem(
  matcher: FilterMatcher,
  index: Int,
  selected: Boolean,
  details: Map<String, String>?,
  onMatcherClick: (FilterMatcher, Int) -> Unit,
  onSaveMatcher: (Map<String, String>) -> Unit,
) {
  val background =
    if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceTint
  LocalContext.current
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = background),
    modifier = Modifier
      .clickable { onMatcherClick(matcher, index) }
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
    AnimatedVisibility(
      visible = selected,
      enter = fadeIn() + expandVertically(tween(500)),
      exit = fadeOut() + shrinkVertically(tween(500)),
    ) {
      MatcherDetails(matcher = matcher, details = details, onSaveMatcher = onSaveMatcher)
//      Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier
//          .fillMaxWidth()
//          .padding(
//            horizontal = dimensionResource(id = R.dimen.horizontal_margin),
//            vertical = dimensionResource(id = R.dimen.loot_item_padding),
//          )
//      ) {
//        Text(
//          text = matcher.summaryString(),
//          style = MaterialTheme.typography.headlineSmall,
//          modifier = Modifier.padding(
//            start = dimensionResource(
//              id = R.dimen.horizontal_margin
//            )
//          ),
//        )
//      }
    }
  }
}

@Composable
fun MatcherDetails(
  matcher: FilterMatcher,
  details: Map<String, String>?,
  onSaveMatcher: (Map<String, String>) -> Unit,
) {
  when (matcher) {
    is NameMatcher -> NameMatcherDetails(
      details = details,
      onSaveMatcher = onSaveMatcher
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameMatcherDetails(
  details: Map<String, String>?,
  onSaveMatcher: (Map<String, String>) -> Unit
) {
  if (details == null) {
    return
  }
  val newDetails = remember { mutableStateMapOf<String, String>() }
  newDetails["MATCHER_TYPE"] = MatcherType.NAME.name
  var nameText by remember { mutableStateOf(details["name"]!!) }
  Column(modifier = Modifier.fillMaxWidth()) {
    Row {
      TextField(value = nameText, onValueChange = { nameText = it }, label = { Text("Name") })
      IconButton(onClick = { newDetails["name"] = nameText; onSaveMatcher(newDetails) }) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null)
      }
    }
  }
}