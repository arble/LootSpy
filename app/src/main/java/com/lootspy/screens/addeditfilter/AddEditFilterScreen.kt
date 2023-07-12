package com.lootspy.screens.addeditfilter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.lootspy.R
import com.lootspy.data.matcher.FilterMatcher
import com.lootspy.data.matcher.NameMatcher
import com.lootspy.screens.addeditfilter.matcher.NameMatcherDetails
import com.lootspy.util.AlertDialog
import com.lootspy.util.NewMatcherDialog
import com.lootspy.util.ScreenContentWithEmptyText
import com.lootspy.util.TextInputDialog
import com.lootspy.util.Validation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFilterScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AddEditFilterViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  var showNewMatcherDialog by remember { mutableStateOf(false) }
  var showUnsavedChangesAlert by remember { mutableStateOf(false) }
  var showEnterNicknameDialog by remember { mutableStateOf(false) }
  var showAlreadyMatchedDialog by remember { mutableStateOf(false) }
  var showEditFilterNameDialog by remember { mutableStateOf(false) }
  val context = LocalContext.current
  val innerOnBack = {
    if (viewModel.checkModifiedFilter()) {
      showUnsavedChangesAlert = true
    } else {
      onBack()
    }
  }
  BackHandler(onBack = innerOnBack)
  Scaffold(
    topBar = {
      AddEditFilterTopAppBar(
        filterName = uiState.name.ifEmpty { stringResource(id = R.string.add_edit_filter_new_filter) },
        onBack = innerOnBack,
        addFilterMatcher = { showNewMatcherDialog = true },
        editFilterName = { showEditFilterNameDialog = true },
      )
    },
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    modifier = modifier.fillMaxSize(),
    floatingActionButton = {
      if (uiState.matchers.isNotEmpty() && uiState.selectedMatcher == null) {
        FloatingActionButton(onClick = {
          if (uiState.name.isEmpty()) {
            showEnterNicknameDialog = true
          } else {
            viewModel.updateFilter()
            onBack()
          }
        }) {
          Icon(imageVector = Icons.Filled.Check, contentDescription = null)
        }
      }
    }
  ) { paddingValues ->

    val onMatcherClick: (FilterMatcher, Int) -> Unit = { _, index ->
      viewModel.updateSelectedMatcher(index)
    }
    if (showNewMatcherDialog) {
      NewMatcherDialog(
        onDismiss = { showNewMatcherDialog = false },
        onSubmit = {
          showNewMatcherDialog = false
          viewModel.createBlankMatcher(it)
        },
      )
    }
    if (showEnterNicknameDialog) {
      TextInputDialog(
        titleText = stringResource(id = R.string.add_edit_filter_new_filter_name_title),
        messageText = "",
        labelText = stringResource(R.string.add_edit_filter_edit_name_label),
        initialFieldText = "",
        validators = listOf(Validation.VALIDATOR_EMPTY),
        submitText = stringResource(id = R.string.dialog_submit),
        onSubmit = { viewModel.createNewFilter(it); showEnterNicknameDialog = false; onBack() },
        cancelText = stringResource(id = R.string.dialog_cancel),
        onCancel = { showEnterNicknameDialog = false }
      )
    }
    if (showEditFilterNameDialog) {
      TextInputDialog(
        titleText = stringResource(R.string.add_edit_filter_edit_name_title),
        messageText = "",
        labelText = stringResource(R.string.add_edit_filter_edit_name_label),
        initialFieldText = uiState.name,
        validators = listOf(Validation.VALIDATOR_EMPTY),
        submitText = stringResource(R.string.dialog_submit),
        onSubmit = { viewModel.changeFilterName(it); showEditFilterNameDialog = false },
        cancelText = stringResource(R.string.dialog_cancel),
        onCancel = { showEditFilterNameDialog = false }
      )
    }
    if (showUnsavedChangesAlert) {
      AlertDialog(
        titleText = stringResource(id = R.string.add_edit_filter_unsaved_changes),
        messageText = stringResource(id = R.string.add_edit_filter_unsaved_changes_message),
        ackText = stringResource(id = R.string.dialog_keep_editing),
        confirmText = stringResource(id = R.string.dialog_discard),
        onDismiss = { showUnsavedChangesAlert = false },
        onConfirm = { showUnsavedChangesAlert = false; onBack() }
      )
    }
    if (showAlreadyMatchedDialog) {
      AlertDialog(
        titleText = stringResource(id = R.string.add_edit_filter_already_matched_title),
        messageText = stringResource(id = R.string.add_edit_filter_already_matched_message),
        ackText = stringResource(id = R.string.dialog_ok),
        onDismiss = { showAlreadyMatchedDialog = false }
      )
    }
    if (uiState.removedMatchers != null && uiState.removedMatchers!! > 0) {
      LaunchedEffect(snackbarHostState) {
        snackbarHostState.showSnackbar(
          withDismissAction = true,
          message = context.resources.getString(
            R.string.add_edit_filter_removed_matchers,
            uiState.removedMatchers,
            if (uiState.removedMatchers == 1) "" else "s",
          )
//          message = "${uiState.removedMatchers} redundant matcher" +
//              "${if (uiState.removedMatchers == 1) "" else "s"} removed"
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
  filterName: String,
  onBack: () -> Unit,
  addFilterMatcher: () -> Unit,
  editFilterName: () -> Unit,
) {
  TopAppBar(
    title = { Text(text = filterName) },
    navigationIcon = {
      IconButton(onClick = onBack) {
        Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.back))
      }
    },
    actions = {
      IconButton(onClick = editFilterName) {
        Icon(Icons.Default.Edit, stringResource(id = R.string.add_edit_filter_edit_name))
      }
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
    if (selected) MaterialTheme.colorScheme.surfaceTint else MaterialTheme.colorScheme.surfaceVariant
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

