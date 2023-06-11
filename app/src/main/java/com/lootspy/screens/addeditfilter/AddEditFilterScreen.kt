package com.lootspy.screens.addeditfilter

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.lootspy.util.AlertDialog
import com.lootspy.util.NewMatcherDialog
import com.lootspy.util.ScreenContentWithEmptyText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFilterScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AddEditFilterViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val showNewMatcherDialog = remember { mutableStateOf(false) }
  var showAlreadyMatchedDialog by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val innerOnBack = { onBack() }
  BackHandler(onBack = innerOnBack)
  Scaffold(
    topBar = {
      AddEditFilterTopAppBar(
        onBack = innerOnBack,
        addFilterMatcher = { showNewMatcherDialog.value = true },
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      FloatingActionButton(onClick = { showNewMatcherDialog.value = true }) {
        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
      }
    }
  ) { paddingValues ->

    val onMatcherClick: (FilterMatcher, Int) -> Unit = { _, index ->
      viewModel.updateSelectedMatcher(index)
    }
    if (showNewMatcherDialog.value) {
      NewMatcherDialog(
        show = showNewMatcherDialog,
        onSubmit = {
          showNewMatcherDialog.value = false
          viewModel.createBlankMatcher(it)
        },
      )
    }
    if (showAlreadyMatchedDialog) {
      AlertDialog(
        titleText = "Already matched!",
        messageText = "Other active matchers completely cover everything that this would match.",
        ackText = "OK",
        onDismiss = { showAlreadyMatchedDialog = false }
      )
    }
    if (uiState.removedMatchers != null && uiState.removedMatchers!! > 0) {
      LaunchedEffect(snackbarHostState) {
        snackbarHostState.showSnackbar(
          withDismissAction = true,
          message = "${uiState.removedMatchers} redundant matcher" +
              "${if (uiState.removedMatchers == 1) "" else "s"} removed"
        )
        viewModel.onRedundantMatcherSnackbarDismiss()
      }
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
          onSaveMatcher = {
            showAlreadyMatchedDialog = !viewModel.updateMatcherFields(it, uiState.matchers)
          },
          onDeleteMatcher = { viewModel.deleteSelectedMatcher() },
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
  onDeleteMatcher: () -> Unit,
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
      MatcherDetails(
        matcher = matcher,
        details = details,
        onSaveMatcher = onSaveMatcher,
        onDeleteMatcher = onDeleteMatcher,
      )
    }
  }
}

@Composable
fun MatcherDetails(
  matcher: FilterMatcher,
  details: Map<String, String>?,
  onSaveMatcher: (Map<String, String>) -> Unit,
  onDeleteMatcher: () -> Unit,
) {
  when (matcher) {
    is NameMatcher -> NameMatcherDetails(
      details = details,
      onSaveMatcher = onSaveMatcher,
      onDeleteMatcher = onDeleteMatcher,
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NameMatcherDetails(
  details: Map<String, String>?,
  onSaveMatcher: (Map<String, String>) -> Unit,
  onDeleteMatcher: () -> Unit,
) {
  if (details == null) {
    return
  }
  val newDetails = remember { mutableStateMapOf<String, String>() }
  newDetails["MATCHER_TYPE"] = MatcherType.NAME.name
  var nameText by remember { mutableStateOf(details["name"]!!) }
  val emptyText = nameText.isEmpty()
  val badCharacters = nameText.contains("[^a-zA-Z0-9 ]".toRegex())
  val errorText = if (emptyText) {
    "Cannot be empty"
  } else if (badCharacters) {
    "Can match only alphanumerics and spaces"
  } else {
    ""
  }
  Column(
    modifier = Modifier
      .fillMaxWidth()
  ) {
    Row {
      TextField(
        value = nameText,
        onValueChange = { nameText = it },
        label = { Text("Name") },
        supportingText = {
          Text(
            text = errorText,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.error,
          )
        },
        modifier = Modifier.weight(1f)
      )
      IconButton(
        onClick = { newDetails["name"] = nameText; onSaveMatcher(newDetails) },
        enabled = !emptyText && !badCharacters,
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
      }
      IconButton(onClick = onDeleteMatcher) {
        Icon(Icons.Default.Delete, contentDescription = null)
      }
    }
  }
}