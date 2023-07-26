package com.lootspy.screens.addeditfilter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lootspy.R
import com.lootspy.data.matcher.FilterMatcher
import com.lootspy.data.matcher.MatcherType
import com.lootspy.data.matcher.ItemMatcher
import com.lootspy.screens.addeditfilter.matcher.ItemMatcherDetails
import com.lootspy.screens.addeditfilter.matcher.ItemMatcherSummary
import com.lootspy.util.TextAlertDialog
import com.lootspy.util.TextInputDialog
import com.lootspy.util.Validation
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditFilterScreen(
  onBack: () -> Unit,
  modifier: Modifier = Modifier,
  viewModel: AddEditFilterViewModel = hiltViewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  var showUnsavedChangesAlert by remember { mutableStateOf(false) }
  var showEnterNicknameDialog by remember { mutableStateOf(false) }
  var showEditFilterNameDialog by remember { mutableStateOf(false) }
  var showMatcherSheet by remember { mutableStateOf(false) }
  val innerOnBack = {
    if (viewModel.checkModifiedFilter()) {
      showUnsavedChangesAlert = true
    } else {
      onBack()
    }
  }
  val sheetState =
    remember { SheetState(skipPartiallyExpanded = true, initialValue = SheetValue.Hidden) }
  val scope = rememberCoroutineScope()
  BackHandler(onBack = innerOnBack)
  Scaffold(
    topBar = {
      AddEditFilterTopAppBar(
        filterName = uiState.name.ifEmpty { stringResource(id = R.string.add_edit_filter_new_filter) },
        onBack = innerOnBack,
        addFilterMatcher = {
          showMatcherSheet = true
        },
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
    val activeMatcherState = viewModel.activeMatcher.collectAsStateWithLifecycle()
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
      TextAlertDialog(
        titleText = stringResource(id = R.string.add_edit_filter_unsaved_changes),
        messageText = stringResource(id = R.string.add_edit_filter_unsaved_changes_message),
        ackText = stringResource(id = R.string.dialog_keep_editing),
        confirmText = stringResource(id = R.string.dialog_discard),
        onDismiss = { showUnsavedChangesAlert = false },
        onConfirm = { showUnsavedChangesAlert = false; onBack() }
      )
    }

    Column(
      modifier = Modifier.padding(paddingValues),
      verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      uiState.matchers.forEachIndexed { index, matcher ->
        MatcherSummary(
          matcher = matcher,
          index = index,
          onMatcherClick = { selectedIndex, selectedMatcher ->
            viewModel.setActiveMatcher(selectedMatcher, selectedIndex)
            scope.launch {
              showMatcherSheet = true
              sheetState.show()
            }
          })
      }
    }
    if (showMatcherSheet) {
      ModalBottomSheet(
        onDismissRequest = { viewModel.clearActiveFilter(); showMatcherSheet = false },
        sheetState = sheetState,
        modifier = Modifier.fillMaxHeight(0.8f)
      ) {
        val (activeMatcher, activeIndex) = activeMatcherState.value
        Box(modifier = Modifier.fillMaxWidth()) {
          val density = LocalDensity.current
          androidx.compose.animation.AnimatedVisibility(
            visible = activeMatcher == null,
            exit = slideOutHorizontally { with(density) { (-it / 2).dp.roundToPx() } } +
                fadeOut(targetAlpha = 0.3f)
          ) {
            MatcherTypeSelectorCard()
          }
          androidx.compose.animation.AnimatedVisibility(
            visible = activeMatcher != null,
            enter = slideInHorizontally { with(density) { (it / 2).dp.roundToPx() } } +
                fadeIn(initialAlpha = 0.3f)
          ) {
            when (activeMatcher) {
              is ItemMatcher -> ItemMatcherDetails(
                matcher = activeMatcher,
                index = activeIndex,
                onFinish = {
                  scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                      viewModel.clearActiveFilter()
                      showMatcherSheet = false
                    }
                  }
                }
              )

              else -> Unit
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MatcherTypeSelectorCard(
  viewModel: AddEditFilterViewModel = hiltViewModel()
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceTint)
  ) {
    Text(
      text = stringResource(id = R.string.add_matcher_dialog_text),
      textAlign = TextAlign.Center,
      modifier = Modifier
        .padding(top = 5.dp)
        .fillMaxWidth(),
      style = MaterialTheme.typography.labelLarge,
      maxLines = 2,
      overflow = TextOverflow.Ellipsis
    )
    Spacer(modifier = Modifier.height(6.dp))
    var selectedType by remember { mutableStateOf(MatcherType.NAME) }
    Row(modifier = Modifier.fillMaxWidth()) {
      var expanded by remember { mutableStateOf(false) }
      ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = {
        expanded = !expanded
      }) {
        TextField(
          modifier = Modifier.menuAnchor(),
          readOnly = true,
          label = { Text(stringResource(id = R.string.add_matcher_type_dropdown_label)) },
          value = selectedType.printableName(),
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
          colors = ExposedDropdownMenuDefaults.textFieldColors(),
          onValueChange = {}
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
          MatcherType.displayValues().forEach {
            DropdownMenuItem(text = { Text(it.printableName()) }, onClick = {
              selectedType = it
              expanded = false
            })
          }
        }
      }
      IconButton(onClick = { viewModel.setActiveMatcher(type = selectedType) }) {
        Icon(imageVector = Icons.Default.Check, contentDescription = null)
      }
    }
    Text(
      text = stringResource(id = selectedType.descriptionResource()),
      modifier = Modifier.padding(all = 8.dp)
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
private fun MatcherSummary(
  matcher: FilterMatcher,
  index: Int,
  onMatcherClick: (Int, FilterMatcher) -> Unit,
) {
  when (matcher) {
    is ItemMatcher -> ItemMatcherSummary(matcher, index, onMatcherClick)
    else -> Unit
  }
}