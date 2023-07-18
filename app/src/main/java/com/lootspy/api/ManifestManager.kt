package com.lootspy.api

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.lootspy.api.manifest.AutocompleteTable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
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
  private val weaponCategories = HashMap<Long, String>()
  private val damageTypes = HashMap<Long, Pair<String, String>>()

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

  private fun loadItemAutocomplete(db: SQLiteDatabase): Boolean {
    db.rawQuery(
      "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '${AutocompleteTable.TABLE_NAME}'",
      null
    ).use {
      if (it.count == 0) {
        return false
      }
    }
    db.rawQuery("SELECT * FROM ${AutocompleteTable.TABLE_NAME}", null).use {
      while (it.moveToNext()) {
        autocompleteHelper.insert(AutocompleteItem.fromCursor(it))
      }
    }
    return true
  }

  suspend fun populateItemAutocomplete(progressCallback: suspend (Int) -> Unit = {}) {
    val db = getManifestDb()
    if (loadItemAutocomplete(db)) {
      // we did this already
      Log.d(LOG_TAG, "Loaded autocomplete table")
      return
    }
    var offset = 0
    var numInserted = 0
    var numProcessed = 0
    var exit = false
    loadDamageTypes()
    loadWeaponCategories()
    Log.d(LOG_TAG, "Generating new autocomplete table from manifest")
    val rowCount =
      db.rawQuery("SELECT COUNT(*) AS total FROM DestinyInventoryItemDefinition", null).use {
        if (it.moveToFirst()) {
          return@use it.getInt(it.getColumnIndexOrThrow("total"))
        } else {
          return@use 0
        }
      }
    if (rowCount == 0) {
      Log.e(LOG_TAG, "No rows in DestinyInventoryItemDefinition table. Definitely a bug!")
      return
    }
    val decile = rowCount / 10
    var currentDecile = 0
    val successorItems = HashMap<String, Triple<Long, String, String>>()
    while (true) {
      db.rawQuery("SELECT * FROM DestinyInventoryItemDefinition LIMIT 250 OFFSET $offset", null)
        .use { cursor ->
          if (cursor.count == 0) {
            exit = true
            return@use
          }
          outer@ while (cursor.moveToNext()) {
            if (++numProcessed > (currentDecile + 1) * decile) {
              progressCallback(currentDecile)
              currentDecile++
            }
            val obj = cursor.blobToJson()
            val (name, icon) = obj.displayPair("name", "icon") ?: continue
            val watermark = obj["iconWatermark"]?.jsonPrimitive?.content ?: continue
            val hash = obj["hash"]?.jsonPrimitive?.long ?: continue
            val categoryHashesArray = obj["itemCategoryHashes"]?.jsonArray ?: continue
            for (element in categoryHashesArray) {
              val categoryHash = element.jsonPrimitive.long
              val category = weaponCategories[categoryHash] ?: continue
              val defaultDamageTypeHash = obj["defaultDamageTypeHash"]?.jsonPrimitive
              if (defaultDamageTypeHash == null) {
                successorItems[name] = Triple(hash, icon, watermark)
                continue@outer
              }
              val damageTypeInfo = damageTypes[defaultDamageTypeHash.long] ?: continue@outer
              val didInsert = autocompleteHelper.insert(
                AutocompleteItem(
                  hash = hash,
                  name = name,
                  type = category,
                  iconPath = icon,
                  watermarkPath = watermark,
                  damageType = damageTypeInfo.first,
                  damageIconPath = damageTypeInfo.second
                )
              )
              if (didInsert) {
                numInserted++
              }
            }
          }
        }
      if (exit) {
        break
      }
      offset += 250
    }
    for (entry in successorItems) {
      val name = entry.key
      val precursorItem = autocompleteHelper.items[name] ?: continue
      val (hash, icon, watermark) = entry.value
      val successorItem = AutocompleteItem(
        hash = hash,
        name = name,
        type = precursorItem.type,
        iconPath = icon,
        watermarkPath = watermark,
        damageType = precursorItem.damageType,
        damageIconPath = precursorItem.damageIconPath,
      )
      autocompleteHelper.insertSuccessor(successorItem)
    }
    if (numInserted == 0) {
      Log.w(LOG_TAG, "Exiting early due to no valid items read")
      return
    }
    Log.d(LOG_TAG, "Read $numProcessed rows and parsed $numInserted items")
    beginTransaction().use { transaction ->
      db.execSQL(AutocompleteTable.CREATE_TABLE)
      for (item in autocompleteHelper.items.values) {
        db.insertWithOnConflict(
          AutocompleteTable.TABLE_NAME,
          null,
          item.toContentValues(),
          SQLiteDatabase.CONFLICT_REPLACE
        )
      }
      transaction.setTransactionSuccessful()
    }
    Log.d(LOG_TAG, "Wrote autocomplete database")
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
          val hash = obj["hash"]?.jsonPrimitive?.long
          if (displayPair != null && hash != null) {
            damageTypes[hash] = displayPair
          }
        }
      }
  }

  private fun loadWeaponCategories() {
    if (weaponCategories.isNotEmpty()) {
      return
    }
    getManifestDb().query("DestinyItemCategoryDefinition", null, null, null, null, null, null)
      .use { cursor ->
        val jsonIndex = cursor.getColumnIndex("json")
        while (cursor.moveToNext()) {
          val obj = cursor.blobToJson(jsonIndex)
          val name =
            obj["displayProperties"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: continue
          val hash = obj["hash"]?.jsonPrimitive?.long ?: continue
          if (weaponCategoryNames.contains(name)) {
            weaponCategories[hash] = name
          }
        }
      }
  }

  fun dropAutocompleteTable() {
    getManifestDb().execSQL("DROP TABLE IF EXISTS ${AutocompleteTable.TABLE_NAME}")
  }

  private class ManifestTransaction(private val db: SQLiteDatabase) : Closeable {
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
    private const val LOG_TAG = "LootSpy Manifest"
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