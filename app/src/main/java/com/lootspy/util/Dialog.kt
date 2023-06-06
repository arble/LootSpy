package com.lootspy.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.lootspy.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewFilterDialog(
  show: MutableState<Boolean>,
  onClick: (String) -> Unit
) {
  if (show.value) {
    Dialog(onDismissRequest = { show.value = false }) {
      Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(10.dp, 5.dp, 10.dp, 10.dp),
      ) {
        Image(
          painter = painterResource(id = R.drawable.logo_no_fill),
          null,
          contentScale = ContentScale.Fit,
          modifier = Modifier
            .padding(top = 35.dp)
            .height(70.dp)
            .fillMaxWidth()
        )
        Text(
          text = "Enter a name",
          textAlign = TextAlign.Center,
          modifier = Modifier
            .padding(top = 5.dp)
            .fillMaxWidth(),
          style = MaterialTheme.typography.labelLarge,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        var text by remember { mutableStateOf("Epic loot") }
        TextField(value = text, onValueChange = { text = it })
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
          TextButton(onClick = { show.value = false; onClick(text) }) {
            Text(
              text = "Submit",
              color = Color.White,
              modifier = Modifier.padding(top = 5.dp, bottom = 5.dp)
            )
          }
        }
      }
    }
  }
}
