package com.lootspy.api

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.lootspy.api.manifest.AutocompleteTable
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
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

typealias SuccessorMap = MutableMap<String, Triple<UInt, String, String>>

@Singleton
class ManifestManager @Inject constructor(
  @ApplicationContext private val context: Context,
  private val autocompleteHelper: AutocompleteHelper,
) {
  private var manifestDb: SQLiteDatabase? = null
  private val weaponCategories = HashMap<UInt, String>()
  private val tierHashes = HashMap<UInt, String>()
  private val damageTypes = HashMap<UInt, Pair<String, String>>()
  private val powerCaps = HashMap<UInt, Int>()

  private fun getManifestDbFile(): File {
    return context.getDatabasePath(MANIFEST_DATABASE)
  }

  private fun getManifestDb(): SQLiteDatabase {
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

  private fun findWantedCategory(obj: JsonObject): String? {
    val categoryHashArray = obj["itemCategoryHashes"]?.jsonArray ?: return null
    for (categoryHash in categoryHashArray) {
      return weaponCategories[categoryHash.jsonPrimitive.long.toUInt()] ?: continue
    }
    return null
  }

  private fun getWatermarkAndShelved(obj: JsonObject): Pair<String, Boolean>? {
    val qualityObj = obj["quality"]?.jsonObject ?: return null
    val powerCapObj =
      qualityObj["versions"]?.jsonArray?.getOrNull(0)?.jsonObject ?: return null
    val powerCapHash =
      powerCapObj["powerCapHash"]?.jsonPrimitive?.long?.toUInt() ?: return null
    val watermark =
      qualityObj["displayVersionWatermarkIcons"]?.jsonArray?.getOrNull(0)?.jsonPrimitive?.content
        ?: return null
    val isShelved = powerCaps.getOrDefault(powerCapHash, 999999) < 10000
    return Pair(watermark, isShelved)
  }

  private fun makeAutocompleteItem(
    hash: UInt,
    obj: JsonObject,
    successorItems: SuccessorMap
  ): AutocompleteItem? {
    // Check categories first, to bail early on the most possible items
    val category = findWantedCategory(obj) ?: return null
    val tierTypeHash =
      obj["inventory"]?.jsonObject?.get("tierTypeHash")?.jsonPrimitive?.long ?: return null
    if (!tierHashes.contains(tierTypeHash.toUInt())) {
      return null
    }
    val (name, icon) = obj.displayPair("name", "icon") ?: return null
    val watermarkShelvedPair = getWatermarkAndShelved(obj) ?: return null
    val (watermark, isShelved) = watermarkShelvedPair
    val defaultDamageTypeHash = obj["defaultDamageTypeHash"]?.jsonPrimitive
    if (defaultDamageTypeHash == null) {
      successorItems[name] = Triple(hash, icon, watermark)
      return null
    }
    val damageTypeInfo = damageTypes[defaultDamageTypeHash.long.toUInt()] ?: return null
    return AutocompleteItem(
      hash = hash,
      name = name,
      type = category,
      iconPath = icon,
      watermarkPath = watermark,
      isShelved = isShelved,
      damageType = damageTypeInfo.first,
      damageIconPath = damageTypeInfo.second
    )
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
    loadTiers()
    loadPowerCaps()
    Log.d(LOG_TAG, "Generating new autocomplete table from manifest")
    // Get a row count first so that we can report accurate progress to the user
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
    val successorItems: SuccessorMap = HashMap()
    while (true) {
      db.rawQuery("SELECT * FROM DestinyInventoryItemDefinition LIMIT 250 OFFSET $offset", null)
        .use { cursor ->
          if (cursor.count == 0) {
            exit = true
            return@use
          }
          while (cursor.moveToNext()) {
            if (++numProcessed > (currentDecile + 1) * decile) {
              progressCallback(currentDecile)
              currentDecile++
            }
            val (hash, obj) = cursor.manifestColumns()
            val item = makeAutocompleteItem(hash, obj, successorItems) ?: continue
            if (autocompleteHelper.insert(item)) {
              numInserted++
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
        // TODO: is this always valid?
        isShelved = false,
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
          val (hash, obj) = it.manifestColumns()
          val displayPair = obj.displayPair("name", "icon")
          if (displayPair != null) {
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
        while (cursor.moveToNext()) {
          val (hash, obj) = cursor.manifestColumns()
          val name =
            obj["displayProperties"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: continue
          if (weaponCategoryNames.contains(name)) {
            weaponCategories[hash] = name
          }
        }
      }
  }

  private fun loadTiers() {
    if (tierHashes.isNotEmpty()) {
      return
    }
    getManifestDb().rawQuery("SELECT * FROM DestinyItemTierTypeDefinition", null).use {
      while (it.moveToNext()) {
        val obj = it.blobToJson()
        val hash = it.getInt(it.getColumnIndexOrThrow("id")).toUInt()
        val displayProperties = obj["displayProperties"]?.jsonObject ?: continue
        val name = displayProperties["name"]?.jsonPrimitive?.content ?: continue
        if (name != "Legendary" && name != "Exotic") {
          continue
        }
        tierHashes[hash] = name
      }
    }
  }

  private fun loadPowerCaps() {
    if (powerCaps.isNotEmpty()) {
      return
    }
    getManifestDb().rawQuery("SELECT * FROM DestinyPowerCapDefinition", null).use {
      while (it.moveToNext()) {
        val (hash, obj) = it.manifestColumns()
        val powerCap = obj["powerCap"]?.jsonPrimitive?.int ?: continue
        powerCaps[hash] = powerCap
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