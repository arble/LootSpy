package com.lootspy.manifest

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.lootspy.types.item.BasicItem
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
typealias RowProcessor = suspend (Cursor) -> Unit

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
  private val raceMap = mutableMapOf<UInt, String>()
  private val classMap = mutableMapOf<UInt, String>()
  private val craftingMap = mutableMapOf<UInt, UInt>()

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
        autocompleteHelper.insert(AutocompleteTable.fromCursor(it))
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

  private fun makeBasicItem(
    hash: UInt,
    obj: JsonObject,
    successorItems: SuccessorMap
  ): Pair<BasicItem, UInt?>? {
    // Check categories first, to bail early on the most possible items
    val category = findWantedCategory(obj) ?: return null
    val tierTypeHash =
      obj["inventory"]?.jsonObject?.get("tierTypeHash")?.jsonPrimitive?.long ?: return null
    val craftHash =
      obj["inventory"]?.jsonObject?.get("recipeItemHash")?.jsonPrimitive?.long?.toUInt()
    val tier = tierHashes[tierTypeHash.toUInt()] ?: return null
    val (name, icon) = obj.displayPair("name", "icon") ?: return null
    val watermarkShelvedPair = getWatermarkAndShelved(obj) ?: return null
    val (watermark, isShelved) = watermarkShelvedPair
    val defaultDamageTypeHash = obj["defaultDamageTypeHash"]?.jsonPrimitive
    if (defaultDamageTypeHash == null) {
      successorItems[name] = Triple(hash, icon, watermark)
      return null
    }
    val damageTypeInfo = damageTypes[defaultDamageTypeHash.long.toUInt()] ?: return null
    return Pair(
      BasicItem(
        hash = hash,
        name = name,
        tier = tier,
        type = category,
        iconPath = icon,
        watermarkPath = watermark,
        isShelved = isShelved,
        damageType = damageTypeInfo.first,
        damageIconPath = damageTypeInfo.second,
      ), craftHash
    )
  }

  private fun getTableRowCount(db: SQLiteDatabase, tableName: String) =
    db.rawQuery("SELECT COUNT(*) AS total FROM $tableName", null).use {
      if (it.moveToFirst()) {
        return@use it.getInt(it.getColumnIndexOrThrow("total"))
      } else {
        return@use 0
      }
    }

  private fun getTotalRowCountThrowZero(db: SQLiteDatabase, tables: Collection<String>): Int {
    var totalCount = 0
    tables.forEach {
      val rowCount = getTableRowCount(db, it)
      if (rowCount == 0) {
        throw IllegalStateException("Table $it had no rows. Please report this bug!")
      }
      totalCount += rowCount
    }
    return totalCount
  }

  private suspend fun windowedTableScan(
    db: SQLiteDatabase,
    tableName: String,
    windowSize: Int = 250,
    rowProcessor: RowProcessor,
  ) {
    var exit = false
    var offset = 0
    while (true) {
      db.rawQuery("SELECT * FROM $tableName LIMIT $windowSize OFFSET $offset", null)
        .use { cursor ->
          if (cursor.count == 0) {
            exit = true
            return@use
          }
          while (cursor.moveToNext()) {
            rowProcessor(cursor)
          }
        }
      if (exit) {
        break
      }
      offset += windowSize
    }
  }

  private fun processSuccessors(successorItems: SuccessorMap) {
    for (entry in successorItems) {
      val name = entry.key
      val precursorItem = autocompleteHelper.items[name] ?: continue
      val (hash, icon, watermark) = entry.value
      val successorItem = BasicItem(
        hash = hash,
        name = name,
        tier = precursorItem.tier,
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
  }

  private fun writeAutocompleteTable(db: SQLiteDatabase) {
    beginTransaction().use { transaction ->
      db.execSQL(AutocompleteTable.CREATE_TABLE)
      for (item in autocompleteHelper.items.values) {
        db.insertWithOnConflict(
          AutocompleteTable.TABLE_NAME,
          null,
          AutocompleteTable.toContentValues(item),
          SQLiteDatabase.CONFLICT_REPLACE
        )
      }
      transaction.setTransactionSuccessful()
    }
    Log.d(LOG_TAG, "Wrote autocomplete database")
  }

  private fun writeCraftingRecordsTable(db: SQLiteDatabase, craftableItems: Map<UInt, UInt>) {
    beginTransaction().use { transaction ->
      db.execSQL(DeepsightTable.CREATE_TABLE)
      for (mapping in craftableItems.entries) {
        db.insertWithOnConflict(
          DeepsightTable.TABLE_NAME,
          null,
          ContentValues().apply {
            put(DeepsightTable.ITEM_HASH, mapping.key.toInt())
            put(DeepsightTable.RECORD_HASH, mapping.value.toInt())
          },
          SQLiteDatabase.CONFLICT_REPLACE
        )
      }
      transaction.setTransactionSuccessful()
    }
  }

  suspend fun createShortcutTables(progressCallback: suspend (Int) -> Unit = {}) {
    val db = getManifestDb()
    if (loadItemAutocomplete(db)) {
      // we did this already
      Log.d(LOG_TAG, "Loaded autocomplete table")
      return
    }
    var numInserted = 0
    var numProcessed = 0
    loadDamageTypes()
    loadWeaponCategories()
    loadTiers()
    loadPowerCaps()
    Log.d(LOG_TAG, "Generating new autocomplete table from manifest")
    // Get a row count first so that we can report accurate progress to the user
    val rowCount = try {
      getTotalRowCountThrowZero(
        db,
        listOf("DestinyInventoryItemDefinition", "DestinyRecordDefinition")
      )
    } catch (e: IllegalStateException) {
      e.message?.let { Log.e(LOG_TAG, it) }
      return
    }
    val decile = rowCount / 10
    var currentDecile = 0
    val successorItems: SuccessorMap = HashMap()
    val craftableItems = hashMapOf<String, UInt>()

    // Autocomplete table
    windowedTableScan(db, "DestinyInventoryItemDefinition", 250) { cursor ->
      if (++numProcessed > (currentDecile + 1) * decile) {
        progressCallback(currentDecile)
        currentDecile++
      }
      val (hash, obj) = cursor.manifestColumns()
      val (item, craftHash) = makeBasicItem(hash, obj, successorItems) ?: return@windowedTableScan
      if (autocompleteHelper.insert(item)) {
        numInserted++
      }
      if (craftHash != null) {
        craftableItems[item.name] = craftHash
      }
    }
//    while (true) {
//      db.rawQuery("SELECT * FROM DestinyInventoryItemDefinition LIMIT 250 OFFSET $offset", null)
//        .use { cursor ->
//          if (cursor.count == 0) {
//            exit = true
//            return@use
//          }
//          while (cursor.moveToNext()) {
//            if (++numProcessed > (currentDecile + 1) * decile) {
//              progressCallback(currentDecile)
//              currentDecile++
//            }
//            val (hash, obj) = cursor.manifestColumns()
//            val (item, isCraftable) = makeBasicItem(hash, obj, successorItems) ?: continue
//            if (autocompleteHelper.insert(item)) {
//              numInserted++
//            }
//            if (isCraftable) {
//              craftableItemNames.add(item.name)
//            }
//          }
//        }
//      if (exit) {
//        break
//      }
//      offset += 250
//    }
    processSuccessors(successorItems)
    if (numInserted == 0) {
      Log.w(LOG_TAG, "Exiting early due to no valid items read")
      return
    }
    Log.d(LOG_TAG, "Read $numProcessed rows and parsed $numInserted items")
    writeAutocompleteTable(db)

    // Crafting progress table
    val craftingHashes = HashMap<UInt, UInt>()
    windowedTableScan(db, "DestinyRecordDefinition") { cursor ->
      if (++numProcessed > (currentDecile + 1) * decile) {
        progressCallback(currentDecile)
        currentDecile++
      }
      val (recordHash, obj) = cursor.manifestColumns()
      val name = obj.displayString("name") ?: return@windowedTableScan
      val itemHash = craftableItems[name] ?: return@windowedTableScan
      craftingHashes[itemHash] = recordHash
    }
    writeCraftingRecordsTable(db, craftingHashes)
  }

  private fun loadCraftableRecords(db: SQLiteDatabase = getManifestDb()) {
    if (craftingMap.isNotEmpty()) {
      return
    }
    db.rawQuery(
      "SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '${DeepsightTable.TABLE_NAME}'",
      null
    ).use {
      if (it.count == 0) {
        throw IllegalStateException("Deepsight table doesn't exist. This should never be possible")
      }
    }
    db.rawQuery("SELECT * FROM ${DeepsightTable.TABLE_NAME}", null).use {
      while (it.moveToNext()) {
        craftingMap[it.getInt(it.getColumnIndexOrThrow(DeepsightTable.ITEM_HASH)).toUInt()] =
          it.getInt(it.getColumnIndexOrThrow(DeepsightTable.RECORD_HASH)).toUInt()
      }
    }
  }

  fun getCraftableRecords(): Map<UInt, UInt> {
    loadCraftableRecords()
    return craftingMap
  }

  fun resolveCraftingProgress(itemHashes: Collection<UInt>): Map<UInt, Int> {
    val result = HashMap<UInt, Int>()
    val recordHashes = mutableListOf<UInt>()
    val db = getManifestDb()
    db.query(
      DeepsightTable.TABLE_NAME,
      null,
      "${DeepsightTable.ITEM_HASH} IN ${makeQuestionMarkList(itemHashes.size)}",
      itemHashes.map { it.toString() }.toTypedArray(),
      null,
      null,
      null
    ).use {
      recordHashes.add(it.getInt(it.getColumnIndexOrThrow(DeepsightTable.RECORD_HASH)).toUInt())
    }
    db.query(
      "DestinyRecordDefinition",
      null,
      "id IN ${makeQuestionMarkList(itemHashes.size)}",
      recordHashes.map { it.toString() }.toTypedArray(),
      null,
      null,
      null
    ).use {
      val (_, obj) = it.manifestColumns()

    }
    return result
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

  fun getCharacterDefinitions(): Pair<Map<UInt, String>, Map<UInt, String>> {
    if (raceMap.isNotEmpty() && classMap.isNotEmpty()) {
      return Pair(raceMap, classMap)
    }
    getManifestDb().rawQuery("SELECT * FROM DestinyRaceDefinition", null).use {
      while (it.moveToNext()) {
        val (hash, obj) = it.manifestColumns()
        val name =
          obj["displayProperties"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: continue
        raceMap[hash] = name
      }
    }
    getManifestDb().rawQuery("SELECT * FROM DestinyClassDefinition", null).use {
      while (it.moveToNext()) {
        val (hash, obj) = it.manifestColumns()
        val name = obj.displayString("name") ?: continue
        classMap[hash] = name
      }
    }
    return Pair(raceMap, classMap)
  }

  fun resolveItems(hashes: Collection<UInt>): List<BasicItem> {
    val itemMap = mutableMapOf<String, BasicItem>()
    val selectionArgs = hashes.map { it.toInt().toString() }.toTypedArray()
    val successorItems: SuccessorMap = HashMap()
    getManifestDb().query(
      "DestinyInventoryItemDefinition",
      null,
      "id IN ${makeQuestionMarkList(hashes.size)}",
      selectionArgs,
      null,
      null,
      null
    ).use {
      while (it.moveToNext()) {
        val (hash, obj) = it.manifestColumns()
        val name = obj.displayString("name") ?: continue
        val item = makeBasicItem(hash, obj, successorItems)?.first ?: continue
        itemMap[name] = item
      }
    }
    for (entry in successorItems) {
      val name = entry.key
      val precursorItem = itemMap[name] ?: continue
      val (hash, icon, watermark) = entry.value
      itemMap[name] = BasicItem(
        hash = hash,
        name = name,
        tier = precursorItem.tier,
        type = precursorItem.type,
        iconPath = icon,
        watermarkPath = watermark,
        // TODO: is this always valid?
        isShelved = false,
        damageType = precursorItem.damageType,
        damageIconPath = precursorItem.damageIconPath,
      )
    }
    return itemMap.values.toList()
  }

  private fun makeQuestionMarkList(count: Int): String {
    val builder = StringBuilder().append('(')
    builder.append(Array(count) { "?" }.joinToString(","))
    builder.append(')')
    return builder.toString()
  }

  fun getEmblemPaths(hashes: Map<UInt, Long>): Map<Long, String> {
    val result = mutableMapOf<Long, String>()
    val selectionArgs = hashes.keys.map { it.toInt().toString() }.toTypedArray()
    getManifestDb().query(
      "DestinyInventoryItemDefinition",
      null,
      "id IN ${makeQuestionMarkList(hashes.size)}",
      selectionArgs,
      null,
      null,
      null
    ).use {
      while (it.moveToNext()) {
        val (hash, obj) = it.manifestColumns()
        val icon =
          obj["displayProperties"]?.jsonObject?.get("icon")?.jsonPrimitive?.content ?: continue
        val characterId = hashes[hash] ?: continue
        result[characterId] = icon
      }
    }
    return result
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