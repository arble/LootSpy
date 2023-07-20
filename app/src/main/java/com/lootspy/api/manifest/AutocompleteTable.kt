package com.lootspy.api.manifest

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

  }
}