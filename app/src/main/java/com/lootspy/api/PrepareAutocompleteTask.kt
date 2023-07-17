package com.lootspy.api

import android.content.Context
import android.util.Log
import androidx.compose.ui.res.stringResource
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lootspy.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

@HiltWorker
class PrepareAutocompleteTask @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val manifestManager: ManifestManager
) : CoroutineWorker(context, params) {
  override suspend fun doWork(): Result {
    manifestManager.populateItemAutocomplete {
      setProgress(workDataOf(context.getString(R.string.tasks_autocomplete_progress) to it))
    }
    return Result.success()
  }

  companion object {
    private const val LOG_TAG = "LootSpy Manifest Optimiser"
  }
}