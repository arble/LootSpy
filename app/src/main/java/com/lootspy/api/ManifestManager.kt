package com.lootspy.api

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedInputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManifestManager @Inject constructor(
  @ApplicationContext private val context: Context,
) {
  private var manifestDb: SQLiteDatabase? = null
  private var manifestShortcutsDb: SQLiteDatabase? = null
  private var weaponCategories: HashSet<Int>? = null
  private var damageTypes: Map<Int, Pair<String, String>>? = null

  private fun getManifestDbFile(): File {
    return context.getDatabasePath(MANIFEST_DATABASE)
  }

  private fun getManifestShortcutDbFile(): File {
    return context.getDatabasePath(MANIFEST_SHORTCUTS)
  }

  fun getManifestDb(): SQLiteDatabase {
    val maybeManifestDb = manifestDb
    if (maybeManifestDb != null) {
      return maybeManifestDb
    }
    val openParams = SQLiteDatabase.OpenParams.Builder().build()
    val db = SQLiteDatabase.openDatabase(getManifestDbFile(), openParams)
    manifestDb = db
    return db
  }

  fun deleteManifestDb(): Boolean {
    val maybeManifestDb = manifestDb
    if (maybeManifestDb != null) {
      val deleted = SQLiteDatabase.deleteDatabase(getManifestDbFile())
      return if (deleted) {
        manifestDb = null
        true
      } else {
        false
      }
    }
    return true
  }

  @Throws(IOException::class)
  suspend fun unzipNewDatabase(
    zippedFile: File,
    progressCallback: suspend (Int, Int) -> Unit,
  ): Boolean {
    return withContext(Dispatchers.IO) {
      ZipInputStream(BufferedInputStream(FileInputStream(zippedFile))).use { input ->
        val zipEntry =
          input.nextEntry ?: throw IOException("zipped file didn't contain a readable entry")
        if (zipEntry.isDirectory) {
          throw IOException("zip entry didn't contain a file")
        }
        progressCallback(0, 0)
        val length = zipEntry.size
        var bytesInflated = 0
        var currentDecile = 0
        val decile = length / 10
        val buffer = ByteArray(4096)
        if (!deleteManifestDb()) {
          return@withContext false
        }
        val destinationFile = getManifestDbFile()
        FileOutputStream(destinationFile).use { output ->
          while (true) {
            val count = input.read(buffer)
            if (count < 0) {
              break
            }
            bytesInflated += count
            output.write(buffer, 0, count)
            if (bytesInflated > (currentDecile + 1) * decile) {
              progressCallback(currentDecile, bytesInflated)
              currentDecile++
            }
          }
          output.flush()
        }
        input.closeEntry()
        return@withContext true
      }
    }
  }

  private fun beginTransaction() = ManifestTransaction(getManifestDb())

  fun populateShortcuts(): Boolean {
    return true
  }

  private fun loadDamageTypes(): Map<Int, Pair<String, String>> {
    val maybeDamageTypes = damageTypes
    if (maybeDamageTypes != null) {
      return maybeDamageTypes
    }
    val result = HashMap<Int, Pair<String, String>>()
    getManifestDb().query("DestinyDamageTypeDefinition", null, null, null, null, null, null, "20").use {
      while (it.moveToNext()) {
        val obj = it.blobToJson()
        val displayPair = obj.displayPair("name", "icon")
        val hash = obj["hash"]?.jsonPrimitive?.int
        if (displayPair != null && hash != null) {
          result[hash] = displayPair
        }
      }
    }
    damageTypes = result
    return result
  }

  private fun populateItemAutocomplete(): Boolean {
    val db = getManifestDb()
    var limit = 0
    var exit = false
    db.query("DestinyInventoryItemDefinition", null, null, null, null, null, null, "20").use {
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
        val obj = Json.decodeFromString<JsonObject>(cutstring)
        val displayObj = obj["displayProperties"]?.jsonObject
        if (displayObj != null) {
          val name = displayObj["name"]
          val icon = displayObj["icon"]
        }
        val categoryHashesArray = obj["itemCategoryHashes"]?.jsonArray
        if (categoryHashesArray != null) {
          for (element in categoryHashesArray) {
            if (getWeaponCategories().contains(element.jsonPrimitive.int)) {

            }
          }
        }
      }
    }
    return true
  }

  fun getWeaponCategories(): Set<Int> {
    val maybeResult = weaponCategories
    if (maybeResult != null) {
      return maybeResult
    }
    val result = HashSet<Int>()
    getManifestDb().query("DestinyItemCategoryDefinition", null, null, null, null, null, null)
      .use { cursor ->
        val (idIndex, jsonIndex) = cursor.manifestColumns()
        while (cursor.moveToNext()) {
          val hash = cursor.getInt(idIndex)
          val obj = cursor.blobToJson(jsonIndex)
          val displayObj = obj["displayProperties"]?.jsonObject
          if (displayObj != null) {
            val name = displayObj["name"]?.jsonPrimitive.toString()
            if (weaponCategoryNames.contains(name)) {
              result.add(hash)
            }
          }
        }
      }
    weaponCategories = result
    return result
  }

  class ManifestTransaction(private val db: SQLiteDatabase) : Closeable {
    init {
      db.beginTransaction()
    }
    override fun close() {
      db.endTransaction()
    }

    fun setTransactionSuccessful() {
      db.setTransactionSuccessful()
    }

  }

  companion object {
    private const val MANIFEST_DATABASE = "destiny_manifest.db"
    private const val MANIFEST_SHORTCUTS = "manifest_shortcuts.db"
    private val weaponCategoryNames = setOf(
      "Auto Rifle",
      "Sidearm",
      "Hand Cannon",
      "Sniper Rifle",
      "Pulse Rifle",
      "Scout Rifle",
      "Fusion Rifle",
      "Shotgun",
      "Machine Gun",
      "Rocket Launcher",
      "Trace Rifles",
      "Bows",
      "Glaives",
      "Sword",
      "Grenade Launchers",
      "Linear Fusion Rifles",
    )
  }
}