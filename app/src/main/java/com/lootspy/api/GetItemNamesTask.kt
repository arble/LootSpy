package com.lootspy.api

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.internal.decodeStringToJsonTree
import kotlinx.serialization.json.jsonObject

@HiltWorker
class GetItemNamesTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val manifestManager: ManifestManager
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val db = manifestManager.getManifestDb()
    var limit = 0
    var exit = false
    while (true) {
      val limitString = "$limit,${limit + 500}"
      db.query("DestinyInventoryItemDefinition", null, null, null, null, null, null, "20").use {
//        val idIndex = it.getColumnIndex("id")
//        val jsonIndex = it.getColumnIndex("json")
        val (idIndex, jsonIndex) = it.manifestColumns()
        if (it.count == 0) {
          exit = true
          return@use
        }
        while (it.moveToNext()) {
          val hash = it.getInt(idIndex)
          val blob = it.getBlob(jsonIndex)
          val jsonString = blob.toString(Charsets.US_ASCII).trim()
          val cutstring = jsonString.substring(0, jsonString.length - 1)
          Log.d(LOG_TAG, "JSON: $cutstring")
          val obj = Json.decodeFromString<JsonObject>(cutstring)
          val displayObj = obj["displayProperties"]?.jsonObject
          if (displayObj != null) {
            val name = displayObj["name"]
            val icon = displayObj["icon"]
            Log.d(LOG_TAG, "$name: $hash $icon")
          }
        }
      }
      if (exit) {
        break
      }
      limit += 500
    }
    return Result.success()
  }

  companion object {
    private const val LOG_TAG = "LootSpy Manifest Optimiser"
  }
}