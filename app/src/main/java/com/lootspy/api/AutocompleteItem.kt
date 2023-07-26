package com.lootspy.api

import android.content.ContentValues
import android.database.Cursor
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
import com.lootspy.R
import com.lootspy.api.manifest.AutocompleteTable
import com.lootspy.util.BungiePathHelper

data class AutocompleteItem(
  val hash: UInt,
  val name: String,
  val tier: String,
  val type: String,
  val iconPath: String,
  val watermarkPath: String,
  val isShelved: Boolean,
  val damageType: String,
  val damageIconPath: String,
) {

  fun toContentValues(): ContentValues =
    ContentValues().apply {
      put(AutocompleteTable.HASH, hash.toInt())
      put(AutocompleteTable.NAME, name)
      put(AutocompleteTable.TIER, tier)
      put(AutocompleteTable.TYPE, type)
      put(AutocompleteTable.ICON_PATH, iconPath)
      put(AutocompleteTable.WATERMARK_PATH, watermarkPath)
      put(AutocompleteTable.IS_SHELVED, if (isShelved) 1 else 0)
      put(AutocompleteTable.DAMAGE_TYPE, damageType)
      put(AutocompleteTable.DAMAGE_ICON_PATH, damageIconPath)
    }

  @Composable
  fun IconBox(
    modifier: Modifier = Modifier,
    placeholderPainter: Painter,
    errorPainter: Painter,
  ) {
    Box(
      modifier = modifier
        .height(64.dp)
        .width(64.dp)
    ) {
      AsyncImage(
        model = BungiePathHelper.getFullUrlForPath(iconPath),
        placeholder = placeholderPainter,
        error = errorPainter,
        modifier = modifier
          .width(64.dp)
          .fillMaxHeight(),
        contentDescription = null
      )
      AsyncImage(
        model = BungiePathHelper.getFullUrlForPath(watermarkPath),
        placeholder = placeholderPainter,
        error = errorPainter,
        modifier = modifier
          .width(64.dp)
          .fillMaxHeight(),
        contentDescription = null
      )
    }
  }

  @Composable
  fun Composable(
    modifier: Modifier = Modifier,
    placeholder: Painter = painterResource(id = R.drawable.ic_launcher_foreground),
    error: Painter = painterResource(id = com.google.android.material.R.drawable.mtrl_ic_cancel),
    onClick: (AutocompleteItem) -> Unit = {},
    damageIconSize: Dp = 24.dp
  ) {
    val (cardColour, textColour) = when (tier) {
      "Legendary" -> Pair(LEGENDARY_CARD_BG, Color.White)
      "Exotic" -> Pair(EXOTIC_CARD_BG, Color.Black)
      else -> Pair(MaterialTheme.colorScheme.surfaceVariant, Color.White)
    }
    Card(
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(containerColor = cardColour),
    modifier = modifier
      .height(64.dp)
      .clickable { onClick(this) },
  ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
          .fillMaxWidth()
          .fillMaxHeight()
      ) {
        IconBox(placeholderPainter = placeholder, errorPainter = error)
        Text(
          text = name,
          modifier = modifier.weight(0.5f),
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          color = textColour
        )
        AsyncImage(
          model = BungiePathHelper.getFullUrlForPath(damageIconPath),
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
          Text(text = type, modifier = modifier.weight(0.5f), color = textColour)
          Text(text = damageType, modifier = modifier.weight(0.5f), color = textColour)
        }
      }
    }
  }

  companion object {
    fun fromCursor(cursor: Cursor): AutocompleteItem {
      val hashIndex = cursor.getColumnIndex(AutocompleteTable.HASH)
      val nameIndex = cursor.getColumnIndex(AutocompleteTable.NAME)
      val tierIndex = cursor.getColumnIndex(AutocompleteTable.TIER)
      val typeIndex = cursor.getColumnIndex(AutocompleteTable.TYPE)
      val iconPathIndex = cursor.getColumnIndex(AutocompleteTable.ICON_PATH)
      val watermarkPathIndex = cursor.getColumnIndex(AutocompleteTable.WATERMARK_PATH)
      val isShelvedIndex = cursor.getColumnIndex(AutocompleteTable.IS_SHELVED)
      val damageTypeIndex = cursor.getColumnIndex(AutocompleteTable.DAMAGE_TYPE)
      val damageIconPathIndex = cursor.getColumnIndex(AutocompleteTable.DAMAGE_ICON_PATH)
      return AutocompleteItem(
        cursor.getInt(hashIndex).toUInt(),
        cursor.getString(nameIndex),
        cursor.getString(tierIndex),
        cursor.getString(typeIndex),
        cursor.getString(iconPathIndex),
        cursor.getString(watermarkPathIndex),
        cursor.getInt(isShelvedIndex) > 0,
        cursor.getString(damageTypeIndex),
        cursor.getString(damageIconPathIndex),
      )
    }

    private val LEGENDARY_CARD_BG = Color(red = 0x51, green = 0x30, blue = 0x65)
    private val EXOTIC_CARD_BG = Color(red = 0xc3, green = 0xa0, blue = 0x19)
  }
}