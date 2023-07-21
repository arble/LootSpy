package com.lootspy.screens.addeditfilter.matcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lootspy.R
import com.lootspy.data.matcher.MatcherType
import com.lootspy.data.matcher.NameMatcher
import com.lootspy.screens.addeditfilter.AddEditFilterViewModel
import com.lootspy.util.SupportingErrorText
import com.lootspy.util.Validation

@Composable
fun ItemMatcherSummary(
  summaryText: String,
  onMatcherClick: () -> Unit,
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = Modifier
      .clickable { onMatcherClick() },
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
        text = summaryText,
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
  details: Map<String, String>?,
  onSaveMatcher: (Map<String, String>) -> Unit,
  onDeleteMatcher: () -> Unit,
  viewModel: AddEditFilterViewModel = hiltViewModel()
) {
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceTint),
  ) {


    val newDetails = remember { mutableStateMapOf("MATCHER_TYPE" to MatcherType.NAME.name) }
    var nameText by remember { mutableStateOf(matcher.name) }
    val inputError = Validation.validate(nameText, Validation.VALIDATORS_NORMAL_TEXT)
    val isMatched = viewModel.isItemAlreadyMatched(nameText)
    val finalInputError = if (isMatched) R.string.item_matcher_already_matched else inputError
    val suggestions = viewModel.suggestions.collectAsStateWithLifecycle()

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
        onValueChange = { nameText = it; viewModel.getSuggestions(it) },
        label = { Text("Name") },
        supportingText = { SupportingErrorText(inputError = finalInputError) },
//        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
//        keyboardActions = KeyboardActions(
//          onDone = {
//            if (inputError == null) {
//              newDetails["name"] = nameText
//              onSaveMatcher(newDetails)
//            }
//          }
//        ),
        modifier = Modifier.weight(1f)
      )
      IconButton(
        onClick = { newDetails["name"] = nameText; onSaveMatcher(newDetails) },
        enabled = inputError == null,
      ) {
        Icon(Icons.Default.Check, contentDescription = null)
      }
      IconButton(onClick = onDeleteMatcher) {
        Icon(Icons.Default.Delete, contentDescription = null)
      }
    }
  }
}