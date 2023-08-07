package com.lootspy.elements

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lootspy.api.R
import com.lootspy.data.bungiePath
import com.lootspy.types.item.VendorItem

@Composable
fun BasicItemElement(
  item: VendorItem,
  modifier: Modifier = Modifier,
  placeholder: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
  error: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
  onClick: (VendorItem) -> Unit = {},
  damageIconSize: Dp = 24.dp
) {
  val (cardColour, textColour) = when (item.tier) {
    "Legendary" -> Pair(LEGENDARY_CARD_BG, Color.White)
    "Exotic" -> Pair(EXOTIC_CARD_BG, Color.Black)
    else -> Pair(MaterialTheme.colorScheme.surfaceVariant, Color.White)
  }
  Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = cardColour),
    modifier = modifier
      .height(64.dp)
      .clickable { onClick(item) },
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = modifier
        .fillMaxWidth()
        .fillMaxHeight()
    ) {
      Box(
        modifier = modifier
          .height(64.dp)
          .width(64.dp)
      ) {
        AsyncImage(
          model = item.iconPath.bungiePath(),
          placeholder = placeholder,
          error = error,
          modifier = modifier
            .width(64.dp)
            .fillMaxHeight(),
          contentDescription = null
        )
        AsyncImage(
          model = item.watermarkPath.bungiePath(),
          placeholder = placeholder,
          error = error,
          modifier = modifier
            .width(64.dp)
            .fillMaxHeight(),
          contentDescription = null
        )
      }
      Text(
        text = item.name,
        modifier = modifier.weight(0.5f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = textColour
      )
      AsyncImage(
        model = item.damageIconPath?.bungiePath(),
        placeholder = placeholder,
        error = error,
        modifier = modifier
          .width(damageIconSize)
          .height(damageIconSize),
        contentDescription = null
      )
      Column(
        modifier
          .fillMaxHeight()
          .weight(0.5f),
        horizontalAlignment = Alignment.Start
      ) {
        Text(text = item.itemType, modifier = modifier.weight(0.5f), color = textColour)
        Text(text = item.damageType ?: "err", modifier = modifier.weight(0.5f), color = textColour)
      }
    }
    val statsMap = item.statsMap
    if (statsMap != null) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
          .fillMaxWidth()
          .fillMaxHeight()
      ) {
        for (entry in statsMap) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = entry.key)
            Text(text = entry.value.first.toString())
          }
        }
      }
    }
  }
}