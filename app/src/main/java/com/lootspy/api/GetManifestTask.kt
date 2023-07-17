package com.lootspy.api

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.URLUtil
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lootspy.R
import com.lootspy.client.ApiClient
import com.lootspy.client.model.Destiny2GetDestinyManifest200Response
import com.lootspy.data.UserStore
import com.lootspy.util.BungiePathHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

@HiltWorker
class GetManifestTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val userStore: UserStore,
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
    Log.d(LOG_TAG, "Retrieved manifest data. $data")
    // TODO: localise
    val enManifestPath = data?.get("en") ?: return Result.failure()
    val fullPath = BungiePathHelper.getFullUrlForPath(enManifestPath)
    val manifestUri = Uri.parse(BungiePathHelper.getFullUrlForPath(enManifestPath))
    val fileName = URLUtil.guessFileName(fullPath, null, null)
    if (fileName == null) {
      Log.e(LOG_TAG, "Could not resolve filename from manifest URL")
      return Result.failure()
    }
    if (fileName == lastManifest) {
      // we already have this manifest, but may still need to unzip it
      Log.d(LOG_TAG, "Manifest unchanged since last check")
      return Result.success(workDataOf(MANIFEST_FILE_KEY to fileName))
    }
    val manifestDir = File(context.filesDir, "DestinyManifest")
    if (!manifestDir.exists()) {
      manifestDir.mkdirs()
    }
    val manifestFile = File(manifestDir, fileName)
    return try {
      Log.d(LOG_TAG, "Beginning download of Destiny manifest")
      downloadManifestFile(manifestUri, manifestFile)
      userStore.saveLastManifest(fileName)
      Log.d(LOG_TAG, "Completed download of Destiny manifest")
      Result.success(workDataOf(MANIFEST_FILE_KEY to fileName))
    } catch (e: IOException) {
      Log.e(LOG_TAG, "Failed to download manifest file: $e")
      Result.failure()
    }
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
              setProgress(workDataOf(context.getString(R.string.tasks_download_progress) to currentDecile))
              Log.d(LOG_TAG, "Downloaded $bytesTransferred bytes")
              currentDecile++
            }
          }
          output.flush()
        }
      }
    }
  }

  companion object {
    private const val MANIFEST_FILE_KEY = "manifest_file"
    private const val LOG_TAG = "LootSpy Manifest Sync"
  }
}
