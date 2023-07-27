package com.lootspy.api.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lootspy.api.ManifestManager
import com.lootspy.api.R
import com.lootspy.data.UserStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.IOException

@HiltWorker
class UnzipManifestWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore,
  private val manifestManager: ManifestManager,
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val lastManifestDb = userStore.lastManifestDb.first()
    val fileName = inputData.getString(MANIFEST_FILE_KEY)
    if (lastManifestDb == fileName) {
      // nothing to do
      return Result.success()
    } else if (fileName == null) {
      Log.e(LOG_TAG, "No filename provided to unzip job. Please report this!")
      return Result.failure()
    }
    val manifestDir = File(context.filesDir, "DestinyManifest")
    try {
      val manifestFile = File(manifestDir, fileName)
      Log.d(LOG_TAG, "Beginning unzip of Destiny manifest")
      setProgress(workDataOf("Unzipping" to 0))
      val updated = manifestManager.unzipNewDatabase(manifestFile) { progress, bytes ->
        setProgress(workDataOf(context.getString(R.string.tasks_unzip_progress) to progress))
        Log.d(LOG_TAG, "Inflated $bytes bytes")
      }
      return if (updated) {
        Log.d(LOG_TAG, "Updated manifest database")
        userStore.saveLastManifestDb(fileName)
        manifestFile.delete()
        Log.d(LOG_TAG, "Completed unzip of Destiny manifest")
        Result.success()
      } else {
        Log.e(LOG_TAG, "Failed to update manifest database")
        Result.failure()
      }
    } catch (e: IOException) {
      Log.e(LOG_TAG, "Failed to unzip manifest file: $e")
      return Result.failure()
    }
  }

  companion object {
    private const val MANIFEST_FILE_KEY = "manifest_file"
    private const val LOG_TAG = "LootSpy Manifest Unzip"
  }
}