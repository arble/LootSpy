package com.lootspy.manifest

class DeepsightTable {
  companion object {
    const val TABLE_NAME = "DeepsightProgress"

    const val ITEM_HASH = "item_hash"
    const val RECORD_HASH = "record_hash"

    const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
        "$ITEM_HASH INTEGER PRIMARY KEY, " +
        "$RECORD_HASH INTEGER NOT NULL" +
        ")"
  }
}