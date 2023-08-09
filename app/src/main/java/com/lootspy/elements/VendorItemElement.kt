package com.lootspy.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lootspy.api.R
import com.lootspy.data.bungiePath
import com.lootspy.types.item.VendorItem

@Composable
fun VendorItemElement(
  item: VendorItem,
  modifier: Modifier = Modifier,
  damageIconSize: Dp = 24.dp,
  onClick: (VendorItem) -> Unit = {},
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
    VendorItemElementHeader(item = item, cardColour = cardColour, textColour = textColour)
  }
}

@Composable
fun VendorItemElementHeader(
  item: VendorItem,
  modifier: Modifier = Modifier,
  placeholder: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
  error: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
  cardColour: Color,
  textColour: Color,
  damageIconSize: Dp = 24.dp,
  onClick: (VendorItem) -> Unit = {},
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .fillMaxWidth()
      .height(64.dp)
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
}

@Composable
fun VendorItemDetails(
  item: VendorItem,
  modifier: Modifier = Modifier,
  placeholder: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
  error: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
  damageIconSize: Dp = 24.dp,
  onClick: (VendorItem) -> Unit = {},
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
//      .height(128.dp)
      .clickable { onClick(item) },
  ) {
    VendorItemElementHeader(item = item, cardColour = cardColour, textColour = textColour)
    val statsMap = item.itemStatMap()
    if (statsMap != null) {
      for (entry in statsMap) {
        Row(modifier = Modifier.height(20.dp)) {
          val filledBarWeight = entry.value.toFloat() / 200f
          val emptyBarWeight = 0.5f - filledBarWeight
          Text(text = entry.key, modifier = Modifier.weight(0.35f), textAlign = TextAlign.Right)
          Text(
            text = entry.value.toString(),
            modifier = Modifier.weight(0.1f),
            textAlign = TextAlign.Right
          )
          Spacer(modifier = Modifier.width(8.dp))
          if (filledBarWeight > 0.0f) {
            Box(
              modifier = Modifier
                .weight(filledBarWeight)
                .fillMaxHeight()
                .clip(shape = RectangleShape)
                .background(Color.White)
            )
          }
          if (emptyBarWeight > 0.0f) {
            Box(
              modifier = Modifier
                .weight(emptyBarWeight)
                .fillMaxHeight()
                .clip(shape = RectangleShape)
                .background(Color.Black)
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
      }
    }
    val perkList = item.perkArray
    if (perkList != null) {
      for (slotPerks in perkList) {
        val (firstPerkList, otherPerksIndexed) = slotPerks.withIndex().partition {
          it.index == 0
        }
        val firstPerk = firstPerkList.getOrNull(0)?.value ?: continue
        if (firstPerk.name == "Default Shader") {
          continue
        }
        val otherPerks = otherPerksIndexed.map { it.value }
        Row(modifier = Modifier.height(48.dp), verticalAlignment = Alignment.CenterVertically) {
          AsyncImage(
            model = firstPerk.iconPath.bungiePath(),
            placeholder = placeholder,
            error = error,
            modifier = modifier
              .width(48.dp)
              .fillMaxHeight(),
            contentDescription = null
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = firstPerk.name)
          Spacer(modifier = Modifier.weight(1f))
          for (otherPerk in otherPerks) {
            AsyncImage(
              model = otherPerk.iconPath.bungiePath(),
              placeholder = placeholder,
              error = error,
              modifier = modifier
                .width(48.dp)
                .fillMaxHeight(),
              contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }
    }
  }
}