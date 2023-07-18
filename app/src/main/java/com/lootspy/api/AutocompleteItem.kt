package com.lootspy.api

import android.content.ContentValues
import android.database.Cursor
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.lootspy.api.manifest.AutocompleteTable

data class AutocompleteItem(
  val hash: Long,
  val name: String,
  val type: String,
  val iconPath: String,
  val watermarkPath: String,
  val damageType: String,
  val damageIconPath: String,
) {

  fun toContentValues(): ContentValues =
    ContentValues().apply {
      put(AutocompleteTable.HASH, hash)
      put(AutocompleteTable.NAME, name)
      put(AutocompleteTable.TYPE, type)
      put(AutocompleteTable.ICON_PATH, iconPath)
      put(AutocompleteTable.WATERMARK_PATH, watermarkPath)
      put(AutocompleteTable.DAMAGE_TYPE, damageType)
      put(AutocompleteTable.DAMAGE_ICON_PATH, damageIconPath)
    }

  @Composable
  fun AutoCompleteItemRow(
    modifier: Modifier = Modifier,
    dp: Dp = 32.dp
  ) {
    Row(modifier = modifier.height(dp)) {
      Box(modifier = modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
        AsyncImage(model = iconPath, contentDescription = null)
        AsyncImage(model = watermarkPath, contentDescription = null)
      }
    }
  }

  companion object {
    fun fromCursor(cursor: Cursor): AutocompleteItem {
      val hashIndex = cursor.getColumnIndex(AutocompleteTable.HASH)
      val nameIndex = cursor.getColumnIndex(AutocompleteTable.NAME)
      val typeIndex = cursor.getColumnIndex(AutocompleteTable.TYPE)
      val iconPathIndex = cursor.getColumnIndex(AutocompleteTable.ICON_PATH)
      val watermarkPathIndex = cursor.getColumnIndex(AutocompleteTable.WATERMARK_PATH)
      val damageTypeIndex = cursor.getColumnIndex(AutocompleteTable.DAMAGE_TYPE)
      val damageIconPathIndex = cursor.getColumnIndex(AutocompleteTable.DAMAGE_ICON_PATH)
      return AutocompleteItem(
        cursor.getLong(hashIndex),
        cursor.getString(nameIndex),
        cursor.getString(typeIndex),
        cursor.getString(iconPathIndex),
        cursor.getString(watermarkPathIndex),
        cursor.getString(damageTypeIndex),
        cursor.getString(damageIconPathIndex),
      )
    }
  }
}