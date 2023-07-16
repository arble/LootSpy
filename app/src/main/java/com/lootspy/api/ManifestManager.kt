package com.lootspy.api

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
  private val autocompleteHelper: AutocompleteHelper,
) {
  private var manifestDb: SQLiteDatabase? = null
  private val weaponCategories = HashMap<Int, String>()
  private val damageTypes = HashMap<Int, Pair<String, String>>()

  private fun getManifestDbFile(): File {
    return context.getDatabasePath(MANIFEST_DATABASE)
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

  private fun loadDamageTypes() {
    if (damageTypes.isNotEmpty()) {
      return
    }
    getManifestDb().query("DestinyDamageTypeDefinition", null, null, null, null, null, null, "20")
      .use {
        while (it.moveToNext()) {
          val obj = it.blobToJson()
          val displayPair = obj.displayPair("name", "icon")
          val hash = obj["hash"]?.jsonPrimitive?.int
          if (displayPair != null && hash != null) {
            damageTypes[hash] = displayPair
          }
        }
      }
  }

  private fun populateItemAutocomplete(): Boolean {
    val db = getManifestDb()
    var limit = 0
    var exit = false
    loadDamageTypes()
    loadWeaponCategories()
    while (true) {
      val limitString = "$limit,${limit + 500}"
      db.query("DestinyInventoryItemDefinition", null, null, null, null, null, null, limitString)
        .use { cursor ->
          if (cursor.count == 0) {
            exit = true
            return@use
          }
          while (cursor.moveToNext()) {
            val obj = cursor.blobToJson()
            val props = obj.displayTriple("name", "icon", "iconWatermark") ?: continue
            val categoryHashesArray = obj["itemCategoryHashes"]?.jsonArray
            val defaultDamageTypeHash = obj["defaultDamageTypeHash"]?.jsonPrimitive ?: continue
            val damageTypeInfo = damageTypes[defaultDamageTypeHash.int] ?: continue
            if (categoryHashesArray != null) {
              for (element in categoryHashesArray) {
                val hash = element.jsonPrimitive.int
                val category = weaponCategories[hash] ?: continue
                autocompleteHelper.insert(
                  AutocompleteItem(
                    name = props.first,
                    type = category,
                    iconPath = props.second,
                    watermarkPath = props.third,
                    damageType = damageTypeInfo.first,
                    damageIconPath = damageTypeInfo.second
                  )
                )
              }
            }
          }
        }
      if (exit) {
        break
      }
      limit += 500
    }
    return true
  }

  fun loadWeaponCategories() {
    if (weaponCategories.isNotEmpty()) {
      return
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