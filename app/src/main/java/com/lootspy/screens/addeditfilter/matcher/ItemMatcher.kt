package com.lootspy.screens.addeditfilter.matcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lootspy.R
import com.lootspy.data.matcher.FilterMatcher
import com.lootspy.data.matcher.NameMatcher
import com.lootspy.screens.addeditfilter.AddEditFilterViewModel
import com.lootspy.util.SupportingErrorText
import com.lootspy.util.TextAlertDialog
import com.lootspy.util.Validation

@Composable
fun ItemMatcherSummary(
  matcher: FilterMatcher,
  index: Int,
  onMatcherClick: (Int, FilterMatcher) -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = Modifier
      .clickable { onMatcherClick(index, matcher) },
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
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemMatcherDetails(
  matcher: NameMatcher,
  index: Int?,
  onFinish: () -> Unit,
  viewModel: AddEditFilterViewModel = hiltViewModel()
) {
  val suggestions = viewModel.suggestions.collectAsStateWithLifecycle()
  var alreadyMatched by remember { mutableStateOf(false) }
  var confirmDelete by remember { mutableStateOf(false) }
  if (alreadyMatched) {
    TextAlertDialog(
      titleText = "Already matched!",
      messageText = stringResource(id = R.string.item_matcher_already_matched),
      ackText = "OK",
    ) {
      alreadyMatched = false
    }
  }
  if (confirmDelete) {
    TextAlertDialog(
      titleText = "Delete matcher?",
      messageText = "Really delete this matcher?",
      ackText = "Cancel",
      confirmText = "Confirm",
      onConfirm = { viewModel.deleteSelectedMatcher(); confirmDelete = false }
    ) {
      confirmDelete = false
    }
  }
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceTint),
  ) {
    var nameText by remember { mutableStateOf(matcher.name) }
    var inputError by remember { mutableStateOf<Int?>(null) }
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
    Row {
      TextField(
        value = nameText,
        onValueChange = {
          nameText = it
          val validationError = Validation.validate(it, Validation.VALIDATORS_NORMAL_TEXT)
          if (validationError != null) {
            inputError = validationError
          } else {
            viewModel.getSuggestions(it)
          }
        },
        label = { Text("Name") },
        supportingText = { SupportingErrorText(inputError = inputError) },
        modifier = Modifier.weight(1f)
      )
    }
  }
  suggestions.value.forEach { item ->
    item.Composable(onClick = {
      if (viewModel.saveItemMatcher(index, it)) {
        onFinish()
      } else {
        alreadyMatched = true
      }
    })
  }
}