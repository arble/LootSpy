package com.lootspy.api.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.lootspy.manifest.ManifestManager
import com.lootspy.api.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PrepareAutocompleteWorker @AssistedInject constructor(
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