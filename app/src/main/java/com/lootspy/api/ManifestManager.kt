package com.lootspy.api

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManifestManager @Inject constructor(
  @ApplicationContext private val context: Context,
) {
  private var manifestDb: SQLiteDatabase? = null

  fun getManifestDbFile(): File {
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

  fun deleteManifestDb() {
    val maybeManifestDb = manifestDb
    if (maybeManifestDb != null) {
      SQLiteDatabase.deleteDatabase(getManifestDbFile())
      manifestDb = null
    }
  }

  companion object {
    private const val MANIFEST_DATABASE = "destiny_manifest.db"
  }
}