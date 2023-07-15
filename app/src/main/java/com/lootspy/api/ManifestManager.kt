package com.lootspy.api

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.BufferedInputStream
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
  private var weaponCategories: HashSet<Int>? = null

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

  fun getWeaponCategories(): Set<Int> {
    val maybeResult = weaponCategories
    if (maybeResult != null) {
      return maybeResult
    }
    val result = HashSet<Int>()
    getManifestDb().query("DestinyItemCategoryDefinition", null, null, null, null, null, null).use { cursor ->
      val (idIndex, jsonIndex) = cursor.manifestColumns()
      while (cursor.moveToNext()) {
        val hash = cursor.getInt(idIndex)
        val blob = cursor.getBlob(jsonIndex)
        val obj = blob.manifestJsonObject()
        val displayObj = obj["displayProperties"]?.jsonObject
        if (displayObj != null) {
          val name = displayObj["name"]?.jsonPrimitive.toString()
          if (weaponCategoryNames.contains(name)) {
            result.add(hash)
          }
        }
      }
    }
    return result
  }

  companion object {
    private const val MANIFEST_DATABASE = "destiny_manifest.db"
    private val weaponCategoryNames = setOf("Auto Rifle", "Sidearm", "Hand Cannon", "Sniper Rifle")
  }
}