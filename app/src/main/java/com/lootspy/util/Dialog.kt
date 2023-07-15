package com.lootspy.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lootspy.R
import com.lootspy.data.matcher.MatcherType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatcherDialog(
  onDismiss: () -> Unit,
  onSubmit: (MatcherType) -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
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
      var expanded by remember { mutableStateOf(false) }
      var selectedType by remember { mutableStateOf(MatcherType.NAME) }
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
          MatcherType.values().forEach {
            DropdownMenuItem(text = { Text(it.printableName()) }, onClick = {
              selectedType = it
              expanded = false
            })
          }
        }
      }
      Text(
        text = stringResource(id = selectedType.descriptionResource()),
        modifier = Modifier.padding(all = 8.dp)
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround
      ) {
        TextButton(onClick = onDismiss) {
          Text(
            text = "Cancel",
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
          )
        }
        TextButton(onClick = { onSubmit(selectedType) }) {
          Text(
            text = "Confirm",
            color = Color.White,
            modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
          )
        }
      }
    }
  }
}

@Composable
fun TextAlertDialog(
  titleText: String,
  messageText: String,
  ackText: String,
  confirmText: String? = null,
  modal: Boolean = false,
  onConfirm: () -> Unit = {},
  onDismiss: () -> Unit,
) {
  AlertDialog(
    titleText = titleText,
    modal = modal,
    dialogContents = {
      Text(
        text = messageText,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(top = 10.dp, start = 25.dp, end = 25.dp)
          .fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium
      )
      Spacer(modifier = Modifier.height(12.dp))
      Row(modifier = Modifier.fillMaxWidth()) {
        TextButton(
          onClick = onDismiss,
          modifier = Modifier.weight(if (confirmText != null) 0.5f else 1f)
        ) {
          Text(
            text = ackText,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
          )
        }
        if (confirmText != null) {
          TextButton(
            onClick = onConfirm,
            modifier = Modifier.weight(0.5f)
          ) {
            Text(
              text = confirmText,
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.ExtraBold,
            )
          }
        }
      }
    }) {

  }
}

@Composable
fun ProgressAlertDialog(
  titleText: String,
  infoText: String,
  progress: Float,
  cancelText: String? = null,
  onCancel: () -> Unit = {},
) {
  AlertDialog(
    titleText = titleText,
    onDismiss = onCancel,
    modal = cancelText == null,
    dialogContents = {
      Text(
        text = infoText,
        textAlign = TextAlign.Center,
        modifier = Modifier
          .padding(top = 10.dp, start = 25.dp, end = 25.dp)
          .fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.Black,
      )
      Spacer(modifier = Modifier.height(12.dp))
      val progressModifier = Modifier.align(Alignment.CenterHorizontally)
      if (progress > 0f) {
        CircularProgressIndicator(progress = progress, modifier = progressModifier)
      } else {
        CircularProgressIndicator(modifier = progressModifier)
      }
      if (cancelText != null) {
        TextButton(
          onClick = onCancel,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = cancelText,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,
          )
        }
      }
    })
}

@Composable
fun AlertDialog(
  titleText: String,
  dialogContents: @Composable ColumnScope.() -> Unit,
  modifier: Modifier = Modifier,
  modal: Boolean = false,
  onDismiss: () -> Unit,
) {
  Dialog(onDismissRequest = if (modal) {
    {}
  } else onDismiss) {
    Card(
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
    ) {
      Column(modifier.background(Color.White)) {
        Text(
          text = titleText,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .padding(top = 5.dp)
            .fillMaxWidth(),
          style = MaterialTheme.typography.labelLarge,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = Color.Black,
        )
        dialogContents()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputDialog(
  titleText: String,
  messageText: String,
  labelText: String,
  initialFieldText: String,
  modifier: Modifier = Modifier,
  validators: List<Validator>,
  submitText: String,
  onSubmit: (String) -> Unit,
  cancelText: String,
  onCancel: () -> Unit,
) {
  var nameText by remember { mutableStateOf(initialFieldText) }
  val inputError = Validation.validate(nameText, validators)
  Dialog(onDismissRequest = onCancel) {
    Card(
      shape = RoundedCornerShape(10.dp),
      modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
    ) {
      Column(modifier.background(Color.White)) {
        Text(
          text = titleText,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .padding(top = 5.dp)
            .fillMaxWidth(),
          style = MaterialTheme.typography.labelLarge,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = Color.Black,
        )
        Text(
          text = messageText,
          textAlign = TextAlign.Center,
          modifier = Modifier
            .padding(top = 10.dp, start = 25.dp, end = 25.dp)
            .fillMaxWidth(),
          style = MaterialTheme.typography.bodyMedium,
          color = Color.Black,
        )
        Spacer(modifier = Modifier.height(12.dp))
        TextField(
          value = nameText,
          onValueChange = { nameText = it },
          label = { Text(labelText) },
          supportingText = { SupportingErrorText(inputError = inputError) },
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
          keyboardActions = KeyboardActions(
            onDone = {
              if (inputError == null) {
                onSubmit(nameText)
              }
            }
          ),
        )
        Row(modifier = Modifier.fillMaxWidth()) {
          TextButton(onClick = onCancel, modifier = Modifier.weight(0.5f)) {
            Text(
              text = cancelText,
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            )
          }
          TextButton(
            enabled = nameText.isNotEmpty(),
            onClick = { onSubmit(nameText) },
            modifier = Modifier.weight(0.5f)
          ) {
            Text(
              text = submitText,
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            )
          }
        }
      }
    }
  }
}
