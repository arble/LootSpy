package com.lootspy.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager

class WorkBuilders {
  companion object {
    fun dispatchWorker(
      context: Context,
      workerClass: Class<out ListenableWorker>,
      workData: Map<String, String>?
    ) {
      val workManager = WorkManager.getInstance(context)
      val data = Data.Builder()
      if (workData != null) {
        data.putAll(workData)
      }
      val workRequest = OneTimeWorkRequest.Builder(workerClass)
        .setInputData(data.build())
        .build()
      workManager.enqueue(workRequest)
    }

    fun dispatchUniqueWorker(
      context: Context,
      workerClass: Class<out ListenableWorker>,
      workName: String,
      workData: Map<String, String>? = null,
      policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
      val workManager = WorkManager.getInstance(context)
      val data = Data.Builder()
      if (workData != null) {
        data.putAll(workData)
      }
      val workRequest = OneTimeWorkRequest.Builder(workerClass)
        .setInputData(data.build())
        .build()
      workManager.enqueueUniqueWork(workName, policy, workRequest)
    }
  }
}