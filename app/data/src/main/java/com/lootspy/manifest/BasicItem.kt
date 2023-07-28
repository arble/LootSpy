package com.lootspy.manifest

import android.content.ContentValues
import android.database.Cursor

open class BasicItem(
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

  companion object {
    fun fromCursor(cursor: Cursor): BasicItem {
      val hashIndex = cursor.getColumnIndex(AutocompleteTable.HASH)
      val nameIndex = cursor.getColumnIndex(AutocompleteTable.NAME)
      val tierIndex = cursor.getColumnIndex(AutocompleteTable.TIER)
      val typeIndex = cursor.getColumnIndex(AutocompleteTable.TYPE)
      val iconPathIndex = cursor.getColumnIndex(AutocompleteTable.ICON_PATH)
      val watermarkPathIndex = cursor.getColumnIndex(AutocompleteTable.WATERMARK_PATH)
      val isShelvedIndex = cursor.getColumnIndex(AutocompleteTable.IS_SHELVED)
      val damageTypeIndex = cursor.getColumnIndex(AutocompleteTable.DAMAGE_TYPE)
      val damageIconPathIndex = cursor.getColumnIndex(AutocompleteTable.DAMAGE_ICON_PATH)
      return BasicItem(
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
  }
}