package com.lootspy.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lootspy.filter.matchers.MatcherType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatcherDialog(
  show: MutableState<Boolean>,
  onSubmit: (MatcherType) -> Unit
) {
  if (show.value) {
    Dialog(onDismissRequest = { show.value = false }) {
      Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp)
      ) {
        Text(
          text = "Select a type for this matcher",
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
            label = { Text("Type") },
            value = selectedType.printableName(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.textFieldColors(),
            onValueChange = {}
          )
          ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MatcherType.values().forEach {
              DropdownMenuItem(text = { Text(it.name) }, onClick = {
                selectedType = it
                expanded = false
              })
            }
          }
        }
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          TextButton(onClick = { show.value = false }) {
            Text(
              text = "Cancel",
              fontWeight = FontWeight.Bold,
              color = Color.White,
              modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            )
          }
          TextButton(onClick = { show.value = false; onSubmit(selectedType) }) {
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
}

@Composable
fun AlertDialog(
  titleText: String,
  messageText: String,
  ackText: String,
  modifier: Modifier = Modifier,
  confirmText: String? = null,
  onDismiss: () -> Unit,
  onConfirm: () -> Unit = {},
) {
  Dialog(onDismissRequest = onDismiss) {
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
        )
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
          TextButton(onClick = onDismiss) {
            Text(
              text = ackText,
              textAlign = TextAlign.Center,
              fontWeight = FontWeight.ExtraBold,
              modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            )
          }
          if (confirmText != null) {
            TextButton(onClick = onConfirm) {
              Text(
                text = confirmText,
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

}
