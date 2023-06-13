package com.lootspy.screens.addeditfilter.matcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.lootspy.filter.matchers.MatcherType
import com.lootspy.util.SupportingErrorText
import com.lootspy.util.Validation

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
  val inputError = Validation.validate(nameText, Validation.VALIDATORS_NORMAL_TEXT)
  Column(
    modifier = Modifier
      .fillMaxWidth()
  ) {
    Row {
      TextField(
        value = nameText,
        onValueChange = { nameText = it },
        label = { Text("Name") },
        supportingText = { SupportingErrorText(inputError = inputError) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
          onDone = {
            if (inputError == null) {
              newDetails["name"] = nameText
              onSaveMatcher(newDetails)
            }
          }
        ),
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