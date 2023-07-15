package com.lootspy.util

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.WorkRequest

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
      val (workManager, workRequest) = getManagerAndInitialRequest(
        context,
        workerClass,
        workData
      )
      workManager.enqueueUniqueWork(workName, policy, workRequest)
    }

    fun dispatchUniqueWorkerLinearFollowers(
      context: Context,
      initialWorkerClass: Class<out ListenableWorker>,
      workName: String,
      workData: Map<String, String>? = null,
      followingJobs: List<Class<out ListenableWorker>>,
      policy: ExistingWorkPolicy = ExistingWorkPolicy.KEEP
    ) {
      val (workManager, initialRequest) = getManagerAndInitialRequest(
        context,
        initialWorkerClass,
        workData
      )
      var chain = workManager.beginUniqueWork(workName, policy, initialRequest)
      for (subsequentClass in followingJobs) {
        chain = chain.then(OneTimeWorkRequest.Builder(subsequentClass).build())
      }
      chain.enqueue()
    }

    private fun getManagerAndInitialRequest(
      context: Context,
      workerClass: Class<out ListenableWorker>,
      workData: Map<String, String>?,
    ): Pair<WorkManager, OneTimeWorkRequest> {
      val workManager = WorkManager.getInstance(context)
      val data = Data.Builder()
      if (workData != null) {
        data.putAll(workData)
      }
      val workRequest = OneTimeWorkRequest.Builder(workerClass)
        .setInputData(data.build())
        .build()
      return Pair(workManager, workRequest)
    }
  }
}