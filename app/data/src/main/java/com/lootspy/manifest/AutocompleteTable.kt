package com.lootspy.manifest

import android.content.ContentValues
import android.database.Cursor
import com.lootspy.types.item.BasicItem
import com.lootspy.types.item.VendorItem

class AutocompleteTable {
  companion object {
    const val TABLE_NAME = "AutocompleteItems"

    const val HASH = "hash"
    const val NAME = "name"
    const val TIER = "TIER"
    const val TYPE = "type"
    const val ICON_PATH = "icon_path"
    const val WATERMARK_PATH = "watermark_path"
    const val IS_SHELVED = "is_shelved"
    const val DAMAGE_TYPE = "damage_type"
    const val DAMAGE_ICON_PATH = "damage_icon_path"

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
        "$HASH INTEGER PRIMARY KEY, " +
        "$NAME TEXT NOT NULL, " +
        "$TIER TEXT NOT NULL, " +
        "$TYPE TEXT NOT NULL, " +
        "$ICON_PATH TEXT NOT NULL, " +
        "$WATERMARK_PATH TEXT NOT NULL, " +
        "$IS_SHELVED INTEGER NOT NULL, " +
        "$DAMAGE_TYPE TEXT NOT NULL, " +
        "$DAMAGE_ICON_PATH TEXT NOT NULL" +
        ")"

    fun toContentValues(item: VendorItem): ContentValues =
      ContentValues().apply {
        put(HASH, item.hash.toInt())
        put(NAME, item.name)
        put(TIER, item.tier)
        put(TYPE, item.itemType)
        put(ICON_PATH, item.iconPath)
        put(WATERMARK_PATH, item.watermarkPath)
        put(IS_SHELVED, if (item.isShelved) 1 else 0)
        put(DAMAGE_TYPE, item.damageType)
        put(DAMAGE_ICON_PATH, item.damageIconPath)
      }

    fun fromCursor(cursor: Cursor): VendorItem {
      val hashIndex = cursor.getColumnIndex(HASH)
      val nameIndex = cursor.getColumnIndex(NAME)
      val tierIndex = cursor.getColumnIndex(TIER)
      val typeIndex = cursor.getColumnIndex(TYPE)
      val iconPathIndex = cursor.getColumnIndex(ICON_PATH)
      val watermarkPathIndex = cursor.getColumnIndex(WATERMARK_PATH)
      val isShelvedIndex = cursor.getColumnIndex(IS_SHELVED)
      val damageTypeIndex = cursor.getColumnIndex(DAMAGE_TYPE)
      val damageIconPathIndex = cursor.getColumnIndex(DAMAGE_ICON_PATH)
      return VendorItem(
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