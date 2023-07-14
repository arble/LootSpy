package com.lootspy.api

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lootspy.client.ApiClient
import com.lootspy.client.model.Destiny2GetDestinyManifest200Response
import com.lootspy.data.UserStore
import com.lootspy.util.BungiePathHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.zip.ZipInputStream

@HiltWorker
class GetManifestTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    val accessToken = userStore.accessToken.first()
    val lastManifest = userStore.lastManifest.first()
    val apiClient = ApiClient()
    apiClient.setAccessToken(accessToken)
    val apiPath = "/Destiny2/Manifest/"
    val call = apiClient.buildBungieCall(apiPath)
    val apiResponse = apiClient.executeTyped<Destiny2GetDestinyManifest200Response>(call)
    if (apiResponse.statusCode != 200) {
      return Result.failure()
    }
    val data = apiResponse.data.response?.mobileWorldContentPaths
    Log.d("LootSpy API Sync", "Retrieved manifest data. $data")
    val enManifestPath = data?.get("en") ?: return Result.failure()
    val manifestUri = Uri.parse(BungiePathHelper.getFullUrlForPath(enManifestPath))
    val fileName = context.contentResolver.query(manifestUri, null, null, null).use {
      it?.moveToFirst()
      val columnIndex = it?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
      if (columnIndex != null) it.getString(columnIndex) else null
    }
    if (fileName.equals(lastManifest)) {
      // we already have this manifest
      return Result.success()
    }
    val manifestDir = File(context.filesDir, "DestinyManifest")
    if (!manifestDir.exists()) {
      manifestDir.mkdirs()
    }
    if (fileName != null) {
      val maybeExistingManifest = File(manifestDir, fileName)
      try {
        downloadManifestFile(manifestUri, maybeExistingManifest)
        userStore.saveLastManifest(fileName)
      } catch (e: IOException) {
        Log.e("LootSpy API Sync", "Failed to download manifest file: $e")
        return Result.failure()
      }
      try {
        unzipToDatabase(maybeExistingManifest, context)
        userStore.saveLastManifestDb(fileName)
      } catch (e: IOException) {
        Log.e("LootSpy API Sync", "Failed to unzip manifest file: $e")
      }

    }
    return Result.success()
  }

  @Throws(IOException::class)
  private suspend fun downloadManifestFile(manifestUri: Uri, destination: File) {
    withContext(Dispatchers.IO) {
      val connection = URL(manifestUri.toString()).openConnection()
      val length = connection.contentLength
      var bytesTransferred = 0
      var currentDecile = 0
      val decile = length / 10
      connection.getInputStream().use { input ->
        FileOutputStream(destination).use { output ->
          setProgress(workDataOf("Downloading" to 0))
          val buf = ByteArray(4096)
          while (true) {
            val byteCount = input.read(buf)
            if (byteCount < 0) {
              break
            }
            output.write(buf, 0, byteCount)
            bytesTransferred += byteCount
            if (bytesTransferred > (currentDecile + 1) * decile) {
              setProgress(workDataOf("Downloading" to currentDecile * 10))
              currentDecile++
            }
          }
          output.flush()
        }
      }
    }
  }

  @Throws(IOException::class)
  private suspend fun unzipToDatabase(zippedFile: File, context: Context) {
    withContext(Dispatchers.IO) {
      ZipInputStream(BufferedInputStream(FileInputStream(zippedFile))).use { input ->
        val zipEntry =
          input.nextEntry ?: throw IOException("zipped file didn't contain a readable entry")
        if (zipEntry.isDirectory) {
          throw IOException("zip entry didn't contain a file")
        }
        setProgress(workDataOf("Unzipping" to 0))
        val length = zipEntry.size
        var bytesInflated = 0
        var currentDecile = 0
        val decile = length / 10
        val buffer = ByteArray(4096)
        val destinationFile = context.getDatabasePath("destiny_manifest.db")
        FileOutputStream(destinationFile).use { output ->
          while (true) {
            val count = input.read(buffer)
            if (count < 0) {
              break
            }
            bytesInflated += count
            output.write(buffer, 0, count)
            if (bytesInflated > (currentDecile + 1) * decile) {
              setProgress(workDataOf("Unzipping" to currentDecile * 10))
              currentDecile++
            }
          }
          output.flush()
        }
        input.closeEntry()
      }
    }
  }
}
